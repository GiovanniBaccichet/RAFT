package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.CommandImplementation;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class ExecuteCommandRequest extends Message{

    private CommandImplementation command;

    public ExecuteCommandRequest(CommandImplementation command) { // called by clients
        super(MessageType.ExecuteCommandRequest);
        this.command = command;
    }


    public Command getCommand() {
        return this.command;
    }
}
