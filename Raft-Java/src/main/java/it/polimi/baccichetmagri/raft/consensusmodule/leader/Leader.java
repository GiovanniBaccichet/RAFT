package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleAbstract;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.snapshot.JSONSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineResult;
import it.polimi.baccichetmagri.raft.network.Configuration;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Leader extends ConsensusModuleAbstract {

    private static final int HEARTBEAT_TIMEOUT = 100; // the timeout for sending a heartbeat is lower than the minimum election
                                                      // timeout possible, so that elections don't start when the leader is still alive

    private final List<AppendEntriesCall> appendEntriesCalls;

    private final IndexesToCommit indexesToCommit;

    private final Timer timer; // timer for sending heartbeats

    private final Logger logger;

    public Leader(int id, Configuration configuration, Log log, StateMachine stateMachine, ConsensusModule consensusModule) throws IOException {
        super(id, configuration, log, stateMachine, consensusModule);
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        int lastLogIndex = this.log.getLastLogIndex();
        this.appendEntriesCalls = new ArrayList<>();
        this.indexesToCommit = new IndexesToCommit();
        while (proxies.hasNext()) {
            this.appendEntriesCalls.add(new AppendEntriesCall(lastLogIndex + 1, 0, proxies.next(), this.indexesToCommit, this.log, this.id));
        }
        this.timer = new Timer();
        this.logger = Logger.getLogger(Leader.class.getName());
    }

    @Override
    public synchronized void initialize() throws IOException {
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardAppendEntryReplies(false);
        this.configuration.discardInstallSnapshotReplies(false);

        // send initial empty AppendEntriesRPC (heartbeat)
        this.sendHeartbeat();

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
        this.log.appendEntry(logEntry, this.commitIndex);

        // send AppendEntriesRPC in parallel to all other servers to replicate the entry
        for(AppendEntriesCall appendEntriesCall : this.appendEntriesCalls) {
            appendEntriesCall.callAppendEntries(this.consensusPersistentState.getCurrentTerm(), this.commitIndex);
        }

        // when at least half of the servers have appended the entry into the log, execute command in the state machine
        int indexToCommit = lastLogIndex + 1;
        StateMachineResult stateMachineResult = null;

        while (this.lastApplied < indexToCommit) {
            try {
                // wait until one of the followers reply to the RPC
                ExecuteCommandDirective directive = this.indexesToCommit.waitForFollowerReplies(indexToCommit);
                // TODO: CONTINUA
                if (directive.equals(ExecuteCommandDirective.PROCEED)) {
                    // UPDATE COMMIT INDEX
                    if (this.appendEntriesCalls.stream().map(AppendEntriesCall::getMatchIndex).filter(index -> index >= indexToCommit).count()
                            >= this.appendEntriesCalls.size()/2 + 1) {
                        this.commitIndex = indexToCommit;
                    }
                    // APPLY ENTRIES COMMITTED
                    stateMachineResult = this.applyCommittedEntries();

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

    private void startHeartbeatTimer() {
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
        for(AppendEntriesCall appendEntriesCall : this.appendEntriesCalls) {
            appendEntriesCall.callAppendEntries(this.consensusPersistentState.getCurrentTerm(), this.commitIndex);
        }
    }

    private StateMachineResult applyCommittedEntries() throws IOException {
        try {
            // if commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
            StateMachineResult stateMachineResult = null;
            while (this.lastApplied < this.commitIndex) {
                this.lastApplied++;
                stateMachineResult = this.stateMachine.executeCommand(this.log.getEntryCommand(this.lastApplied));
            }
            return stateMachineResult;
        } catch(SnapshottedEntryException e) {
            // should never happen, because uncommitted entries cannot be snapshotted
            this.logger.log(Level.SEVERE, "Uncommitted entry is snapshotted");
            e.printStackTrace();
            Server.shutDown();
        }
        return null;
    }

    private Follower toFollower(Integer leaderId) {
        Follower follower = new Follower(this.id, this.configuration, this.log, this.stateMachine, this.container);
        this.container.changeConsensusModuleImpl(follower);
        this.configuration.setLeader(leaderId);
        this.indexesToCommit.notifyAllToInterrupt();
        return follower;
    }

}
