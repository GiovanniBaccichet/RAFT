package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class VoteReply extends Message{

    private final VoteResult voteResult;

    public VoteReply(VoteResult voteResult) {
        super(MessageType.VoteReply);
        this.voteResult = voteResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        consensusModuleProxy.receiveVoteReply(this);
    }

    public VoteResult getVoteResult() {
        return voteResult;
    }
}
