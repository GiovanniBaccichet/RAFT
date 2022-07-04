package it.polimi.baccichetmagri.raft.messages;

public enum MessageType {
    AppendEntryRequest,
    AppendEntryReply,
    ExecuteCommandRequest,
    ExecuteCommandReply,
    VoteRequest,
    VoteReply
}
