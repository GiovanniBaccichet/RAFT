package it.polimi.baccichetmagri.raft.log.snapshot;

import it.polimi.baccichetmagri.raft.machine.State;
import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Comparator;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.SYNC;

public class LogSnapshot {

    private final static String SNAPSHOT_PATH = "snapshot.json";

    public State getMachineState() throws IOException {
        return readSnapshot().getState();
    }

    public int getLastIncludedIndex() throws IOException {
        return readSnapshot().getLastIncludedIndex();
    }

    public int getLastIncludedTerm() throws IOException {
        return readSnapshot().getLastIncludedTerm();
    }

    /**
     * Write snapshot.json in a JSON fashion to a file
     * @throws IOException
     */
    public void writeSnapshot(State state, int lastIncludedIndex, int lastIncludedTerm) throws IOException {
        JsonFilesHandler.write(SNAPSHOT_PATH, new JSONSnapshot(state, lastIncludedIndex, lastIncludedTerm));
    }

    private JSONSnapshot readSnapshot() throws IOException {
        return JsonFilesHandler.read(SNAPSHOT_PATH, JSONSnapshot.class);
    }

}
