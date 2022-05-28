package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsensusModuleProxy implements ConsensusModuleInterface, Runnable {

    private final int id;
    private final String ip;
    private Socket socket;

    private final MessageSerializer messageSerializer;

    private boolean isRunning;

    private final ConsensusModule consensusModule;

    private final BlockingQueue<VoteReply> voteResultsQueueMsg;
    private final BlockingQueue<AppendEntryReply> appendEntryResultsQueue;

    private int nextVoteRequestId;
    private int nextAppendEntryRequestId;

    private boolean discardAppendEntryReplies;
    private boolean discardVoteReplies;

    public ConsensusModuleProxy(int id, String ip, ConsensusModule consensusModule) {
        this.id = id;
        this.ip = ip;
        this.socket = null;
        this.messageSerializer = new MessageSerializer();
        this.isRunning = false;
        this.consensusModule = consensusModule;
        this.voteResultsQueueMsg = new LinkedBlockingQueue<>();
        this.appendEntryResultsQueue = new LinkedBlockingQueue<>();
        this.nextVoteRequestId = 0;
        this.nextAppendEntryRequestId = 0;
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
        return this.id;
    }

    public String getIp() {
        return this.ip;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
        if (!this.isRunning) {
            (new Thread(this)).start();
            this.isRunning = true;
        }
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
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException, InterruptedException {
        VoteReply voteResultMsg = null;
            int voteRequestId = this.nextVoteRequestId;
            this.nextVoteRequestId++;
            VoteRequest voteRequest = new VoteRequest(term, candidateID, lastLogIndex, lastLogTerm, voteRequestId);
            this.sendMessage(voteRequest);

            while(voteResultMsg == null) {
                voteResultMsg = this.voteResultsQueueMsg.take();
                if (voteResultMsg.getMessageId() != voteRequestId) {
                    voteResultMsg = null;
                }
            }
        return voteResultMsg.getVoteResult();
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
        AppendEntryReply appendEntryResult = null;
        try {
            int appendEntryRequestId = this.nextAppendEntryRequestId;
            this.nextAppendEntryRequestId++;
            AppendEntryRequest appendEntryRequest = new AppendEntryRequest(term, leaderID, prevLogIndex, prevLogTerm,
                    logEntries, leaderCommit, appendEntryRequestId);
            this.sendMessage(appendEntryRequest);
            while (appendEntryResult == null) {
                appendEntryResult = this.appendEntryResultsQueue.take();
                if (appendEntryResult.getMessageId() != appendEntryRequestId) {
                    appendEntryResult = null;
                }
            }

        } catch (InterruptedException e) {
            // TODO: if the thread has been interrupted while waiting
        }
        return appendEntryResult.getAppendEntryResult();
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        return null;
    }

    /**
     * Calls ConsensusModuleImpl::requestVote and sends the result to the peer.
     * @param term
     * @param candidateID
     * @param lastLogIndex
     * @param lastLogTerm
     * @throws IOException
     */
    public void callRequestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm, int requestId) throws IOException {
        VoteResult voteResult = null;
        try {
            voteResult = this.consensusModule.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        } catch (IOException e) {
            // TODO
        }

        this.sendMessage(new VoteReply(voteResult, requestId));
    }

    /**
     * Calls ConsensusModuleImpl::appendEntries and sends the result to the peer.
     * @param term
     * @param leaderID
     * @param prevLogIndex
     * @param prevLogTerm
     * @param logEntries
     * @param leaderCommit
     * @throws IOException
     */
    public void callAppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries,
                                  int leaderCommit, int requestId) throws IOException {
        AppendEntryResult appendEntryResult = null;
        try {
            this.consensusModule.appendEntries(term, leaderID, prevLogIndex, prevLogTerm,
                    logEntries, leaderCommit);
        } catch (IOException e) {
            // If an exception has occurred during the execution of the method, send appendEntryResult to notify this event

        }

        this.sendMessage(new AppendEntryReply(appendEntryResult, requestId));
    }

    public void receiveVoteReply(VoteReply voteReply) {
        if (!this.discardVoteReplies) {
            this.voteResultsQueueMsg.add(voteReply);
        }
    }

    public void receiveAppendEntriesReply(AppendEntryReply appendEntryReply) {
        if (!this.discardAppendEntryReplies) {
            this.appendEntryResultsQueue.add(appendEntryReply);
        }
    }

    public void discardAppendEntryReplies(boolean discard) {
        this.discardAppendEntryReplies = discard;
    }

    public void discardVoteReplies(boolean discard) {
        this.discardVoteReplies = discard;
    }

    private void sendMessage(Message message) throws IOException {
        this.checkSocket();
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        out.println(this.messageSerializer.serialize(message));
    }

    private Message readMessage() throws IOException, BadMessageException {
        this.checkSocket();
        Scanner in = new Scanner(this.socket.getInputStream());
        String jsonMessage = in.nextLine();
        return this.messageSerializer.deserialiaze(jsonMessage);
    }

    private void checkSocket() throws IOException {
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        if (!this.isRunning) {
            this.setSocket(new Socket(this.ip, ServerSocketManager.PORT));
            out.println("SERVER " + this.consensusModule.getId());
        }
    }
}
