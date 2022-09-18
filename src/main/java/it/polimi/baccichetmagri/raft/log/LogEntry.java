package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.machine.Command;

public class LogEntry {

    private int term;
    private Command command;

    public LogEntry(int term, Command command) {
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
