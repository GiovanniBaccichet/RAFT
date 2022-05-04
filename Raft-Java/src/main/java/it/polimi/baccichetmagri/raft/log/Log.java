package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.log.storage.LogStorage;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

// log[]: log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
public class Log {

    private final ReadWriteLock readWriteLock; // Multi-threaded inputs (different sockets) require locking

    private final List<CommittedEntryHandler> committedEntryHandlers;

    private final List<AppendedEntryHandler> appendedEntryHandlers;

    private final List<LogEntry> logEntries; // Term + command

    private final LogStorage storage;

    private int commitIndex; // Index is required to guarantee Log Matching Property (along w/ term)

    public Log(LogStorage logStorage) {
        readWriteLock = new ReentrantReadWriteLock();
        committedEntryHandlers = new ArrayList<>();
        appendedEntryHandlers = new ArrayList<>();
        storage = logStorage;
        commitIndex = 0;
        logEntries = new ArrayList<>();
    }

    public Optional<Integer> updateCommitIndex(List<Integer> matchIndices, int currentTerm) {
        return doWrite(() -> {
            int clusterSize = matchIndices.size() + 1;
            List<Integer> matchCurrentIndices = matchIndices.stream()
                    .filter(index -> index > commitIndex)
                    .filter(index -> getEntry(index).getTerm() == currentTerm)
                    .sorted()
                    .collect(toList());
            int majorityThreshold = clusterSize / 2;
            if (matchCurrentIndices.size() + 1 > majorityThreshold) {
                Integer newCommitIndex = matchCurrentIndices
                        .get(matchCurrentIndices.size() - majorityThreshold);
                incrementCommitIndex(newCommitIndex);
                return Optional.of(newCommitIndex);
            }
            return Optional.empty();
        });
    }

    public void incrementCommitIndex(int newCommitIndex) {
        doWrite(() -> {
            if (newCommitIndex > this.commitIndex) {
                range(this.commitIndex, newCommitIndex)
                        .map(i -> i + 1)
                        .forEach(this::notifyCommittedListeners);
                this.commitIndex = newCommitIndex;
            }
            return null;
        });
    }

    private void notifyCommittedListeners(int committedIndex) {
        committedEntryHandlers
                .forEach(handler -> handler.committedEntry(committedIndex, getEntry(committedIndex)));
    }

    // Check if illegal indexes are present in the log
    private void validateIndex(int index) {
        if (index < 1) {
            throw new IllegalArgumentException("[ERROR] Indices start at 1");
        }
    }

    // Acquire lock and read log file
    private <V> V doRead(Supplier<V> function) {
        return acquireLockAnd(readWriteLock.readLock(), function);
    }

    // Acquire lock and write log file
    private <V> V doWrite(Supplier<V> function) {
        return acquireLockAnd(readWriteLock.writeLock(), function);
    }

    // Acquire lock on log file
    private <V> V acquireLockAnd(Lock lock, Supplier<V> function) {
        lock.lock();
        try {
            return function.get();
        } finally {
            lock.unlock();
        }
    }

    // Check if log file has an entry for a particular index
    public boolean containsEntry(int index, int term) { // TODO Implementare term
        return doRead(() -> {
            validateIndex(index);
            return storage.hasEntry(index);
        });
    }

    // Retrieve LogEntry form log file, given the index
    public LogEntry getEntry(int index) {
        return doRead(() -> {
            validateIndex(index);
            return storage.getEntry(index);
        });
    }

    // Get the entries written in the log file
    public List<LogEntry> getEntries() {
        return doRead(storage::getEntries);
    }

    // AppendEntry method (as described in the paper)
    public void appendEntry(int appendIndex, LogEntry logEntry) {
        if (containsEntry(appendIndex, logEntry.getTerm())) {
            if (!(getEntry(appendIndex).getTerm() == logEntry.getTerm())) {
                if (appendIndex <= this.commitIndex) {
                    throw new IllegalStateException("[ERROR] Cannot truncate prior to commit index");
                }
                storage.truncate(appendIndex);
                storage.add(logEntry);
                notifyAppendedListeners(appendIndex);
            }
        } else {
            storage.add(logEntry);
            notifyAppendedListeners(appendIndex);
        }
    }

    private void notifyAppendedListeners(int appendedIndex) {
        appendedEntryHandlers
                .forEach(handler -> handler.appendedEntry(appendedIndex, getEntry(appendedIndex)));
    }

    public Command getEntryCommand(int index) {
        return doRead(() -> {
            validateIndex(index);
            return storage.getEntry(index).getCommand();
        });
    }

    // If there are no entry w/ the input index, it returns -1
    public int getEntryTerm(int index) {
        return doRead(() -> {
            validateIndex(index);
            return storage.getEntry(index).getTerm();
        });
    }

    // Deletes all Entries from a certain index, following
    public void deleteEntriesFrom(int index) {

    }

    // Get last entry's index from the log
    public int getLastIndex() {
        return doRead(storage::getLastIndex);
    }

    public int getNextLogIndex() {
        return doRead(storage::getNextLogIndex);
    }

    public Optional<Integer> getLastLogTerm() {
        return doRead(storage::getLastLogTerm);
    }

}
