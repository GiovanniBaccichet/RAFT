package it.polimi.baccichetmagri.raft;

import java.util.List;

public abstract class ConsensusModule {

    private int id;
    private ConsensusPersistentState  consensusPersistentState;
    private int commitIndex;
    private int lastApplied;
    private List<Integer> servers;

    public VoteResult requestVote(int term, int cadidateID, int lastLogIndex, int lastLogTerm) {

        return null; // TODO cambiare
    }

    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

}
