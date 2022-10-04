package it.polimi.baccichetmagri.raft.consensusmodule.container;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsensusModuleContainerImpl extends ConsensusModuleContainer {

    private ConsensusModule consensusModule;
    private Logger logger;

    public ConsensusModuleContainerImpl(int id, Configuration configuration, Log log, StateMachine stateMachine) {
        try {
            this.consensusModule = new Follower(id, new ConsensusPersistentState(), 0, 0, configuration, log, stateMachine, this);
            this.consensusModule.initialize();
            this.logger = Logger.getLogger(ConsensusModuleContainer.class.getName());
            this.logger.setLevel(Level.FINE);
        } catch (IOException e) {
            this.handleIOException(e);
        }
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        try {
            this.logger.log(Level.FINE, "Calling " + this.consensusModule + "::requestVote(term = " + term + ",\ncandidateId = " +
                    candidateID + ",\nlastLogIndex = " + lastLogIndex + ",\nlastLogTerm = " + lastLogTerm + ")");
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Requesting vote in term: " + term + " for Candidate: " + candidateID);
            return this.consensusModule.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) {
        try {
            this.logger.log(Level.FINE, "Calling " + this.consensusModule + "::appendEntries(term = " + term + ",\nleaderID = " +
                    leaderID + ",\nprevLogIndex = " + prevLogIndex + ",\nprevLogTerm = " + prevLogTerm + ",\nlogEntries = "
                    + logEntries + "\nleaderCommit = " + leaderCommit + ")");
            return this.consensusModule.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        try {
            this.logger.log(Level.FINE, "Calling " + this.consensusModule + "::executeCommand(command = " + command + ")");
            return this.consensusModule.executeCommand(command);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return null;
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) {
        try {
            this.logger.log(Level.FINE, "Calling " + this.consensusModule + "::installSnapshot(term = "
                    + term + ",\nleaderID = " + leaderID + ",\nlastIncludedIndex = " + lastIncludedIndex + ",\nlastIncludedTerm = " +
                    lastIncludedTerm + ",\noffset = " + offset + ",\ndone = " + done + ")");
            return this.consensusModule.installSnapshot(term, leaderID, lastIncludedIndex, lastIncludedTerm, offset, data, done);
        } catch (IOException e) {
            this.handleIOException(e);
        }
        return -1;
    }

    public int getId() {
        return this.consensusModule.getId();
    }

    public void changeConsensusModuleImpl(ConsensusModule consensusModule) {
        try {
            this.logger.log(Level.FINE, "Changing consensus module from " + this.consensusModule + " to " + consensusModule);
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
