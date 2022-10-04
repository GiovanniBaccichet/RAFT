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
public class ConsensusModuleProxy implements ConsensusModuleInterface {

    private final String ip;
    private final int id;

    private final MessageSerializer messageSerializer;
    private final Logger logger;
    private final ConsensusModuleContainer consensusModuleContainer; // the local consensus module

    private final RPCCallHandler<AppendEntryRequest, AppendEntryReply> appendEntryRPCHandler;
    private final RPCCallHandler<VoteRequest, VoteReply> voteRequestRPCHandler;
    private final RPCCallHandler<InstallSnapshotRequest, InstallSnapshotReply> installSnapshotRPCHandler;

    public ConsensusModuleProxy(int id, String ip, ConsensusModuleContainer consensusModuleContainer) {
        this.ip = ip;
        this.id = id;
        this.messageSerializer = new MessageSerializer();
        this.logger = Logger.getLogger(ConsensusModuleProxy.class.getName());
        this.logger.setLevel(Level.FINE);
        this.consensusModuleContainer = consensusModuleContainer;
        this.appendEntryRPCHandler = new RPCCallHandler<>(ip, consensusModuleContainer.getId());
        this.voteRequestRPCHandler = new RPCCallHandler<>(ip, consensusModuleContainer.getId());
        this.installSnapshotRPCHandler = new RPCCallHandler<>(ip, consensusModuleContainer.getId());
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
            VoteReply voteReply = this.voteRequestRPCHandler.makeCall(new VoteRequest(term, candidateID, lastLogIndex, lastLogTerm));
            return voteReply.getVoteResult();
        } catch (InterruptedException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Request vote method call to server " + this.id + " interrupted");
            return null;
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
                    logEntries, leaderCommit));
            return appendEntryReply.getAppendEntryResult();
        } catch (InterruptedException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append entries method call to server " + this.id + " INTERRUPTED");
            return null;
        }

    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Method executeCommand should not be called");
        return null;
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException{
        try {
            InstallSnapshotReply installSnapshotReply = this.installSnapshotRPCHandler.makeCall(new InstallSnapshotRequest(term, leaderID, lastIncludedIndex,
                    lastIncludedTerm, offset, data, done));
            return installSnapshotReply.getTerm();
        } catch (InterruptedException e) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Install snapsnot method call to server " + this.id + " INTERRUPTED");
            return 0;
        }
    }

    public void receiveMethodCall(Socket socket) {
        try {
            Message request = this.readMessage(socket);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "MessageType: " + request.getMessageType());
            Message reply = null;
            switch (request.getMessageType()) {
                case AppendEntryRequest -> reply = callAppendEntries((AppendEntryRequest) request);
                case VoteRequest -> reply = callRequestVote((VoteRequest) request);
                case InstallSnapshotRequest -> reply = callInstallSnapshot((InstallSnapshotRequest) request);
                default -> System.out.println("[" + this.getClass().getSimpleName() + "]" + "Invalid message received ");
            }
            if (reply != null) {
                this.sendMessage(reply, socket);
            }
            socket.close();
        } catch (BadMessageException e) {
            this.logger.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getIp() {
        return this.ip;
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

    /**
     * Calls ConsensusModuleImpl::requestVote and sends the result to the peer.

     * @throws IOException
     */
    private VoteReply callRequestVote(VoteRequest voteRequest) throws IOException {
        VoteResult voteResult = null;
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Calling requestVote");
        try {
            voteResult = this.consensusModuleContainer.requestVote(voteRequest.getTerm(), voteRequest.getCandidateId(),
                    voteRequest.getLastLogIndex(), voteRequest.getLastLogTerm());
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Vote Result: " + voteResult.isVoteGranted() + " in term " + voteResult.getTerm());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new VoteReply(voteResult);
    }

    /**
     * Calls ConsensusModuleImpl::appendEntries and sends the result to the peer.
     * @throws IOException
     */
    private AppendEntryReply callAppendEntries(AppendEntryRequest appendEntryRequest) throws IOException {
        AppendEntryResult appendEntryResult = null;
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Calling AppendEntries");
        try {
            appendEntryResult = this.consensusModuleContainer.appendEntries(appendEntryRequest.getTerm(), appendEntryRequest.getLeaderId(),
                    appendEntryRequest.getPrevLogIndex(), appendEntryRequest.getPrevLogTerm(), appendEntryRequest.getLogEntries(),
                    appendEntryRequest.getLeaderCommit());
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append Entry: " + appendEntryResult.isSuccess() + " in term " + appendEntryResult.getTerm());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AppendEntryReply(appendEntryResult);
    }

    private InstallSnapshotReply callInstallSnapshot(InstallSnapshotRequest installSnapshotRequest) throws IOException {
        int currentTerm = this.consensusModuleContainer.installSnapshot(installSnapshotRequest.getTerm(), installSnapshotRequest.getLeaderId(),
                installSnapshotRequest.getLastIncludedIndex(), installSnapshotRequest.getLastIncludedTerm(), installSnapshotRequest.getOffset(),
                installSnapshotRequest.getData(), installSnapshotRequest.isDone());
        return new InstallSnapshotReply(currentTerm);
    }

    private void sendMessage(Message message, Socket socket) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        String jsonMessage = this.messageSerializer.serialize(message);
        out.println(jsonMessage);
        out.flush();
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending message to: " + this.id);
        System.out.println("[✉️]: " + jsonMessage);
    }

    private Message readMessage(Socket socket) throws IOException, BadMessageException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String jsonMessage = in.readLine();
        System.out.println("\u001B[45m" + "\u001B[37m" + "[" + this.getClass().getSimpleName() + "] " + "Received message from: " + this.id + " message: " + jsonMessage + "\u001B[0m");
        return this.messageSerializer.deserialize(jsonMessage);
    }


}
