package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;

public class InstallSnapshotReply extends Message{
    private final int term;

    public InstallSnapshotReply(int messageId, int term) {
        super(MessageType.InstallSnapshotReply, messageId);
        this.term = term;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException {
        consensusModuleProxy.receiveInstallSnapshotReply(this);
    }

    public int getTerm() {
        return term;
    }
}
