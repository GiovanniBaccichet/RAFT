package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class VoteResult extends Message{

    private int term;
    private boolean voteGranted;

    public VoteResult(int term, boolean voteGranted, int senderId) {
        super(MessageId.VoteResult);
        this.term = term;
        this.voteGranted = voteGranted;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }
}
