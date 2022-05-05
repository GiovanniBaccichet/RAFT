package it.polimi.baccichetmagri.raft.log;

import java.io.*;

public class EntrySerializer {

    private EntrySerializer(){

    }

    public static byte[] serialize(LogEntry entry) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(entry);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Error serializing entry", ex);
        }
    }

    public static LogEntry deserialize(byte[] entryBytes) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(entryBytes))) {
            return (LogEntry) ois.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new RuntimeException("Error deserializing entry", ex);
        }
    }

}
