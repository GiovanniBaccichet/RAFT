package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;

public abstract class Message {

    private final MessageType messageType;
    private final int messageId;

    public Message(MessageType messageType, int messageId) {
        this.messageType = messageType;
        this.messageId = messageId;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public abstract void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException;
}
