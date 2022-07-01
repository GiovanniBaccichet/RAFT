package it.polimi.baccichetmagri.raft.machine;

public class StateMachineImplementation extends StateMachine {

    private StateImplementation state = new StateImplementation();
    @Override
    public StateMachineResult executeCommand(Command command) {
        this.state.addNumber(((CommandImplementation) command).getNumberToAdd());
        return this.state.getNumber();
    }

    @Override
    public State getState() {
        return null;
    }
}
