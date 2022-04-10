package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public abstract class Message {

    protected final MessageId messageId;

    public Message(MessageId messageId) {
        this.messageId = messageId;
    }

    public MessageId getMessageId() {
        return this.messageId;
    }

    public abstract void execute(ConsensusModuleProxy consensusModuleProxy);
}
