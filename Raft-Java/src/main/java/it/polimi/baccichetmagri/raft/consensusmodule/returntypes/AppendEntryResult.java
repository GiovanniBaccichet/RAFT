package it.polimi.baccichetmagri.raft.consensusmodule.returntypes;

public class AppendEntryResult {

    private final int term; // currentTerm, for leader to update itself
    private final boolean success; // true if follower contained entry matching prevLogIndex and prevLogTerm

    public AppendEntryResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public boolean isSuccess() {
        return success;
    }
}
