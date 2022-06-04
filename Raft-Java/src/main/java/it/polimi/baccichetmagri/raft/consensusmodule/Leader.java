package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineResult;
import it.polimi.baccichetmagri.raft.network.Configuration;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

class Leader extends ConsensusModuleAbstract {

    private static final int HEARTBEAT_TIMEOUT = 100; // the timeout for sending a heartbeat is lower than the minimum election
                                                      // timeout possible, so that elections don't start when the leader is still alive

    private final Map<Integer, Integer> nextIndex; // for each server, index of the next log entry to send to that server
                                             // (initialized to leader last log index + 1)
    private final Map<Integer, Integer> matchIndex; // for each server, index of the highest log entry known to be replicated on server
                                              // (initialized to 0, increases monotonically)

    private final ExecuteCommandQueue executeCommandQueue;

    private final Timer timer; // timer for sending heartbeats

    private final Logger logger;

    Leader(int id, Configuration configuration, Log log, StateMachine stateMachine, ConsensusModule consensusModule) {
        super(id, configuration, log, stateMachine, consensusModule);
        this.nextIndex = new ConcurrentHashMap<>();
        this.matchIndex = new ConcurrentHashMap<>();
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        int lastLogIndex = this.log.getLastLogIndex();
        while (proxies.hasNext()) {
            int proxyId = proxies.next().getId();
            this.nextIndex.put(proxyId, lastLogIndex + 1);
            this.matchIndex.put(proxyId, 0);
        }
        this.timer = new Timer();
        this.logger = Logger.getLogger(Leader.class.getName());
        this.executeCommandQueue = new ExecuteCommandQueue();
    }

    @Override
    synchronized void initialize() throws IOException {
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardAppendEntryReplies(false);

        // send initial empty AppendEntriesRPC (heartbeat)
        this.callAppendEntriesOnAllServers(this.consensusPersistentState.getCurrentTerm(), this.id,
                this.log.getLastLogIndex(), this.log.getLastLogTerm(), this.commitIndex);

        this.startHeartbeatTimer();
    }

    @Override
    public synchronized VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
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

        this.stopHeartbeatTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastLogIndex();

        // append command to local log as new entry
        LogEntry logEntry = new LogEntry(currentTerm, command);
        List<LogEntry> logEntries = new ArrayList<>();
        logEntries.add(logEntry);
        this.log.appendEntry(new LogEntry(currentTerm, command));

        // send AppendEntriesRPC in parallel to all other servers to replicate the entry
        this.callAppendEntriesOnAllServers(currentTerm, this.id, lastLogIndex, this.log.getLastLogTerm(), this.commitIndex);

        // when at least half of the servers have appended the entry into the log, execute command in the state machine
        int indexToCommit = lastLogIndex + 1;
        StateMachineResult stateMachineResult = null;
        while (this.lastApplied < indexToCommit) {
            try {
                // wait until one of the followers reply to the RPC
                ExecuteCommandDirective directive = this.executeCommandQueue.waitForFollowerReplies(indexToCommit);

                if (directive.equals(ExecuteCommandDirective.PROCEED)) {
                    // UPDATE COMMIT INDEX
                    // if there exists an N such that N > commitIndex, a majority of matchIndex[i] ≥ N, and log[N].term == currentTerm: set commitIndex = N
                    int newCommitIndex = this.commitIndex;
                    for (int i = this.commitIndex + 1; i <= this.log.getLastLogIndex(); i++) {
                        int finalI = i;
                        if (this.matchIndex.values().stream().filter(index -> index >= finalI).count() >= this.matchIndex.size()/2 + 1
                                && this.log.getEntryTerm(i) == this.consensusPersistentState.getCurrentTerm()) {
                            newCommitIndex = i;
                        }
                    }

                    // APPLY ENTRIES COMMITTED
                    // if commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
                    while (this.lastApplied < this.commitIndex) {
                        this.lastApplied++;
                        stateMachineResult = this.stateMachine.executeCommand(this.log.getEntryCommand(this.lastApplied));
                    }
                } else { // directive == INTERRUPT: the server has converted to follower, redirect client to current leader
                    return new ExecuteCommandResult(null, false, this.configuration.getLeaderIP());
                }
            } catch (InterruptedException e) {

            }
        }

        this.startHeartbeatTimer();
        return new ExecuteCommandResult(stateMachineResult, true, this.configuration.getIp());
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) {
        return 0; // TODO implementare
    }

    private void callAppendEntriesOnAllServers(int term, int leaderID, int prevLogIndex, int prevLogTerm, int leaderCommit) {
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        while (proxies.hasNext()) {
            ConsensusModuleProxy proxy = proxies.next();
            new Thread(() -> {
                boolean done = false;
                while (!done) {
                    try {
                        List<LogEntry> logEntries = this.log.getEntries(this.nextIndex.get(proxy.getId()), this.log.getLastLogIndex() + 1);
                        AppendEntryResult appendEntryResult = proxy.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
                        if (appendEntryResult.isSuccess()) {
                            // update nextIndex and matchIndex
                            this.nextIndex.put(proxy.getId(), prevLogIndex + logEntries.size() + 1);
                            this.matchIndex.put(proxy.getId(), prevLogIndex + logEntries.size());
                            this.executeCommandQueue.notifyFollowerReply();
                            done = true;
                        } else {
                            // decrement next index
                            this.nextIndex.put(proxy.getId(), this.nextIndex.get(proxy.getId()) - 1);
                        }

                    } catch (IOException e) {
                        // error in network communication, redo call
                    }
                }}).start();
        }
    }

    private void startHeartbeatTimer() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() { // send heartbeat to all servers
                try {
                    callAppendEntriesOnAllServers(consensusPersistentState.getCurrentTerm(), id,
                            log.getLastLogIndex(), log.getLastLogTerm(), commitIndex);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "An error has occurred while accessing to persistent state. The program is being terminated.");
                    e.printStackTrace();
                    Server.shutDown();
                }
            }
        }, HEARTBEAT_TIMEOUT);
    }

    private void stopHeartbeatTimer() {
        this.timer.cancel();
    }

    private Follower toFollower(Integer leaderId) {
        Follower follower = new Follower(this.id, this.configuration, this.log, this.stateMachine, this.container);
        this.container.changeConsensusModuleImpl(follower);
        this.configuration.setLeader(leaderId);
        this.executeCommandQueue.notifyAllToInterrupt();
        return follower;
    }

}
