package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

public class Candidate extends ConsensusModuleImpl {

    Candidate(int id, Configuration configuration, Log log, StateMachine stateMachine) {
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
