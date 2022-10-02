package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;

public abstract class Message {

    private final MessageType messageType;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public Message(MessageType messageType, int messageId) {
        this(messageType);
    }

    public MessageType getMessageType() {
        return this.messageType;
    }


    public abstract void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException;

}
