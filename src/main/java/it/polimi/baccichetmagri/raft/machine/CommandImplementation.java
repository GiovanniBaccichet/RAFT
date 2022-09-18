package it.polimi.baccichetmagri.raft.machine;

public class CommandImplementation extends Command{

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
