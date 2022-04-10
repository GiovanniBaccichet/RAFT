package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.MessageId;

public class GenericMessage extends Message {

    public GenericMessage(MessageId messageId) {
        super(messageId);
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }


}
