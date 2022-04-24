package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.messages.VoteResultMsg;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;

import java.util.List;

public abstract class ConsensusModule implements ConsensusModuleInterface {

    protected int id;
    protected ConsensusPersistentState consensusPersistentState; // currentTerm, votedFor
    protected int commitIndex; // index of highest log entry known to be committed (initialized to 0, increases monotonically)
    protected int lastApplied; // index of highest log entry applied to state machine (initialized to 0, increases monotonically)
    protected List<Integer> servers; // IDs of other servers
    protected Log log;
    protected StateMachine stateMachine;

    protected ServerSocketManager networkHandler;


    public synchronized VoteResult requestVote(int term,
                                               int candidateID,
                                               int lastLogIndex,
                                               int lastLogTerm) {

        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        //  Reply false if term < currentTerm
        if (term < currentTerm) {
            return new VoteResult(currentTerm, false);
        }

        this.updateTerm(currentTerm);

        //  If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote
        Integer votedFor = this.consensusPersistentState.getVotedFor();
        int lastIndex = this.log.getLastIndex();
        if ((votedFor == null || votedFor == candidateID) && (lastIndex <= lastLogIndex && this.log.getEntryTerm(lastIndex) <= lastLogTerm)) {
            this.consensusPersistentState.setVotedFor(candidateID);
            return new VoteResult(currentTerm, true);
        }

        return new VoteResult(currentTerm, false);
    }


    public abstract AppendEntryResult appendEntries(int term,
                                                    int leaderID,
                                                    int prevLogIndex,
                                                    int prevLogTerm,
                                                    LogEntry[] logEntries,
                                                    int leaderCommit);

    /**
     * If commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
     */
    protected void checkCommitIndex() {
        while (this.commitIndex > this.lastApplied) {
            this.lastApplied++;
            this.stateMachine.executeCommand(this.log.getEntryCommand(lastApplied));
        }
    }

    // If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower
    protected void updateTerm(int term) {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
            // TODO Convert to follower
        }
    }


}
