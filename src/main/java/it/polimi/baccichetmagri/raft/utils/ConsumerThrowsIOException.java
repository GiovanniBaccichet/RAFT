package it.polimi.baccichetmagri.raft.utils;

import java.io.IOException;

public interface ConsumerThrowsIOException<T> {
    void accept(T t) throws IOException;
}
