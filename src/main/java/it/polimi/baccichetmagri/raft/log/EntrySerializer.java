package it.polimi.baccichetmagri.raft.log;

import java.io.*;

public class EntrySerializer {

    private EntrySerializer(){

    }

    public static byte[] serialize(LogEntry entry) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(entry);
        return baos.toByteArray();
    }

    public static LogEntry deserialize(byte[] entryBytes) throws IOException {
        try  {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(entryBytes));
            return (LogEntry) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Error deserializing entry", ex);
        }
    }

}
