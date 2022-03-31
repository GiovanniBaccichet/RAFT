package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.messages.VoteResult;

import java.net.Socket;

public class ConsensusModuleProxy implements ConsensusModuleInterface {

    private int id;
    private String ip;
    private Socket socket;

    public ConsensusModuleProxy(int id, String ip) {
        this.id = id;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {
        return null;
    }
}
