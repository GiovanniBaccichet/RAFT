package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;

public class VoteRequest extends Message{

    private final int term;
    private final int candidateId;
    private final int lastLogIndex;
    private final int lastLogTerm;

    public VoteRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm, int messageId) {
        super(MessageType.VoteRequest, messageId);
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException {
        consensusModuleProxy.callRequestVote(this.term, this.candidateId, this.lastLogIndex, this.lastLogTerm, this.getMessageId());
    }
}
