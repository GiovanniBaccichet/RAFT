package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

public class InboundRPCCallHandler {
    private Socket socket;
    private ConsensusModuleInterface consensusModule;
    private MessageSerializer messageSerializer;

    public InboundRPCCallHandler(Socket socket, ConsensusModuleInterface consensusModule) {
        this.socket = socket;
        this.consensusModule = consensusModule;
        this.messageSerializer = new MessageSerializer();
    }

    public void receiveMethodCall() {
        try {
            Message request = this.readMessage();
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
        } catch (BadMessageException | IOException e) {
            e.printStackTrace();
        }
    }

    private Message readMessage() throws IOException, BadMessageException {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        String jsonMessage = in.readLine();
        System.out.println("\u001B[45m" + "\u001B[37m" + "[" + this.getClass().getSimpleName() + "] " + "Received message: " + jsonMessage + "\u001B[0m");
        return this.messageSerializer.deserialize(jsonMessage);
    }

    private void sendMessage(Message message, Socket socket) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        String jsonMessage = this.messageSerializer.serialize(message);
        out.println(jsonMessage);
        out.flush();
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending message : " + jsonMessage);
    }

    /**
     * Calls ConsensusModuleImpl::requestVote and sends the result to the peer.

     * @throws IOException
     */
    private VoteReply callRequestVote(VoteRequest voteRequest) throws IOException {
        VoteResult voteResult = null;
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Calling requestVote");
        try {
            voteResult = this.consensusModule.requestVote(voteRequest.getTerm(), voteRequest.getCandidateId(),
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
            appendEntryResult = this.consensusModule.appendEntries(appendEntryRequest.getTerm(), appendEntryRequest.getLeaderId(),
                    appendEntryRequest.getPrevLogIndex(), appendEntryRequest.getPrevLogTerm(), appendEntryRequest.getLogEntries(),
                    appendEntryRequest.getLeaderCommit());
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append Entry: " + appendEntryResult.isSuccess() + " in term " + appendEntryResult.getTerm());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new AppendEntryReply(appendEntryResult);
    }

    private InstallSnapshotReply callInstallSnapshot(InstallSnapshotRequest installSnapshotRequest) throws IOException {
        int currentTerm = this.consensusModule.installSnapshot(installSnapshotRequest.getTerm(), installSnapshotRequest.getLeaderId(),
                installSnapshotRequest.getLastIncludedIndex(), installSnapshotRequest.getLastIncludedTerm(), installSnapshotRequest.getOffset(),
                installSnapshotRequest.getData(), installSnapshotRequest.isDone());
        return new InstallSnapshotReply(currentTerm);
    }
}
