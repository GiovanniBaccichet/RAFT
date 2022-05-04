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

import java.io.IOException;

public class ConsensusModule  implements ConsensusModuleInterface {

    private ConsensusModuleImpl consensusModuleImpl;

    public ConsensusModule(int id, Configuration configuration, Log log, StateMachine stateMachine) {
        this.consensusModuleImpl = new Follower(id, configuration, log, stateMachine, this);
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        return this.consensusModuleImpl.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) throws IOException {
        return this.consensusModuleImpl.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        return this.consensusModuleImpl.executeCommand(command);
    }

    public int getId() {
        return this.consensusModuleImpl.getId();
    }

    void changeConsensusModuleImpl(ConsensusModuleImpl consensusModuleImpl) {
        this.consensusModuleImpl = consensusModuleImpl;
        this.consensusModuleImpl.initialize();
    }
}
