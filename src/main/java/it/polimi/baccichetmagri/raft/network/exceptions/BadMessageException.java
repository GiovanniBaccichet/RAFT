package it.polimi.baccichetmagri.raft.network.exceptions;

public class BadMessageException extends Exception {
    public BadMessageException(String message) {
        super(message);
    }
}
