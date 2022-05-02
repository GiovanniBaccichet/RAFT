package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.machine.Command;

// log[]: log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
public class Log {

    public void appendEntry(LogEntry logEntry) {

    }

    public Command getEntryCommand(int index) {
        return null; // TODO cambiare
    }

    public boolean containsEntry(int index, int term) {
        return false; // TODO cambiare
    }

    // If there are no entry w/ the input index, it returns -1
    public int getEntryTerm(int index) {
        return -1; // TODO cambiare
    }

    // Deletes all Entries from a certain index, following
    public void deleteEntriesFrom(int index) {

    }

    // Get last entry's index from the log
    public int getLastIndex() {
        return 1; // TODO da cambiare
    }

}
