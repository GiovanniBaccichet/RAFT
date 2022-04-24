package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.machine.Command;

public abstract class LogEntry {

    private int term;
    private Command command;

    @Override
    public String toString() {

        return null; // TODO cambiare
    }
}
