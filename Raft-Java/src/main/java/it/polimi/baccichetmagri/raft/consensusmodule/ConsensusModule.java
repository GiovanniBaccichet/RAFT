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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsensusModule  implements ConsensusModuleInterface {

    private ConsensusModuleImpl consensusModuleImpl;
    private final Logger logger;

    public ConsensusModule(int id, Configuration configuration, Log log, StateMachine stateMachine) {
        this.consensusModuleImpl = new Follower(id, configuration, log, stateMachine, this);
        this.logger = Logger.getLogger(ConsensusModule.class.getName());
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        try {
            return this.consensusModuleImpl.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {
        try {
            return this.consensusModuleImpl.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        try {
            return this.consensusModuleImpl.executeCommand(command);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    public int getId() {
        return this.consensusModuleImpl.getId();
    }

    void changeConsensusModuleImpl(ConsensusModuleImpl consensusModuleImpl) {
        this.consensusModuleImpl = consensusModuleImpl;
        this.consensusModuleImpl.initialize();
    }

    private void handleIOException(IOException e) {
        this.logger.log(Level.SEVERE, "An IO error has occurred in the access to persistent storage. The program is " +
                "going to be terminated.");
        e.printStackTrace();
        System.exit(1);
    }
}
