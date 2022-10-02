package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;

public class InstallSnapshotRequest extends Message{
    private final int term;
    private final int leaderId;
    private final int lastIncludedIndex;
    private final int lastIncludedTerm;
    private final int offset;
    private final byte[] data;
    private final boolean done;

    public InstallSnapshotRequest(int term, int leaderId, int lastIncludedIndex, int lastIncludedTerm,
                                  int offset, byte[] data, boolean done) {
        super(MessageType.InstallSnapshotRequest);
        this.term = term;
        this.leaderId = leaderId;
        this.lastIncludedIndex = lastIncludedIndex;
        this.lastIncludedTerm = lastIncludedTerm;
        this.offset = offset;
        this.data = data;
        this.done = done;
    }

    public int getTerm() {
        return term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public int getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    public int getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    public int getOffset() {
        return offset;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isDone() {
        return done;
    }

}
