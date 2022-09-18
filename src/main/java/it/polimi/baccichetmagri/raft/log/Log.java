package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.log.snapshot.JSONSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.LogSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.util.Collections.unmodifiableList;

// log[]: log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
public class Log {

    public static String LOG_FILENAME = "files/log";

    /**
     * Number of entries in the Log after which compress it: modify in production
     */
    private static final int SNAPSHOT_LIMIT = 10;

    private final FileChannel fileChannel;

    private List<Long> entryEndIndex;

    private final StateMachine stateMachine;

    private final LogSnapshot snapshot;

    private final Logger logger;

    public Log(Path logFilePath, StateMachine stateMachine) throws IOException {
        this.fileChannel = FileChannel.open(logFilePath, READ, WRITE, CREATE, SYNC);
        this.stateMachine = stateMachine;
        this.snapshot = new LogSnapshot();
        this.logger = Logger.getLogger(Log.class.getName());
        this.logger.setLevel(Level.FINE);
        this.reIndex();
    }

    /**
     * Close file channel
     * @throws IOException
     */
    public synchronized void close() throws IOException {
        this.fileChannel.close();
    }

    public synchronized int size() throws IOException {
        return entryEndIndex.size() + this.snapshot.getLastIncludedIndex() - 1;
    }

    /**
     * Checks if an entry exists and where it is (log or snapshot.json)
     * @param index index of the entry to check in the Log
     * @param term term of the entry to check in the Log
     * @return an ENUM, depending on the position of the Log Entry (if it exists)
     * @throws IOException
     */
    public synchronized LogEntryStatus containsEntry(int index, int term) throws IOException {
        if (index == 0 && term == 0) { // fictitious entry with index 0
            return LogEntryStatus.NOT_SNAPSHOTTED;
        }
        this.validateIndex(index);
        if (index <= snapshot.getLastIncludedIndex() && term <= snapshot.getLastIncludedTerm()) {
            return LogEntryStatus.SNAPSHOTTED;
        } else {
            if (index <= getLastLogIndex() && term == getLastLogTerm()) {
                return LogEntryStatus.NOT_SNAPSHOTTED;
            } else {
                return LogEntryStatus.NOT_EXISTENT;
            }
        }
    }


    /**
     * Appends a LogEntry to the Log
     * @param entry LogEntry to append
     * @param commitIndex parameter needed to avoid snapshotting uncommitted entries
     * @throws IOException
     */
    public synchronized void appendEntry(LogEntry entry, int commitIndex) throws IOException {
        byte[] entryBytes = EntrySerializer.serialize(entry);
        ByteBuffer byteBuffer = ByteBuffer.allocate(entryBytes.length + 4);
        byteBuffer.putInt(entryBytes.length);
        byteBuffer.put(entryBytes);
        byteBuffer.position(0);
        fileChannel.write(byteBuffer);
        entryEndIndex.add(fileChannel.position());

        this.logger.log(Level.FINE, "Appended entry at index " + this.getLastLogIndex() + ": " + entry);

        if (this.size() > SNAPSHOT_LIMIT && commitIndex > this.snapshot.getLastIncludedIndex()) {
            new Thread(() -> this.createSnapshot(commitIndex));
        }

    }

    /**
     * Used to delete all Entries from a certain index, following
     * @param fromIndex index from which deleting the others
     * @throws IOException
     */
    public synchronized void deleteEntriesFrom(int fromIndex) throws IOException {
        if (fromIndex <= snapshot.getLastIncludedIndex()) {
            /*
             * If fromIndex is related to an Entry included in a snapshot,
             * the deletion starts from the end of that snapshot.
             * This situation should never happen in the real implementation of the algorithm,
             * since only uncommitted entries can be deleted.
             */
            fromIndex = snapshot.getLastIncludedIndex() + 1;
        }
        fileChannel.truncate(entryEndIndex.get(fromIndex - snapshot.getLastIncludedIndex() - 1));
        entryEndIndex = entryEndIndex.subList(0, fromIndex);

        this.logger.log(Level.FINE, "Deleted entries from index" + fromIndex);
    }

    // GETTERS

