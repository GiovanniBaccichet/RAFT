package it.polimi.baccichetmagri.raft.machine;

public abstract class StateMachine {

    State state;

    StateMachine(State state) {
        this.state = state;
    }

    public StateMachineResult executeCommand(Command command) {
        return this.state.applyCommand(command);
    }

    public State getState() {
        return this.state;
    }

    public void resetState(State state) {
        this.state = state;
    }
    
}
