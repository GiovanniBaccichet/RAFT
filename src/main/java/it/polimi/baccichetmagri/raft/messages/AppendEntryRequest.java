package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;
import java.util.List;

public class AppendEntryRequest extends Message{

    private final int term;
    private final int leaderId;
    private final int prevLogIndex;
    private final int prevLogTerm;
    private final List<LogEntry> logEntries;
    private final int leaderCommit;

    public AppendEntryRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm,
                              List<LogEntry> logEntries, int leaderCommit) {
        super(MessageType.AppendEntryRequest);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Append Entry Request | term: " + term + " | leaderId: " + leaderId + " |");
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logEntries = logEntries;
        this.leaderCommit = leaderCommit;
    }
    public int getTerm() {
        return term;
    }

    public int getLeaderId() {
        return leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

}
