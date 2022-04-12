package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class VoteResult extends Message{

    private final int term;
    private final boolean voteGranted;

    public VoteResult(int term, boolean voteGranted, int messageId) {
        super(MessageType.VoteResult, messageId);
        this.term = term;
        this.voteGranted = voteGranted;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        consensusModuleProxy.receiveVoteResult(this);
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

}
