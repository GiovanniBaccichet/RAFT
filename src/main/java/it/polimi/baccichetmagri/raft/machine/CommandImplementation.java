package it.polimi.baccichetmagri.raft.machine;

import java.io.Serializable;

public class CommandImplementation extends Command implements Serializable{

    private final int numberToAdd;

    public CommandImplementation(int numberToSet) {
        this.numberToAdd = numberToSet;
    }

    public int getNumberToAdd() {
        return numberToAdd;
    }

    @Override
    public String toString() {
        return "numberToAdd = " + numberToAdd;
    }
}
