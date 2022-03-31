package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.log.LogEntry;

public class AppendEntryRequest extends Message{

    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private LogEntry[] logEntries;
    private int leaderCommit;

}
