package it.polimi.baccichetmagri.raft.consensusmodule;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class Leader extends ConsensusModuleImpl {

    private Map<Integer, Integer> nextIndex; // for each server, index of the next log entry to send to that server (initialized to leader last log index + 1)
    private Map<Integer, Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server (initialized to 0, increases monotonically)

    Leader(int id, Configuration configuration, Log log, StateMachine stateMachine, ConsensusModule consensusModule) {
        super(id, configuration, log, stateMachine, consensusModule);
        this.nextIndex = new HashMap<>();
        this.matchIndex = new HashMap<>();
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        int lastLogIndex = this.log.getLastIndex();
        while (proxies.hasNext()) {
            int proxyId = proxies.next().getId();
            this.nextIndex.put(proxyId, lastLogIndex + 1);
            this.matchIndex.put(proxyId, 0);
        }
    }

    @Override
    synchronized void initialize() {
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardAppendEntryReplies(false);
        int lastLogIndex = this.log.getLastIndex();
        // send initial empty AppendEntriesRPC (heartbeat)
        this.callAppendEntriesOnAllServers(this.consensusPersistentState.getCurrentTerm(), this.id,
                lastLogIndex, this.log.getEntryTerm(lastLogIndex), new LogEntry[0], this.commitIndex);
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
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        // append command to local log as new entry
        this.log.appendEntry(new LogEntry(currentTerm, command));

        // call AppendEntriesRPC in parallel on all other servers

        return null; // TODO cambiare
    }

    private void callAppendEntriesOnAllServers(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

    }
}
