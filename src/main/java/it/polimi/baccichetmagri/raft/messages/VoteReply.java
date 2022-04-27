package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class VoteReply extends Message{

    private final VoteResult voteResult;

    public VoteReply(VoteResult voteResult, int messageId) {
        super(MessageType.VoteResult, messageId);
        this.voteResult = voteResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        consensusModuleProxy.receiveVoteResult(this);
    }

    public VoteResult getVoteResult() {
        return voteResult;
    }
}
