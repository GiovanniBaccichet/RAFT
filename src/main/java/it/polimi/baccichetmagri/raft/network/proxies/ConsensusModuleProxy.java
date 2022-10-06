package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.messages.*;

import java.io.IOException;
import java.util.List;

/**
 * A proxy for consensus modules of other servers in the cluster.
 */
public class ConsensusModuleProxy implements ConsensusModuleInterface {

    private final String ip;
    private final int id;

    private final OutboundRPCCallHandler<AppendEntryRequest, AppendEntryReply> appendEntryRPCHandler;
    private final OutboundRPCCallHandler<VoteRequest, VoteReply> voteRequestRPCHandler;
    private final OutboundRPCCallHandler<InstallSnapshotRequest, InstallSnapshotReply> installSnapshotRPCHandler;

    public ConsensusModuleProxy(int id, String ip, ConsensusModuleContainer consensusModuleContainer) {
        this.ip = ip;
        this.id = id;
        this.appendEntryRPCHandler = new OutboundRPCCallHandler<>(ip, consensusModuleContainer.getId());
        this.voteRequestRPCHandler = new OutboundRPCCallHandler<>(ip, consensusModuleContainer.getId());
        this.installSnapshotRPCHandler = new OutboundRPCCallHandler<>(ip, consensusModuleContainer.getId());
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

}
