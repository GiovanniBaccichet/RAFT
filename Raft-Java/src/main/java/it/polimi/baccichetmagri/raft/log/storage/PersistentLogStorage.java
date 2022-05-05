package it.polimi.baccichetmagri.raft.log.storage;

import it.polimi.baccichetmagri.raft.log.EntrySerializer;
import it.polimi.baccichetmagri.raft.log.LogEntry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.util.Collections.unmodifiableList;

public class PersistentLogStorage {

    private final EntrySerializer entrySerializer;
    private final FileChannel fileChannel;
    private List<Long> entryEndIndex;


    @Override
    public void add(LogEntry entry) {
        writeEntry(entry);
    }

    @Override
    public void truncate(int fromIndex) {
        try {
            fileChannel.truncate(entryEndIndex.get(fromIndex - 1));
            entryEndIndex = entryEndIndex.subList(0, fromIndex);
        } catch (IOException ex) {
            throw new RuntimeException("[ERROR] Could not truncate the Log", ex);
        }
    }


    private long startPositionOfEntry(int index) {
        return entryEndIndex.get(index - 1);
    }

    private int lengthOfEntry(int index) {
        return (int) (entryEndIndex.get(index) - startPositionOfEntry(index) - 4);
    }

    private LogEntry readEntry(int index) {
        try {
            long offset = startPositionOfEntry(index);
            int length = lengthOfEntry(index);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer, offset + 4);
            return entrySerializer.deserialize(buffer.array());
        } catch (IOException ex) {
            throw new RuntimeException("[ERROR] Could not read Log entry", ex);
        }
    }

}
