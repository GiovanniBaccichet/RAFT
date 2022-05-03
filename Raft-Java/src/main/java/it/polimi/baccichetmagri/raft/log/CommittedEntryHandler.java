package it.polimi.baccichetmagri.raft.log;

public interface CommittedEntryHandler {

    void committedEntry(int index, LogEntry logEntry);

}
