package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.snapshot.JSONSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.machine.*;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Leader extends ConsensusModule {

    private static final int HEARTBEAT_TIMEOUT = 500; // the timeout for sending a heartbeat is lower than the minimum election
                                                      // timeout possible, so that elections don't start when the leader is still alive
    private static final int SNAPSHOT_CHUNK_SIZE = 5*1024; // Send chunks of 5 KB at a time, this parameter needs to be tuned wrt network and storage
    private Timer timer; // timer for sending heartbeats
    private final Logger logger;
    private final Map<Integer, Integer> nextIndex;
    private final Map<Integer, Integer> matchIndex;
    private final List<Thread> appendEntriesCallThreads;

    public Leader(int id, ConsensusPersistentState consensusPersistentState, int commitIndex, int lastApplied,
                  Configuration configuration, Log log, StateMachine stateMachine, ConsensusModuleContainer consensusModuleContainer) throws IOException {
        super(id, consensusPersistentState, commitIndex, lastApplied, configuration, log, stateMachine, consensusModuleContainer);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Instancing a LEADER");
        Iterator<ConsensusModuleInterface> proxies = this.configuration.getIteratorOnAllProxies();
        int lastLogIndex = this.log.getLastLogIndex();
        this.timer = new Timer();
        this.logger = Logger.getLogger(Leader.class.getName());
        this.nextIndex = new ConcurrentHashMap<>();
        this.matchIndex = new ConcurrentHashMap<>();
        while (proxies.hasNext()) {
            ConsensusModuleInterface proxy = proxies.next();
            this.nextIndex.put(proxy.getId(), lastLogIndex + 1);
            this.matchIndex.put(proxy.getId(), 0);
        }
        this.appendEntriesCallThreads = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void initialize() throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Initializing a LEADER");
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardAppendEntryReplies(false);
        this.configuration.discardInstallSnapshotReplies(false);

        // send initial empty AppendEntriesRPC (heartbeat)
        this.sendHeartbeat();

        this.startHeartbeatTimer();
    }

    @Override
    public synchronized VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Executing requestVote()");
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term > currentTerm) { // convert to follower
            Follower follower = this.toFollower(null);
            return follower.requestVote(term,candidateID, lastLogIndex, lastLogTerm);
        } else { // reply false, to notify other server that this one is the leader
            return new VoteResult(currentTerm, false);
        }
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit)
            throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Executing appendEntries()");
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term > currentTerm) { // convert to follower
            Follower follower = this.toFollower(leaderID);
            return follower.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        } else { // reply false, to notify other server that this one is the leader
            return new AppendEntryResult(currentTerm, false);
        }
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Executing executeCommand()");

        this.stopHeartbeatTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastLogIndex();

        // append command to local log as new entry
        LogEntry logEntry = new LogEntry(currentTerm, (CommandImplementation) command);
        this.log.appendEntry(logEntry, this.commitIndex);

        System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Appended new log entry w/ term: " + logEntry.getTerm() + "\u001B[0m");


        EntryReplication entryReplication = new EntryReplication();

        // send AppendEntriesRPC in parallel to all other servers to replicate the entry
        callAppendEntriesOnAllServers(entryReplication);

        // when at least half of the servers have appended the entry into the log, execute command in the state machine
        int indexToCommit = lastLogIndex + 1;
        StateMachineResult stateMachineResult = null;


        while (this.lastApplied < indexToCommit) {
            try {
                // wait until one of the followers reply to the RPC
                ExecuteCommandDirective directive = entryReplication.waitForFollowerReplies();
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Executing command: " + directive + "\u001B[0m");
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Last applied: " + this.lastApplied + "\u001B[0m");
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Commit index: " + this.commitIndex + "\u001B[0m");
                if (directive.equals(ExecuteCommandDirective.COMMIT)) {
                    // UPDATE COMMIT INDEX
                    int newCommitIndex = this.matchIndex.values().stream().sorted().collect(Collectors.toList()).get(this.matchIndex.size() / 2
                            + (((this.matchIndex.size() % 2) == 0) ? 0 : 1));
                    try {
                        if (newCommitIndex > this.commitIndex && this.log.getEntryTerm(newCommitIndex) == currentTerm) {
                            this.commitIndex = newCommitIndex;
                            System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Updated Commit index: " + this.commitIndex + "\u001B[0m");
                            // APPLY ENTRIES COMMITTED
                            stateMachineResult = this.applyCommittedEntries();
                        }
                    } catch (SnapshottedEntryException e) {
                            // should never happen, uncommitted entries are not snapshotted
                            e.printStackTrace();
                            Server.shutDown();
                        }


                } else { // directive == CONVERT_TO_FOLLOWER: the server has converted to follower, redirect client to current leader
                    this.toFollower(null);
                    return new ExecuteCommandResult(null, false, this.configuration.getLeaderIP());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.startHeartbeatTimer();
        return new ExecuteCommandResult((StateMachineResultImplementation) stateMachineResult, true, this.configuration.getIp());
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done)
        throws IOException {
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term > currentTerm) { // convert to follower
            Follower follower = this.toFollower(leaderID);
            return follower.installSnapshot(term, leaderID, lastIncludedIndex, lastIncludedTerm, offset, data, done);
        } else { // reply currentTerm, to notify other server that this one is the leader
            return currentTerm;
        }
    }

    @Override
    public String toString() {
        return "LEADER";
    }

    private void startHeartbeatTimer() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() { // send heartbeat to all servers
                try {
                    sendHeartbeat();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "An error has occurred while accessing persistent state. The program is being terminated.");
                    e.printStackTrace();
                    Server.shutDown();
                }
            }
        }, HEARTBEAT_TIMEOUT);
    }

    private void stopHeartbeatTimer() {
        this.timer.cancel();
    }

    private void sendHeartbeat() throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "][" + Thread.currentThread().getId() + "] " + "Sending heartbeat");
        this.callAppendEntriesOnAllServers(new EntryReplication());
        this.startHeartbeatTimer();
    }

    private void callAppendEntriesOnAllServers(EntryReplication entryReplication) {
        Iterator<ConsensusModuleInterface> proxies = this.configuration.getIteratorOnAllProxies();
        while(proxies.hasNext()) {
            ConsensusModuleInterface proxy = proxies.next();
            Thread thread = new Thread(() -> this.callAppendEntriesOnProxy(proxy, entryReplication));
            this.appendEntriesCallThreads.add(thread);
            thread.start();
        }
    }

    private void callAppendEntriesOnProxy(ConsensusModuleInterface proxy, EntryReplication entryReplication) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Calling append entries on server " + proxy.getId());
        try {
            int lastLogIndex = this.log.getLastLogIndex();
                boolean done = false;
                while(!done) {
                    // RETRIEVE LOG ENTRIES TO SEND
                    List<LogEntry> logEntries = null;
                    boolean allEntriesToSendNotSnapshotted = false;
                    int firstIndexToSend = nextIndex.get(proxy.getId());

                    while(!allEntriesToSendNotSnapshotted) {
                        try {
                            // the entries to send are the ones from nextIndex to the last one
                            logEntries = log.getEntries(firstIndexToSend, log.getLastLogIndex() + 1);

                            allEntriesToSendNotSnapshotted = true;
                        } catch (SnapshottedEntryException e) { // send snapshot instead of snapshotted entries
                            JSONSnapshot snapshotToSend = log.getJSONSnapshot();
                            try {
                                this.callInstallSnapshot(proxy, snapshotToSend);
                                firstIndexToSend = snapshotToSend.getLastIncludedIndex() + 1;
                            } catch (ConvertToFollowerException ex) {
                                entryReplication.convertToFollower();
                                done = true;
                            }
                        }
                    }

                    if (done) {
                        break;
                    }

                    // CALL APPEND_ENTRIES_RPC ON THE FOLLOWER
                    int prevLogIndex = firstIndexToSend - 1;
                    int prevLogTerm;
                    if (prevLogIndex == 0) { // first entry sent is the first entry of the log (no previous entry)
                        prevLogTerm = 0;
                    } else {
                        try {
                            prevLogTerm = this.log.getEntryTerm(prevLogIndex);
                        } catch (SnapshottedEntryException e) {
                            prevLogTerm = this.log.getJSONSnapshot().getLastIncludedTerm();
                        }
                    }
                    int currentTerm = this.consensusPersistentState.getCurrentTerm();

                    AppendEntryResult appendEntryResult = proxy.appendEntries(currentTerm, this.id, prevLogIndex,
                            prevLogTerm, logEntries, this.commitIndex);
                    if (appendEntryResult.isSuccess()) {
                        // update nextIndex and matchIndex
                        this.nextIndex.put(proxy.getId(), prevLogIndex + logEntries.size() + 1);
                        this.matchIndex.put(proxy.getId(), prevLogIndex + logEntries.size());
                        entryReplication.notifySuccessfulReply();
                        done = true;
                    } else {
                        if (appendEntryResult.getTerm() > currentTerm) {
                            entryReplication.convertToFollower();
                            done = true;
                        } else {
                            // decrement next index
                            this.nextIndex.put(proxy.getId(), this.nextIndex.get(proxy.getId()) - 1);
                        }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println("Append entries call to server " + proxy.getId() + " thread interrupted");
        }
        this.appendEntriesCallThreads.remove(Thread.currentThread());
    }



    private void callInstallSnapshot(ConsensusModuleInterface proxy, JSONSnapshot snapshot) throws IOException, ConvertToFollowerException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Calling installSnapshot on server " + proxy.getId());
        // convert the snapshot object into a byte array
        ByteArrayOutputStream snapshotBytesStream = new ByteArrayOutputStream();
        ObjectOutputStream snapshotObjectStream = new ObjectOutputStream(snapshotBytesStream);
        snapshotObjectStream.writeObject(snapshot);
        snapshotObjectStream.flush();
        byte[] snapshotBytes = snapshotBytesStream.toByteArray();

        // call installSnapshot on the follower
        int term = this.consensusPersistentState.getCurrentTerm();
        for (int i = 0; i < snapshotBytes.length / SNAPSHOT_CHUNK_SIZE + 1; i++) {
            int followerTerm = proxy.installSnapshot(term, this.id, snapshot.getLastIncludedIndex(), snapshot.getLastIncludedTerm(),
                    i * SNAPSHOT_CHUNK_SIZE, Arrays.copyOfRange(snapshotBytes, i * SNAPSHOT_CHUNK_SIZE, i * (SNAPSHOT_CHUNK_SIZE + 1)),
                    i * (SNAPSHOT_CHUNK_SIZE + 1) >= snapshotBytes.length);
            if (followerTerm > term) {
                throw new ConvertToFollowerException(term);
            }
        }
    }

    private StateMachineResult applyCommittedEntries() throws IOException {
        try {
            // if commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
            StateMachineResult stateMachineResult = null;
            while (this.lastApplied < this.commitIndex) {
                this.lastApplied++;
                stateMachineResult = this.stateMachine.executeCommand(this.log.getEntryCommand(this.lastApplied));
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Last applied in applyCommittedEntries(): " + this.lastApplied + "\u001B[0m");
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[46m" + "Commit index in applyCommittedEntries(): " + this.commitIndex + "\u001B[0m");
            }
            return stateMachineResult;
        } catch(SnapshottedEntryException e) {
            // should never happen, because uncommitted entries cannot be snapshotted
            e.printStackTrace();
            Server.shutDown();
        }
        return null;
    }

    private Follower toFollower(Integer leaderId) {
        Follower follower = new Follower(this.id, this.consensusPersistentState, this.commitIndex, this.lastApplied,
                this.configuration, this.log, this.stateMachine, this.container);
        for (Thread thread : this.appendEntriesCallThreads) {
            if (thread != null) {
                thread.interrupt();
            }
        }
        this.timer.cancel();
        this.container.changeConsensusModuleImpl(follower);
        this.configuration.setLeader(leaderId);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Converting to FOLLOWER");
        return follower;
    }

}
