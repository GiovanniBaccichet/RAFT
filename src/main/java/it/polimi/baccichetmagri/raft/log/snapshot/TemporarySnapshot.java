package it.polimi.baccichetmagri.raft.log.snapshot;

import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This class is used when a follower installs a remote snapshot sent it by the leader.
 * The snapshot arrives in chunks, stored in a temporary snapshot, that will be then saved in the actual snapshot file when the sending
 * of the chunks will be completed.
 */
public class TemporarySnapshot {
    private final static String TEMPORARY_SNAPSHOT_PATH = "Log/temporary_snapshot.json";

    public void writeChunk(byte[] chunk, int offset) throws IOException {
        // write the chunk to the file at the given offset
        try (RandomAccessFile file = new RandomAccessFile(TEMPORARY_SNAPSHOT_PATH, "rw")) {
            file.seek(offset);
            file.write(chunk);
        }
    }

    public JSONSnapshot getJsonSnapshot() throws IOException {
        return JsonFilesHandler.read(TEMPORARY_SNAPSHOT_PATH, JSONSnapshot.class);
    }

    public void delete() throws IOException {
        (new FileWriter(TEMPORARY_SNAPSHOT_PATH, false)).close();
    }
}
