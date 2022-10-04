package it.polimi.baccichetmagri.raft.network.messageserializer;

import com.google.gson.Gson;
import it.polimi.baccichetmagri.raft.messages.*;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

public class MessageSerializer {

    private final Gson gson = new Gson();

    public String serialize(Message message) {
        return this.gson.toJson(message);
    }

    public Message deserialize(String jsonMessage) throws BadMessageException {
        if (jsonMessage == null) {
            throw new NullPointerException();
        }

        GenericMessage message = this.gson.fromJson(jsonMessage, GenericMessage.class);
        return switch (message.getMessageType()) {
            case AppendEntryRequest -> this.gson.fromJson(jsonMessage, AppendEntryRequest.class);
            case AppendEntryReply -> this.gson.fromJson(jsonMessage, AppendEntryReply.class);
            case ExecuteCommandRequest -> this.gson.fromJson(jsonMessage, ExecuteCommandRequest.class);
            case ExecuteCommandReply -> this.gson.fromJson(jsonMessage, ExecuteCommandReply.class);
            case VoteRequest -> this.gson.fromJson(jsonMessage, VoteRequest.class);
            case VoteReply -> this.gson.fromJson(jsonMessage, VoteReply.class);
            default -> throw new BadMessageException("invalid message: " + jsonMessage);
        };
    }
}
