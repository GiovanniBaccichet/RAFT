package it.polimi.raft;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RaftServant extends Remote {

    public int voteRequest (int candidateID, int candidateTerm, int lastIndex, int lastTerm)
        throws RemoteException;

    public int appendEntry (int leaderID, int leaderTerm, int prevIndex, int prevTerm, Entry[] entries, int leaderCommit)
        throws RemoteException;

}
