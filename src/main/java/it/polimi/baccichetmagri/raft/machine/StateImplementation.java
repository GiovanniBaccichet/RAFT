package it.polimi.baccichetmagri.raft.machine;

public class StateImplementation extends State {

    private int number = 0;


    @Override
    public StateMachineResult applyCommand(Command command) {
        CommandImplementation commandImplementation = (CommandImplementation) command;
        this.number += commandImplementation.getNumberToAdd();
        return new StateMachineResultImplementation(this.number);
    }

    public int getNumber() { // for testing purposes
        return number;
    }
}
