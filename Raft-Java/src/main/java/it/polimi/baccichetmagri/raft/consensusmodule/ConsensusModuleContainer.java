package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsensusModuleContainer implements ConsensusModuleInterface {

    private ConsensusModule consensusModule;
    private Logger logger;

    public ConsensusModuleContainer(int id, Configuration configuration, Log log, StateMachine stateMachine) {
        try {
            this.consensusModule = new Follower(id, configuration, log, stateMachine, this);
            this.consensusModule.initialize();
            this.logger = Logger.getLogger(ConsensusModuleContainer.class.getName());
        } catch (IOException e) {
            this.handleIOException(e);
        }
    }

    @Override
    public synchronized VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        try {
            return this.consensusModule.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) {
        try {
            return this.consensusModule.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        try {
            return this.consensusModule.executeCommand(command);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public synchronized int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) {
        return this.consensusModule.installSnapshot(term, leaderID, lastIncludedIndex, lastIncludedTerm, offset, data, done);
    }

    public synchronized int getId() {
        return this.consensusModule.getId();
    }

    public synchronized void changeConsensusModuleImpl(ConsensusModule consensusModule) {
        try {
            this.consensusModule = consensusModule;
            this.consensusModule.initialize();
        } catch (IOException e) {

        }
    }

    private void handleIOException(IOException e) {
        this.logger.log(Level.SEVERE, "An IO error has occurred in the access to persistent storage. The program is " +
                "going to be terminated.");
        e.printStackTrace();
        Server.shutDown();
    }
}
