package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.machine.Result;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class ExecuteCommandResult extends Message{
    private Result result;

    public ExecuteCommandResult(Result result, int senderId) {
        super(MessageId.ExecuteCommandResult);
        this.result = result;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }
}
