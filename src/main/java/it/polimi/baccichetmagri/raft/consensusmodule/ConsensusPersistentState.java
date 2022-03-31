package it.polimi.baccichetmagri.raft.consensusmodule;

public class ConsensusPersistentState {

    // currentTerm: latest term server has seen (initialized to 0 on first boot, increases monotonically)

    // votedFor: candidateId that received vote in current term (or null if none)

    public int getCurrentTerm() {
        return 0;
    }

    public void setCurrentTerm(int currentTerm) {

    }

    public Integer getVotedFor() {
        return 0;
    }

    public void setVotedFor(Integer votedFor) {

    }
}
