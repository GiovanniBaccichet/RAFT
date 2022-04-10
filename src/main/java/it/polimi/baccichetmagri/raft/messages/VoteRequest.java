package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class VoteRequest extends Message{

    private int term;
    private int candidateId;
    private int lastLogIndex;
    private int lastLogTerm;

    public VoteRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        super(MessageId.VoteRequest);
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }
}
