package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class VoteReply extends Message{

    private final VoteResult voteResult;

    public VoteReply(VoteResult voteResult) {
        super(MessageType.VoteReply);
        this.voteResult = voteResult;
    }

    public VoteResult getVoteResult() {
        return voteResult;
    }
}
