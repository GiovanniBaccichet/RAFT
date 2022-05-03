package it.polimi.baccichetmagri.raft.log;

public interface AppendedEntryHandler {

    void appendedEntry(int index, LogEntry logEntry);

}
