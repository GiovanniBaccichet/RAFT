package it.polimi.baccichetmagri.raft.consensusmodule.returntypes;

public class VoteResult {

    private final int term;
    private final boolean voteGranted;

    public VoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }
}
