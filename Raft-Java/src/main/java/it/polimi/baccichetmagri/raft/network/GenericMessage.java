package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.MessageType;

public class GenericMessage extends Message {

    public GenericMessage(MessageType messageType) {
        super(messageType);
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }


}
