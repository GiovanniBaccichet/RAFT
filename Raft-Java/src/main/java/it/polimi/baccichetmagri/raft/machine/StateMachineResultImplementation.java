package it.polimi.baccichetmagri.raft.machine;

public class StateMachineResultImplementation extends StateMachineResult {

    private int number;

    public StateMachineResultImplementation(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
