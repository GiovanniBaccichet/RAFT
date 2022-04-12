package it.polimi.baccichetmagri.raft.messages;

public enum MessageType {
    AppendEntryRequest,
    AppendEntryResult,
    ExecuteCommandRequest,
    ExecuteCommandResult,
    VoteRequest,
    VoteResult
}
