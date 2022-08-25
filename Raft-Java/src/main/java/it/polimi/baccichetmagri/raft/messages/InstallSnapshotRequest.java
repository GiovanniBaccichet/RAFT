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

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException {
        consensusModuleProxy.callInstallSnapshot(this.term, this.leaderId, this.lastIncludedIndex, this.lastIncludedTerm,
                this.offset, this.data, this.done, this.getMessageId());
    }
}
