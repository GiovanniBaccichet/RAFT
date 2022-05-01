package it.polimi.baccichetmagri.raft.log.storage;

import it.polimi.baccichetmagri.raft.log.Term;
import it.polimi.baccichetmagri.raft.log.entries.LogEntry;

import java.util.List;
import java.util.Optional;

import static sun.jvm.hotspot.runtime.BasicObjectLock.size; // Not sure about this

public interface LogStorage {

    void addLogEntry(LogEntry logEntry);

    void truncate(int fromIndex);

    default boolean isPopulated(int index){
        return size() >= index;
    }

    LogEntry getEntry(int index);

    List<LogEntry> getEntries();

    List<LogEntry> getEntries(int fromIndexInclusive, int toIndexExclusive);

    default int getLastLogIndex() {
        return size();
    }

    default int getNextLogIndex() {
        return size() + 1;
    }

    default Optional<Term> getLastLogTerm() {
        return isEmpty() ?
                Optional.empty()
                : Optional.of(getEntry(getLastLogIndex()).getTerm());
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    int size();

}
