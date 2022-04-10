package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class ExecuteCommandRequest extends Message{

    private Command command;

    public ExecuteCommandRequest(Command command) { // called by clients
        super(MessageId.ExecuteCommandRequest);
        this.command = command;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }

}
