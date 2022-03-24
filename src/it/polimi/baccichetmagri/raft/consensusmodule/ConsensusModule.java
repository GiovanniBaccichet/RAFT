package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.messages.VoteResult;
import it.polimi.baccichetmagri.raft.network.NetworkHandler;

import java.util.List;

public abstract class ConsensusModule {

    protected int id;
    protected ConsensusPersistentState consensusPersistentState; // currentTerm, votedFor
    protected int commitIndex; // index of highest log entry known to be committed (initialized to 0, increases monotonically)
    protected int lastApplied; // index of highest log entry applied to state machine (initialized to 0, increases monotonically)
    protected List<Integer> servers; // IDs of other servers
    protected Log log;
    protected StateMachine stateMachine;

    protected NetworkHandler networkHandler;

    /**
     * Invoked by candidates to gather votes
     * @param term candidate’s term
     * @param cadidateID candidate requesting vote
     * @param lastLogIndex index of candidate’s last log entry
     * @param lastLogTerm term of candidate’s last log entry
     * @return a VoteResult containing the current term and a boolean, true if candidate received vote
     */
    public VoteResult requestVote(int term,
                                  int cadidateID,
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
        if ((votedFor == null || votedFor == cadidateID) && (lastIndex <= lastLogIndex && this.log.getEntryTerm(lastIndex) <= lastLogTerm)) {
            this.consensusPersistentState.setVotedFor(cadidateID);
            return new VoteResult(currentTerm, true);
        }

        return new VoteResult(currentTerm, false);
    }

    /**
     * Invoked by leader to replicate log entries; also used as heartbeat
     * @param term leader’s term
     * @param leaderID so follower can redirect clients
     * @param prevLogIndex index of log entry immediately preceding new ones
     * @param prevLogTerm term of prevLogIndex entry
     * @param logEntries log entries to store (empty for heartbeat; may send more than one for efficiency)
     * @param leaderCommit leader’s commitIndex
     * @return an AppendEntryResult containing the current term and a boolean, true if follower contained entry matching prevLogIndex and prevLogTerm
     */
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
