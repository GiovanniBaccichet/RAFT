package it.polimi.baccichetmagri.raft.machine;

public class StateMachineResultImplementation extends StateMachineResult {

    private final int number;

    public StateMachineResultImplementation(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "" + this.number;
    }
}
