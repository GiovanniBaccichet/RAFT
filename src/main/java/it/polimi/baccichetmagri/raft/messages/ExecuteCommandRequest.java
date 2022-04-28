package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class ExecuteCommandRequest extends Message{

    private Command command;

    public ExecuteCommandRequest(Command command, int messageId) { // called by clients
        super(MessageType.ExecuteCommandRequest, messageId);
        this.command = command;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {
        // this message can't be sent by a server, so this method does nothing
    }

    public Command getCommand() {
        return this.command;
    }
}
