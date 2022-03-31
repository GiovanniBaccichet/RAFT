package it.polimi.baccichetmagri.raft.messages;

public class VoteRequest extends Message{

    private int term;
    private int candidateId;
    private int lastLogIndex;
    private int lastLogTerm;



}
