package it.polimi.baccichetmagri.raft.messages;

public enum MessageId {
    AppendEntryRequest,
    AppendEntryResult,
    ExecuteCommandRequest,
    ExecuteCommandResult,
    VoteRequest,
    VoteResult
}
