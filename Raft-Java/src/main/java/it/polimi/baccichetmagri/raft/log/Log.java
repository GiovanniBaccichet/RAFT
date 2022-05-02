package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.log.storage.LogStorage;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// log[]: log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
public class Log {

    private final ReadWriteLock readWriteLock; // Multi-threaded inputs (different sockets) require locking

    private final List<LogEntry> logEntries; // Term + command

    private final LogStorage storage;

    private int index; // Index is required to guarantee Log Matching Property (along w/ term)

    public Log(LogStorage logStorage) {
        readWriteLock = new ReentrantReadWriteLock();
        storage = logStorage;
        index = 0;
        logEntries = new ArrayList<>();
    }

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
