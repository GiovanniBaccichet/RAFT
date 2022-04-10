package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;

import java.util.Map;

public class Leader extends ConsensusModule {

    private Map<Integer, Integer> nextIndex; // for each server, index of the next log entry to send to that server (initialized to leader last log index + 1)
    private Map<Integer, Integer> matchIndex; // for each server, index of highest log entry known to be replicated on server (initialized to 0, increases monotonically)

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

}
