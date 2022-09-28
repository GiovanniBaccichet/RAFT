package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class AppendEntryReply extends Message{

    private final AppendEntryResult appendEntryResult;

    public AppendEntryReply(AppendEntryResult appendEntryResult, int messageId) {
        super(MessageType.AppendEntryReply, messageId);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append Entry Reply ID: " + messageId);
        this.appendEntryResult = appendEntryResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        consensusModuleProxy.receiveAppendEntriesReply(this);
    }

    public AppendEntryResult getAppendEntryResult() {
        return this.appendEntryResult;
    }
}
