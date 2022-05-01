package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.entries.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

abstract class ConsensusModuleImpl implements ConsensusModuleInterface {

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

    ConsensusModuleImpl(int id, Configuration configuration, Log log, StateMachine stateMachine,
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

    abstract void initialize();

    @Override
    public abstract VoteResult requestVote(int term,
                                               int candidateID,
                                               int lastLogIndex,
                                               int lastLogTerm);

    @Override
    public abstract AppendEntryResult appendEntries(int term,
                                                    int leaderID,
                                                    int prevLogIndex,
                                                    int prevLogTerm,
                                                    LogEntry[] logEntries,
                                                    int leaderCommit);

    @Override
    public abstract ExecuteCommandResult executeCommand(Command command);

    int getId() {
        return this.id;
    }

    /**
     * If commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
     */
    void checkCommitIndex() {
        while (this.commitIndex > this.lastApplied) {
            this.lastApplied++;
            this.stateMachine.executeCommand(this.log.getEntryCommand(lastApplied));
        }
    }

    // If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower
    void updateTerm(int term) {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
            // TODO Convert to follower
        }
    }


}