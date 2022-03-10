package it.polimi.baccichetmagri.raft;

public abstract class StateMachine {

    public abstract void executeCommand(Command command);
    
}
