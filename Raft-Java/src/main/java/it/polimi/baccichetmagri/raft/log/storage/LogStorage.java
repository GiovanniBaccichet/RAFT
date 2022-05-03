package it.polimi.baccichetmagri.raft.log.storage;

import it.polimi.baccichetmagri.raft.log.LogEntry;

import java.util.List;
import java.util.Optional;

public interface LogStorage {

    void add(LogEntry logEntry);

    void truncate(int fromIndex);

    default boolean hasEntry(int index) {
        return size() >= index;
    }

    LogEntry getEntry(int index);

    List<LogEntry> getEntries();

    List<LogEntry> getEntries(int fromIndexInclusive, int toIndexExclusive);

    default int getLastIndex() {
        return size();
    }

    default int getNextLogIndex() {
        return size() + 1;
    }

    default Optional<Integer> getLastLogTerm() {
        return isEmpty() ?
                Optional.empty()
                : Optional.of(getEntry(getLastIndex()).getTerm());
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    int size();

}
