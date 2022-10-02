package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class ExecuteCommandReply extends Message{
    private final ExecuteCommandResult executeCommandResult;

    public ExecuteCommandReply(ExecuteCommandResult executeCommandResult) {
        super(MessageType.ExecuteCommandReply);
        this.executeCommandResult = executeCommandResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        // this message can't be sent by a server, so this method does nothing
    }

    public ExecuteCommandResult getExecuteCommandResult() {
        return executeCommandResult;
    }
}
