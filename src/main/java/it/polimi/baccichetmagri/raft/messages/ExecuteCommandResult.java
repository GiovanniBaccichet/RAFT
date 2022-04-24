package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.machine.StateMachineResult;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class ExecuteCommandResult extends Message{
    private StateMachineResult stateMachineResult;

    public ExecuteCommandResult(StateMachineResult stateMachineResult, int messageId) {
        super(MessageType.ExecuteCommandResult, messageId);
        this.stateMachineResult = stateMachineResult;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        // this message can't be sent by a server, so this method does nothing
    }

    public StateMachineResult getStateMachineResult() {
        return stateMachineResult;
    }
}
