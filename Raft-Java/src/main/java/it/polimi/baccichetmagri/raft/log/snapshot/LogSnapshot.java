package it.polimi.baccichetmagri.raft.log.snapshot;

import it.polimi.baccichetmagri.raft.machine.State;
import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Comparator;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.SYNC;

public class LogSnapshot implements Comparable<LogSnapshot> {

    private final static String SNAPSHOT_PATH = "snapshot";

    private final int lastIncludedIndex;

    private final int lastIncludedTerm;

    private final State state;

    public LogSnapshot(int lastIncludedIndex, int lastIncludedTerm, State state) throws IOException {
        this.lastIncludedIndex = lastIncludedIndex;
        this.lastIncludedTerm = lastIncludedTerm;
        this.state = state;
    }

    // GETTERS

    public int lastIncludedIndex() {
        return lastIncludedIndex;
    }

    public int lastIncludedTerm() {
        return lastIncludedTerm;
    }

    // PUBLIC METHODS

    /**
     * Write snapshot in a JSON fashion to a file
     * @throws IOException
     */
    public void writeSnapshot() throws IOException {
        JsonFilesHandler.write(SNAPSHOT_PATH, new JSONSnapshot(this.state, this.lastIncludedIndex, this.lastIncludedTerm));
    }

    // OVERRIDE

    /**
     * Used to compare 2 NON-empty LogSnapshots
     * @param otherLogSnapshot the Log Snapshot to be compared.
     * @return +1 if the compared Log is more up to date wrt the comparing one, -1 in the other case and 0 if the Logs are identical
     */
    @Override
    public int compareTo(LogSnapshot otherLogSnapshot) {
        return Comparator.comparing(ls -> ((LogSnapshot) ls).lastIncludedTerm).thenComparing(ls -> ((LogSnapshot
        ) ls).lastIncludedIndex).compare(this, otherLogSnapshot);
    }

}
