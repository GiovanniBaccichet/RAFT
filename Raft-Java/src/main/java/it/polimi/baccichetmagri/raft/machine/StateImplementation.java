package it.polimi.baccichetmagri.raft.machine;

public class StateImplementation extends State {

    private int number;

    public StateMachineResultImplementation getNumber() {
        return new StateMachineResultImplementation(this.number);
    }

    public void addNumber(int numberToAdd) {
        this.number += numberToAdd;
    }
}
