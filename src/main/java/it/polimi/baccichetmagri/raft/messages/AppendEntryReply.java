package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class AppendEntryReply extends Message{

    private final AppendEntryResult appendEntryResult;

    public AppendEntryReply(AppendEntryResult appendEntryResult) {
        super(MessageType.AppendEntryReply);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append Entry Reply ID: ");
        this.appendEntryResult = appendEntryResult;
    }


    public AppendEntryResult getAppendEntryResult() {
        return this.appendEntryResult;
    }
}
