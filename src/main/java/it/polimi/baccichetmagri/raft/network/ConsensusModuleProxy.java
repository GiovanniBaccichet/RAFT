package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConsensusModuleProxy implements ConsensusModuleInterface, Runnable {

    private final int id;
    private final String ip;
    private Socket socket;
    private final MessageSerializer messageSerializer;
    private boolean isRunning;
    private ConsensusModule consensusModule;

    public ConsensusModuleProxy(int id, String ip, ConsensusModule consensusModule) {
        this.id = id;
        this.ip = ip;
        this.messageSerializer = new MessageSerializer();
        this.isRunning = false;
        this.consensusModule = consensusModule;
    }

    public void run() {
        try {
            while(true) {
                Message message = this.readMessage();
                message.execute(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                this.socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            this.isRunning = false;
        } catch (BadMessageException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
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

    /**
     * Calls requestVote on the remote peer and returns the result.
     * @param term candidate’s term
     * @param candidateID candidate requesting vote
     * @param lastLogIndex index of candidate’s last log entry
     * @param lastLogTerm term of candidate’s last log entry
     * @return
     */
    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        VoteRequest voteRequest = new VoteRequest(term, candidateID, lastLogIndex, lastLogTerm);
        this.sendMessage(voteRequest);
        return null; // TODO
    }

    /**
     * Calls appendEntries on the remote peer and returns the result.
     * @param term leader’s term
     * @param leaderID so follower can redirect clients
     * @param prevLogIndex index of log entry immediately preceding new ones
     * @param prevLogTerm term of prevLogIndex entry
     * @param logEntries log entries to store (empty for heartbeat; may send more than one for efficiency)
     * @param leaderCommit leader’s commitIndex
     * @return
     */
    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) throws IOException {
        AppendEntryRequest appendEntryRequest = new AppendEntryRequest(term, leaderID, prevLogIndex, prevLogTerm,
                logEntries, leaderCommit);
        this.sendMessage(appendEntryRequest);
        return null; // TODO
    }

    /**
     * Calls ConsensusModule::requestVote and sends the result to the peer.
     * @param term
     * @param candidateID
     * @param lastLogIndex
     * @param lastLogTerm
     * @throws IOException
     */
    public void callRequestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        VoteResult voteResult = this.consensusModule.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        this.sendMessage(voteResult);
    }

    /**
     * Calls ConsensusModule::appendEntries and sends the result to the peer.
     * @param term
     * @param leaderID
     * @param prevLogIndex
     * @param prevLogTerm
     * @param logEntries
     * @param leaderCommit
     * @throws IOException
     */
    public void callAppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) throws IOException {
        AppendEntryResult appendEntryResult = this.consensusModule.appendEntries(term, leaderID, prevLogIndex, prevLogTerm,
                logEntries, leaderCommit);
        this.sendMessage(appendEntryResult);
    }

    public void receiveVoteResult(VoteResult voteResult) {
        // TODO
    }

    public void receiveAppendEntriesResult(AppendEntryResult appendEntryResult) {
        // TODO
    }

    private void sendMessage(Message message) throws IOException {
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(this.messageSerializer.serialize(message));
    }

    private Message readMessage() throws IOException, BadMessageException {
        Scanner in = new Scanner(this.socket.getInputStream());
        String jsonMessage = in.nextLine();
        return this.messageSerializer.deserialiaze(jsonMessage);
    }
}
