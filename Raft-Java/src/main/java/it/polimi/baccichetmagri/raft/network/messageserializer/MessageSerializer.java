package it.polimi.baccichetmagri.raft.network.messageserializer;

import com.google.gson.Gson;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

public class MessageSerializer {

    private final Gson gson = new Gson();

    public String serialize(Message message) {
        return this.gson.toJson(message);
    }

    public Message deserialiaze(String jsonMessage) throws BadMessageException {
        if (jsonMessage == null) {
            throw new NullPointerException();
        }

        GenericMessage message = this.gson.fromJson(jsonMessage, GenericMessage.class);
        switch (message.getMessageType()) {
            case AppendEntryRequest:
                return this.gson.fromJson(jsonMessage, AppendEntryRequest.class);
            case AppendEntryReply:
                return this.gson.fromJson(jsonMessage, AppendEntryReply.class);
            case ExecuteCommandRequest:
                return this.gson.fromJson(jsonMessage, ExecuteCommandRequest.class);
            case ExecuteCommandReply:
                return this.gson.fromJson(jsonMessage, ExecuteCommandReply.class);
            case VoteRequest:
                return this.gson.fromJson(jsonMessage, VoteRequest.class);
            case VoteReply:
                return this.gson.fromJson(jsonMessage, VoteReply.class);
            default:
                throw new BadMessageException("invalid message: " + jsonMessage);
        }
    }
}
