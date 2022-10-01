package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A proxy for consensus modules of other servers in the cluster.
 */
public class ConsensusModuleProxy implements ConsensusModuleInterface, Runnable {


    private final int id;
    private final String ip;
    private Socket socket;

    private final MessageSerializer messageSerializer;
    private final Logger logger;

    private boolean isRunning; // true if a socket is open and communicating with the remote server

    private final ConsensusModuleContainer consensusModuleContainer; // the local consensus module

    private final RPCCallHandler<AppendEntryRequest, AppendEntryReply> appendEntryRPCHandler;
    private final RPCCallHandler<VoteRequest, VoteReply> voteRequestRPCHandler;
    private final RPCCallHandler<InstallSnapshotRequest, InstallSnapshotReply> installSnapshotRPCHandler;

    public ConsensusModuleProxy(int id, String ip, ConsensusModuleContainer consensusModuleContainer) {
        this.id = id;
        this.ip = ip;
        this.socket = null;
        this.messageSerializer = new MessageSerializer();
        this.logger = Logger.getLogger(ConsensusModuleProxy.class.getName());
        this.logger.setLevel(Level.FINE);
        this.isRunning = false;
        this.consensusModuleContainer = consensusModuleContainer;
        this.appendEntryRPCHandler = new RPCCallHandler<>();
        this.voteRequestRPCHandler = new RPCCallHandler<>();
        this.installSnapshotRPCHandler = new RPCCallHandler<>();
    }

    /**
     * Listens for messages on the socket and processes them.
     */
    public void run() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Running socket");
        try {
            while(true) {
                try {
                    Message message = this.readMessage();
                    System.out.println("[" + this.getClass().getSimpleName() + "] " + "Received msg from: " + message);
                    message.execute(this);
                } catch (BadMessageException e) {
                    this.logger.log(Level.WARNING, e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            this.logger.log(Level.WARNING, "Error in network communication. Socket will be closed.");
            e.printStackTrace();
            try {
                this.socket.close();
            } catch (IOException ex) {
                this.logger.log(Level.WARNING, "Impossible to close the socket.");
                ex.printStackTrace();
            }
            this.isRunning = false;
        }
    }

    /**
     * Returns the id of the server represented by the proxy.
     * @return The id of the server represented by the proxy.
     */
    public int getId() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Proxy ID: " + this.id);
        return this.id;
    }

    /**
     * Returns the ip address of the server represented by the proxy.
     * @return The ip address of the server represented by the proxy.
     */
    public String getIp() {
        return this.ip;
    }

    public void setSocket(Socket socket) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Socket listening");
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
     * @return the result of the call
     */
    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        try {
            VoteReply voteReply = this.voteRequestRPCHandler.makeCall(new VoteRequest(term, candidateID, lastLogIndex, lastLogTerm),
                    this::sendMessage);
            return voteReply.getVoteResult();
        } catch (InterruptedException e) {
//            return null;
            return new VoteResult(term, false);
        }
    }

    /**
     * Calls appendEntries on the remote peer and returns the result.
     * @param term leader’s term
     * @param leaderID so follower can redirect clients
     * @param prevLogIndex index of log entry immediately preceding new ones
     * @param prevLogTerm term of prevLogIndex entry
     * @param logEntries log entries to store (empty for heartbeat; may send more than one for efficiency)
     * @param leaderCommit leader’s commitIndex
     * @return the result of the call
     */
    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) throws IOException {
        try {
            AppendEntryReply appendEntryReply = this.appendEntryRPCHandler.makeCall(new AppendEntryRequest(term, leaderID, prevLogIndex, prevLogTerm,
                    logEntries, leaderCommit), this::sendMessage);
            return appendEntryReply.getAppendEntryResult();
        } catch (InterruptedException e) {
            return null;
        }

    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        return null;
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException{
        try {
            InstallSnapshotReply installSnapshotReply = this.installSnapshotRPCHandler.makeCall(new InstallSnapshotRequest(term, leaderID, lastIncludedIndex,
                    lastIncludedTerm, offset, data, done), this::sendMessage);
            return installSnapshotReply.getTerm();
        } catch (InterruptedException e) {
            return 0;
        }
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
            voteResult = this.consensusModuleContainer.requestVote(term, candidateID, lastLogIndex, lastLogTerm);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
    public void callAppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries,
                                  int leaderCommit, int requestId) throws IOException {
        AppendEntryResult appendEntryResult = null;
        try {
            appendEntryResult = this.consensusModuleContainer.appendEntries(term, leaderID, prevLogIndex, prevLogTerm,
                    logEntries, leaderCommit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.sendMessage(new AppendEntryReply(appendEntryResult, requestId));
    }

    public void callInstallSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data,
                                    boolean done, int requestId) throws IOException {
        int currentTerm = this.consensusModuleContainer.installSnapshot(term, leaderID, lastIncludedIndex, lastIncludedTerm, offset, data, done);
        this.sendMessage(new InstallSnapshotReply(requestId, currentTerm));
    }

    public void receiveVoteReply(VoteReply voteReply) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "📬 Received vote reply");
        this.voteRequestRPCHandler.receiveReply(voteReply);
    }

    public void receiveAppendEntriesReply(AppendEntryReply appendEntryReply) {
        this.appendEntryRPCHandler.receiveReply(appendEntryReply);
    }

    public void receiveInstallSnapshotReply(InstallSnapshotReply installSnapshotReply) {
        this.installSnapshotRPCHandler.receiveReply(installSnapshotReply);
    }

    public void discardAppendEntryReplies(boolean discard) {
        this.appendEntryRPCHandler.setDiscardReplies(discard);
    }

    public void discardVoteReplies(boolean discard) {
        this.voteRequestRPCHandler.setDiscardReplies(discard);
    }

    public void discardInstallSnapshotReplies(boolean discard) {
        this.installSnapshotRPCHandler.setDiscardReplies(discard);
    }

    private void sendMessage(Message message) throws IOException {
        this.checkSocket();
        PrintWriter out = new PrintWriter(this.socket.getOutputStream());
        String jsonMessage = this.messageSerializer.serialize(message);
        out.println(jsonMessage);
        this.logger.log(Level.FINE, "Sent message to server + " + this.id + ":\n" + jsonMessage);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending message to: " + this.ip);
        System.out.println("[✉️]: " + jsonMessage);
    }

    private Message readMessage() throws IOException, BadMessageException { // TODO CHECK THIS
        this.checkSocket();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "📬 Received message from: " + this.id);
        String jsonMessage = in.readLine();
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "[✉️]: " + jsonMessage);
        return this.messageSerializer.deserialize(jsonMessage);
    }

    private void checkSocket() throws IOException { // TODO: BROKEN
        if (!this.isRunning) {
            try {
                this.setSocket(new Socket(this.ip, ServerSocketManager.RAFT_PORT));
            } catch (Exception e) {
                e.printStackTrace();
            }
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            try {
//                out.println("SERVER" + this.consensusModuleContainer.getId());
                out.println("SERVER 1");
            } catch (Exception e) {
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "ERROR sending the message to " + this.ip);
                e.printStackTrace();
            }
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Server IP: " + this.ip);
        }
    }
}
