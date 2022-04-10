package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.messages.AppendEntryResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;

public class Follower extends ConsensusModule {

    @Override
    public synchronized AppendEntryResult appendEntries(int term,
                                           int leaderID,
                                           int prevLogIndex,
                                           int prevLogTerm,
                                           LogEntry[] logEntries,
                                           int leaderCommit) {

        // Read currentTerm (1 time access)
        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        //  Reply false if term < currentTerm or Reply false if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm
        if (term < currentTerm || !this.log.containsEntry(prevLogIndex, prevLogTerm)) {
            return new AppendEntryResult(currentTerm, false, this.id);
        }

        this.updateTerm(term); // If term T > currentTerm: set currentTerm = T

        // If an existing entry conflicts with a new one (same index but different terms), delete the existing entry and all that follow it
        boolean conflict = false;
        for (int i = prevLogIndex + 1; i < logEntries.length && !conflict; i++) {
            int entryTerm = this.log.getEntryTerm(i);
            if (entryTerm > 0 && entryTerm != term) {
                this.log.deleteEntriesFrom(i);
                conflict = true;
            }
        }

        // Append any new entries not already in the log
        int lastLogIndex = this.log.getLastIndex();
        for (int i = 0; i < logEntries.length; i++) {
            if (lastLogIndex < prevLogIndex + i + 1) {
                this.log.appendEntry(logEntries[i]);
                lastLogIndex++;
            }
        }

        // If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of last new entry)
        if (leaderCommit > this.commitIndex) {
            this.commitIndex = Math.min(leaderCommit, lastLogIndex);
            this.checkCommitIndex(); // If commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
        }

        return new AppendEntryResult(currentTerm, true, this.id);
    }

    // If RPC request or response contains term T > currentTerm: set currentTerm = T (ALREADY follower)
    @Override
    protected void updateTerm(int term) {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
        }
    }

}
