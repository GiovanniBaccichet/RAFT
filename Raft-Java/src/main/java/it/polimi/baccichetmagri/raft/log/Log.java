package it.polimi.baccichetmagri.raft.log;

import it.polimi.baccichetmagri.raft.log.storage.LogStorage;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.util.Collections.unmodifiableList;

// log[]: log entries; each entry contains command for state machine, and term when entry was received by leader (first index is 1)
public class Log {


    private final FileChannel fileChannel;

    private List<Long> entryEndIndex;

    public Log(Path logFilePath) throws IOException {
        this.fileChannel = FileChannel.open(logFilePath, READ, WRITE, CREATE, SYNC);
        this.reIndex();
    }

    // Check if illegal indexes are present in the log
    private void validateIndex(int index) {
        if (index < 1) {
            throw new IllegalArgumentException("[ERROR] Indices start at 1");
        }
    }

    public int size() {
        return entryEndIndex.size() - 1;
    }

    // Check if log file has an entry for a particular index
    public boolean containsEntry(int index, int term) {
        this.validateIndex(index);
        return (this.size() >= index && this.getEntry(index).getTerm() == term);
    }

    private LogEntry readEntry(int index) {
        try {
            long offset = startPositionOfEntry(index);
            int length = lengthOfEntry(index);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer, offset + 4);
            return EntrySerializer.deserialize(buffer.array());
        } catch (IOException ex) {
            throw new RuntimeException("[ERROR] Could not read Log entry", ex);
        }
    }

    private long startPositionOfEntry(int index) {
        return entryEndIndex.get(index - 1);
    }

    private int lengthOfEntry(int index) {
        return (int) (entryEndIndex.get(index) - startPositionOfEntry(index) - 4);
    }

    // Retrieve LogEntry form log file, given the index
    public LogEntry getEntry(int index) {
        validateIndex(index);
        return this.readEntry(index);
    }

    // Get the entries written in the log file
    public List<LogEntry> getEntries() {
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            entries.add(readEntry(i + 1));
        }
        return unmodifiableList(entries);
    }

    public List<LogEntry> getEntries(int fromIndexInclusive, int toIndexExclusive) {
        List<LogEntry> entries = new ArrayList<>();
        for (int i = fromIndexInclusive; i < toIndexExclusive; i++) {
            entries.add(readEntry(i));
        }
        return unmodifiableList(entries);
    }

    private void writeEntry(int appendIndex, LogEntry entry) throws IOException { // TODO implementare Index dentro qui
        byte[] entryBytes = EntrySerializer.serialize(entry);
        ByteBuffer byteBuffer = ByteBuffer.allocate(entryBytes.length + 4);
        byteBuffer.putInt(entryBytes.length);
        byteBuffer.put(entryBytes);
        byteBuffer.position(0);
        fileChannel.write(byteBuffer);
        entryEndIndex.add(fileChannel.position());
    }

    // AppendEntry method (as described in the paper)
    public void appendEntry(int appendIndex, LogEntry logEntry) throws IOException {
        this.writeEntry(logEntry);
    }

    public Command getEntryCommand(int index) {
        validateIndex(index);
        return storage.getEntry(index).getCommand();
    }

    // If there are no entry w/ the input index, it returns -1
    public int getEntryTerm(int index) {
        validateIndex(index);
        return storage.getEntry(index).getTerm();
    }

    // Deletes all Entries from a certain index, following
    public void deleteEntriesFrom(int index) { // TODO finire

    }

    // Get last entry's index from the log
    public int getLastIndex() {
        return storage.getLastIndex();
    }

    public int getNextLogIndex() {
        return storage.getNextLogIndex();
    }

    public Optional<Integer> getLastLogTerm() {
        return storage.getLastLogTerm();
    }

    public void reIndex() throws IOException {
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

}
