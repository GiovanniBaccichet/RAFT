package it.polimi.baccichetmagri.raft.consensusmodule.leader;

class ConvertToFollowerException extends Exception{

    private final int term;
    ConvertToFollowerException(int term) {
        super();
        this.term = term;
    }

    int getTerm() {
        return this.term;
    }
}
