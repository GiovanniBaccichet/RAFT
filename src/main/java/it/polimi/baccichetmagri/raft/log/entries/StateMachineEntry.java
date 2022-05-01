package it.polimi.baccichetmagri.raft.log.entries;

import it.polimi.baccichetmagri.raft.log.Term;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.util.Arrays;

public class StateMachineEntry extends LogEntry {

    private final int clientID;

    private final Command command;

    public StateMachineEntry(Term term, int clientID, Command command) {
        super(term);
        this.clientID = clientID;
        this.command = command;
    }

    public int getClientID() {
        return clientID;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "StateMachineEntry{" +
                "clientID=" + clientID +
                ", command=" + command +
                '}';
    }

}
