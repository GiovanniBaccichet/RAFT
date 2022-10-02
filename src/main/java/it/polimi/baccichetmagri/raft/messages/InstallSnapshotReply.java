package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;

public class InstallSnapshotReply extends Message{
    private final int term;

    public InstallSnapshotReply( int term) {
        super(MessageType.InstallSnapshotReply);
        this.term = term;
    }


    public int getTerm() {
        return term;
    }
}
