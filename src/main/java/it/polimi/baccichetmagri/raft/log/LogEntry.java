package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.CommandImplementation;

import java.io.Serializable;

public class LogEntry implements Serializable {

    private int term;
    private CommandImplementation command;

    public LogEntry(int term, CommandImplementation command) {
        this.term = term;
        this.command = command;
    }

    public int getTerm() {
        return term;
    }

    public Command getCommand(){
        return command;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                ", term=" + term +
                ", command=" + command +
                '}';
    }
}
