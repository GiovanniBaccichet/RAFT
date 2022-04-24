package it.polimi.baccichetmagri.raft.messages;

import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;

public class AppendEntryRequest extends Message{

    private final int term;
    private final int leaderId;
    private final int prevLogIndex;
    private final int prevLogTerm;
    private final LogEntry[] logEntries;
    private final int leaderCommit;

    public AppendEntryRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm,
                              LogEntry[] logEntries, int leaderCommit, int messageId) {
        super(MessageType.AppendEntryRequest, messageId);
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
