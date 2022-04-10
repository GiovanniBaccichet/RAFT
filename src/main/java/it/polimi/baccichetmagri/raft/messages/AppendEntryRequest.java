package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

public class AppendEntryRequest extends Message{

    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private LogEntry[] logEntries;
    private int leaderCommit;

    public AppendEntryRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm,
                              LogEntry[] logEntries, int leaderCommit) {
        super(MessageId.AppendEntryRequest);
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logEntries = logEntries;
        this.leaderCommit = leaderCommit;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) {

    }
}