    /**
     * Used to get the entries written in the log file and NOT snapshotted (useful to print to terminal)
     * @return a List of LogEntries
     * @throws IOException
     */
    public synchronized List<LogEntry> getEntries() throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            entries.add(readEntry(i + 1));
        }
        return unmodifiableList(entries);
    }

    /**
     * Retrieve LogEntry form log file (given that it has not been snapshotted), given the index
     * @param index requested index
     * @return LogEntry, having requested index
     */
    public synchronized LogEntry getEntry(int index) throws IOException, SnapshottedEntryException {
        validateIndex(index);
        if (index <= this.snapshot.getLastIncludedIndex()) {
            throw new SnapshottedEntryException();
        }
        return this.readEntry(index-snapshot.getLastIncludedIndex());
    }

    /**
     * Get the entries written in the log file (specified range)
     * @param fromIndexInclusive lower bound (included) index
     * @param toIndexExclusive upper bound (NOT included) index
     * @return a list of LogEntries, having index in the requested range
     */
    public synchronized List<LogEntry> getEntries(int fromIndexInclusive, int toIndexExclusive) throws IOException, SnapshottedEntryException {
        List<LogEntry> entries = new ArrayList<>();
        if (fromIndexInclusive > toIndexExclusive) {
            throw new IllegalArgumentException();
        }
        if (fromIndexInclusive <= this.snapshot.getLastIncludedIndex()) {
            throw new SnapshottedEntryException();
        }
        fromIndexInclusive -= this.snapshot.getLastIncludedIndex();
        toIndexExclusive -= this.snapshot.getLastIncludedIndex();
        for (int i = fromIndexInclusive; i < toIndexExclusive; i++) {
            entries.add(readEntry(i));
        }
        return unmodifiableList(entries);
    }

    /**
     * Get LogEntry's Command, related to the requested index
     * @param index requested index
     * @return LogEntry's Command, having the requested index
     */
    public synchronized Command getEntryCommand(int index) throws IOException, SnapshottedEntryException {
        validateIndex(index);
        return this.getEntry(index).getCommand();
    }

    /**
     * If there are no entries with requested index, it returns -1
     * @param index requested index
     * @return term related to the requested index
     */
    public synchronized int getEntryTerm(int index) throws IOException, SnapshottedEntryException {
        validateIndex(index);
        return this.getEntry(index).getTerm();
    }

    // Get last entry's index from the log
    public synchronized int getLastLogIndex() throws IOException {
        return this.size();
    }

    public synchronized int getNextLogIndex() throws IOException {
        return this.size() + 1;
    }

    public synchronized int getLastLogTerm() throws IOException {
        try {
            return this.getEntry(getLastLogIndex()).getTerm();
        } catch (SnapshottedEntryException e) {
            return this.snapshot.getLastIncludedTerm();
        }
    }

    public synchronized JSONSnapshot getJSONSnapshot() throws IOException {
        return new JSONSnapshot(this.snapshot.getMachineState(), this.snapshot.getLastIncludedIndex(), this.snapshot.getLastIncludedTerm());
    }

    /**
     * Initializes entryEndIndex from the (persistent) Log content
     * @throws IOException
     */
    public synchronized void reIndex() throws IOException {
            this.entryEndIndex = new ArrayList<>();
            this.entryEndIndex.add(0L);
            this.fileChannel.position(0);
            ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
            while (this.fileChannel.position() < this.fileChannel.size()) {
                lengthBuffer.position(0);
                this.fileChannel.read(lengthBuffer);
                int length = lengthBuffer.getInt(0);
                long endIndex = this.fileChannel.position() + length;
                this.entryEndIndex.add(endIndex);
                this.fileChannel.position(endIndex);
            }
    }

    // SNAPSHOT METHODS

    /**
     * Used to create the Log Snapshot
     * @param commitIndex commit index needed to not snapshot uncommitted entries
     */
    private synchronized void createSnapshot(int commitIndex) {
        try {
            snapshot.writeSnapshot(this.stateMachine.getState(), commitIndex, this.getLastLogTerm());
            List<LogEntry> uncommittedEntries = this.getEntries(commitIndex+1, this.getLastLogIndex()+1);
            deleteEntriesFrom(1);
            for (LogEntry entry : uncommittedEntries) {
                this.appendEntry(entry, commitIndex);
            }
        } catch (IOException e) {
            Logger logger = Logger.getLogger(Log.class.getName());
            logger.log(Level.WARNING, "Impossible to create snapshot.json");
            e.printStackTrace();
        } catch (SnapshottedEntryException e) {
            // Should never happen (check done previously)
        }
    }

    /**
     * Check if illegal indexes are present in the log
     * @param index index to check
     */
    private void validateIndex(int index) {
        if (index < 1) {
            throw new IllegalArgumentException("[ERROR] Indices start at 1");
        }
    }

    private long startPositionOfEntry(int index) {
        return entryEndIndex.get(index - 1);
    }

    private int lengthOfEntry(int index) {
        return (int) (entryEndIndex.get(index) - startPositionOfEntry(index) - 4);
    }

    private LogEntry readEntry(int index) throws IOException {
            long offset = startPositionOfEntry(index);
            int length = lengthOfEntry(index);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer, offset + 4);
            return EntrySerializer.deserialize(buffer.array());
    }


}
