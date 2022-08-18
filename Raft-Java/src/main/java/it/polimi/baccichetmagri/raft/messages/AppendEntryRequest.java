package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

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
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.logEntries = logEntries;
        this.leaderCommit = leaderCommit;
    }

    @Override
    public void execute(ConsensusModuleProxy consensusModuleProxy) throws IOException {
        consensusModuleProxy.callAppendEntries(this.term, this.leaderId, this.prevLogIndex, this.prevLogTerm,
                this.logEntries, this.leaderCommit, this.getMessageId());
    }
}
