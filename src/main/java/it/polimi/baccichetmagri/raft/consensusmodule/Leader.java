package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

import java.util.Map;

class Leader extends ConsensusModuleImpl {

    private Map<Integer, Integer> nextIndex; // for each server, index of the next log entry to send to that server (initialized to leader last log index + 1)
    private Map<Integer, Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server (initialized to 0, increases monotonically)

    Leader(int id, Configuration configuration, Log log, StateMachine stateMachine) {
        super(id, configuration, log, stateMachine);
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

}
