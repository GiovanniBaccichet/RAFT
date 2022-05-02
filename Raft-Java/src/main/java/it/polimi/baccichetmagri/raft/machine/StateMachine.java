package it.polimi.baccichetmagri.raft.machine;

public abstract class StateMachine {

    public abstract StateMachineResult executeCommand(Command command);
    
}
