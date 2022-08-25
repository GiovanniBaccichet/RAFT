package it.polimi.baccichetmagri.raft.network.messageserializer;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.MessageType;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

public class GenericMessage extends Message {

    public GenericMessage(MessageType messageType) {
        super(messageType, 0);
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }


}
