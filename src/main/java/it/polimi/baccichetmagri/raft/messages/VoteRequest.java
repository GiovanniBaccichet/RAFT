package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;

public class VoteRequest extends Message{

    private final int term;
    private final int candidateId;
    private final int lastLogIndex;
    private final int lastLogTerm;

    public VoteRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        super(MessageType.VoteRequest);
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public int getTerm() {
        return term;
    }

    public int getCandidateId() {
        return candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException {

    }
}
