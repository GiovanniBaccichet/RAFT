package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;

public class Candidate extends ConsensusModule {

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

}
