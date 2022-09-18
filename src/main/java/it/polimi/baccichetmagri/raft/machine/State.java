package it.polimi.baccichetmagri.raft.machine;

public abstract class State {

    public abstract StateMachineResult applyCommand (Command command);

}
