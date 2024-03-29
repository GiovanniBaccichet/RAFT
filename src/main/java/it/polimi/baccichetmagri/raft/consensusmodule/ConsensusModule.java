package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
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

public abstract class ConsensusModule implements ConsensusModuleInterface {

    public final static int ELECTION_TIMEOUT_MIN = 1500; // milliseconds
    public final static int ELECTION_TIMEOUT_MAX = 3000; // milliseconds

    protected int id;
    protected ConsensusPersistentState consensusPersistentState; // currentTerm, votedFor
    protected int commitIndex; // index of highest log entry known to be committed (initialized to 0, increases monotonically)
    protected int lastApplied; // index of highest log entry applied to state machine (initialized to 0, increases monotonically)
    protected Configuration configuration;
    protected Log log;
    protected StateMachine stateMachine;
    protected ConsensusModuleContainer container;

    public ConsensusModule(int id, ConsensusPersistentState consensusPersistentState, int commitIndex, int lastApplied,
                           Configuration configuration, Log log, StateMachine stateMachine, ConsensusModuleContainer container) {
        this.id = id;
        this.consensusPersistentState = consensusPersistentState;
        try {
            consensusPersistentState.setVotedFor(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.commitIndex = commitIndex;
        this.lastApplied = lastApplied;
        this.configuration = configuration;
        this.log = log;
        this.stateMachine = stateMachine;
        this.container = container;
    }

    public abstract void initialize() throws IOException;

    @Override
    public abstract VoteResult requestVote(int term,
                                               int candidateID,
                                               int lastLogIndex,
                                               int lastLogTerm) throws IOException;

    @Override
    public abstract AppendEntryResult appendEntries(int term,
                                                    int leaderID,
                                                    int prevLogIndex,
                                                    int prevLogTerm,
                                                    List<LogEntry> logEntries,
                                                    int leaderCommit) throws IOException;

    @Override
    public abstract ExecuteCommandResult executeCommand(Command command) throws IOException;

    @Override
    public abstract int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException;

    public int getId() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "ConsensusModule ID: " + this.id);
        return this.id;
    }

}
