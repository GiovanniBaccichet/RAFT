package it.polimi.baccichetmagri.raft.consensusmodule;

class ConsensusPersistentState {

    // currentTerm: latest term server has seen (initialized to 0 on first boot, increases monotonically)

    // votedFor: candidateId that received vote in current term (or null if none)

    int getCurrentTerm() {
        return 0;
    }

    void setCurrentTerm(int currentTerm) {

    }

    Integer getVotedFor() {
        return 0;
    }

    void setVotedFor(Integer votedFor) {

    }
}
