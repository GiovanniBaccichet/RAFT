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
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
    }

    @Override
    synchronized void initialize() throws IOException {
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardAppendEntryReplies(false);

        // send initial empty AppendEntriesRPC (heartbeat)
        this.callAppendEntriesOnAllServers(this.consensusPersistentState.getCurrentTerm(), this.id,
                this.log.getLastLogIndex(), this.log.getLastLogTerm(), new LogEntry[0], this.commitIndex);

        this.startHeartbeatTimer();
    }

    @Override
    public synchronized VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) throws IOException {

        this.stopHeartbeatTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastLogIndex();
        // append command to local log as new entry
        LogEntry logEntry = new LogEntry(currentTerm, command);
        LogEntry[] logEntries = {logEntry};
        this.log.appendEntry(new LogEntry(currentTerm, command)); // TODO check se ha senso

        // send AppendEntriesRPC in parallel to all other servers to replicate the entry
        this.callAppendEntriesOnAllServers(currentTerm, this.id, lastLogIndex, this.log.getLastLogTerm(),
                logEntries, this.commitIndex);

        // when at least half of the servers have appended the entry into the log, execute command in the state machine
        int indexToCommit = lastLogIndex + 1;
        StateMachineResult stateMachineResult = null;
        while (this.commitIndex < indexToCommit) {
            try {
                this.wait();
                if (this.commitIndex == indexToCommit) {
                    this.lastApplied = indexToCommit;
                    stateMachineResult = this.stateMachine.executeCommand(this.log.getEntryCommand(indexToCommit));
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

    private void callAppendEntriesOnAllServers(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        while (proxies.hasNext()) {
            ConsensusModuleProxy proxy = proxies.next();
            new Thread(() -> {
                boolean done = false;
                while (!done) {
                    try {
                        AppendEntryResult appendEntryResult = proxy.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
                        if (appendEntryResult.isSuccess()) {
                            // update nextIndex and matchIndex
                            this.nextIndex.put(proxy.getId(), prevLogIndex + logEntries.length + 1);
                            this.matchIndex.put(proxy.getId(), prevLogIndex + logEntries.length);
                            this.updateCommitIndex();
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

    private synchronized void updateCommitIndex() throws IOException {
        // if there exists an N such that N > commitIndex, a majority of matchIndex[i] ≥ N, and log[N].term == currentTerm: set commitIndex = N
        int newCommitIndex = this.commitIndex;
        for (int i = this.commitIndex + 1; i <= this.log.getLastLogIndex(); i++) {
            int finalI = i;
            if (this.matchIndex.values().stream().filter(index -> index >= finalI).count() >= this.matchIndex.size()/2 + 1
                && this.log.getEntryTerm(i) == this.consensusPersistentState.getCurrentTerm()) {
                newCommitIndex = i;
            }
        }
        if (newCommitIndex > this.commitIndex) {
            for (int i = this.commitIndex + 1; i <= newCommitIndex; i++) {

            }
        }
    }

    private void startHeartbeatTimer() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() { // send heartbeat to all servers
                try {
                    callAppendEntriesOnAllServers(consensusPersistentState.getCurrentTerm(), id,
                            log.getLastLogIndex(), log.getLastLogTerm(), new LogEntry[0], commitIndex);
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

}
