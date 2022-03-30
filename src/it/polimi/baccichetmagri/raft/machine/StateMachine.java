package it.polimi.baccichetmagri.raft.machine;

import it.polimi.baccichetmagri.raft.machine.Command;

public abstract class StateMachine {

    public abstract void executeCommand(Command command);
    
}
