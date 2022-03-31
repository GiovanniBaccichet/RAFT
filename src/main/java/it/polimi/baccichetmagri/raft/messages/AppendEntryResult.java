package it.polimi.baccichetmagri.raft.messages;

public class AppendEntryResult extends Message{

    private int term; // currentTerm, for leader to update itself
    private boolean success; // true if follower contained entry matching prevLogIndex and prevLogTerm

    public AppendEntryResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
