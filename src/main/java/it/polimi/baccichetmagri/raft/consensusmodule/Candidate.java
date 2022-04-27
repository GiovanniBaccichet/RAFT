package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

public class Candidate extends ConsensusModuleImpl {

    Candidate(int id, Configuration configuration, Log log, StateMachine stateMachine,
              ConsensusModule container) {
        super(id, configuration, log, stateMachine, container);
    }

    @Override
    synchronized void initialize() {
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(false);
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        return null;
    }

}
