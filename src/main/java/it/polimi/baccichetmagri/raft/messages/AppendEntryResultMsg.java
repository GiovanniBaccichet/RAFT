package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class AppendEntryResultMsg extends Message{

    private final AppendEntryResult appendEntryResult;

    public AppendEntryResultMsg(AppendEntryResult appendEntryResult, int messageId) {
        super(MessageType.AppendEntryResult, messageId);
        this.appendEntryResult = appendEntryResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        consensusModuleProxy.receiveAppendEntriesResult(this);
    }

    public AppendEntryResult getAppendEntryResult() {
        return this.appendEntryResult;
    }
}
