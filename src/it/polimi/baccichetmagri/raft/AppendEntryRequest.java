package it.polimi.baccichetmagri.raft;

public class AppendEntryRequest {

    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private LogEntry[] logEntries;
    private int leaderCommit;

}
