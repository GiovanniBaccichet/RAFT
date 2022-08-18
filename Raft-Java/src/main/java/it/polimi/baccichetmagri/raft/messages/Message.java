package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;
import it.polimi.baccichetmagri.raft.network.MessageSerializer;

import java.io.IOException;

public abstract class Message {

    private final MessageType messageType;
    private int messageId;

    public Message(MessageType messageType) {
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return this.messageType;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public abstract void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException;

}
