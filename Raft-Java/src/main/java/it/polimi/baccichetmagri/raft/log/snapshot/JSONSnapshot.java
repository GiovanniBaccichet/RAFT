package it.polimi.baccichetmagri.raft.log.snapshot;

import it.polimi.baccichetmagri.raft.machine.State;

public class JSONSnapshot {

    private final State state;

    private final int lastIncludedIndex;

    private final int lastIncludedTerm;

    public JSONSnapshot(State state, int lastIncludedIndex, int lastIncludedTerm) {
        this.state = state;
        this.lastIncludedIndex = lastIncludedIndex;
        this.lastIncludedTerm = lastIncludedTerm;
    }

    public State getState() {
        return state;
    }

    public int getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    public int getLastIncludedTerm() {
        return lastIncludedTerm;
    }
}
