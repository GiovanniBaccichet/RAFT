package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

import java.io.IOException;

abstract class ConsensusModuleAbstract implements ConsensusModuleInterface {

    protected final static int ELECTION_TIMEOUT_MIN = 150; // milliseconds
    protected final static int ELECTION_TIMEOUT_MAX = 300; // milliseconds

    protected int id;
    protected ConsensusPersistentState consensusPersistentState; // currentTerm, votedFor
    protected int commitIndex; // index of highest log entry known to be committed (initialized to 0, increases monotonically)
    protected int lastApplied; // index of highest log entry applied to state machine (initialized to 0, increases monotonically)
    protected Configuration configuration;
    protected Log log;
    protected StateMachine stateMachine;
    protected ConsensusModule container;

    ConsensusModuleAbstract(int id, Configuration configuration, Log log, StateMachine stateMachine,
                            ConsensusModule container) {
        this.id = id;
        this.consensusPersistentState = new ConsensusPersistentState();
        this.commitIndex = 0;
        this.lastApplied = 0;
        this.configuration = configuration;
        this.log = log;
        this.stateMachine = stateMachine;
        this.container = container;
    }

    abstract void initialize() throws IOException;

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
                                                    LogEntry[] logEntries,
                                                    int leaderCommit) throws IOException;

    @Override
    public abstract ExecuteCommandResult executeCommand(Command command) throws IOException;

    @Override
    public abstract int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done);

    int getId() {
        return this.id;
    }

}
