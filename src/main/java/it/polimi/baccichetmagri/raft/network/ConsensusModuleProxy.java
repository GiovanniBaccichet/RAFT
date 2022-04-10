package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.VoteResult;

import java.net.Socket;

public class ConsensusModuleProxy implements ConsensusModuleInterface, Runnable {

    private int id;
    private String ip;
    private Socket socket;
    private MessageSerializer messageSerializer;
    private boolean isRunning;

    public ConsensusModuleProxy(int id, String ip) {
        this.id = id;
        this.ip = ip;
        this.messageSerializer = messageSerializer;
        this.isRunning = false;
    }

    public void run() {

    }

    public int getId() {
        return id;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        if (!isRunning) {
            (new Thread(this)).start();
            this.isRunning = true;
        }
    }

    public void setSocket(Socket socket, Message message) {
        this.setSocket(socket);

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
