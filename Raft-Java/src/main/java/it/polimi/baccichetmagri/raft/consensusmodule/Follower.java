package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.LogEntryStatus;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class Follower extends ConsensusModuleAbstract {

    private final Timer timer;

    Follower(int id, Configuration configuration, Log log, StateMachine stateMachine,
             ConsensusModule container) {
        super(id, configuration, log, stateMachine, container);
        this.timer = new Timer();
    }

    @Override
    synchronized void initialize() {
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(true);
        this.startElectionTimer();
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term,
                                                        int leaderID,
                                                        int prevLogIndex,
                                                        int prevLogTerm,
                                                        List<LogEntry> logEntries,
                                                        int leaderCommit) throws IOException {

        this.stopElectionTimer();

        // Read currentTerm (1 time access)
        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        //  Reply false if term < currentTerm
        if (term < currentTerm ) {
            this.startElectionTimer();
            return new AppendEntryResult(currentTerm, false);
        }

        // Update leader
        this.configuration.setLeader(leaderID);

        // If term T > currentTerm: set currentTerm = T
        this.updateTerm(term);


        //  Reply false  if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm
        if (this.log.containsEntry(prevLogIndex, prevLogTerm) == LogEntryStatus.NOT_EXISTENT) {
            this.startElectionTimer();
            return new AppendEntryResult(currentTerm, false);
        }

        // If an existing entry conflicts with a new one (same index but different terms), delete the existing entry and all that follow it
        boolean conflict = false;
        for (int i = 0; i < logEntries.size() && !conflict; i++) {
            int entryTerm = this.log.getEntryTerm(prevLogIndex + i + 1);
            if (entryTerm != logEntries.get(i).getTerm()) {
                this.log.deleteEntriesFrom(i);
                conflict = true;
            }
        }

        // Append any new entries not already in the log
        int lastLogIndex = this.log.getLastLogIndex();
        for (int i = 0; i < logEntries.size(); i++) {
            if (lastLogIndex < prevLogIndex + i + 1) {
                this.log.appendEntry(logEntries.get(i), this.commitIndex);
                lastLogIndex++;
            }
        }

        // If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of last new entry)
        if (leaderCommit > this.commitIndex) {
            this.commitIndex = Math.min(leaderCommit, lastLogIndex);
            // If commitIndex > lastApplied: increment lastApplied, apply log[lastApplied] to state machine
            while (this.commitIndex > this.lastApplied) {
                this.lastApplied++;
                this.stateMachine.executeCommand(this.log.getEntryCommand(lastApplied));
            }
        }

        this.startElectionTimer();
        return new AppendEntryResult(currentTerm, true);
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        return new ExecuteCommandResult(null, false,
                this.configuration.getLeaderIP());
    }

    public synchronized VoteResult requestVote(int term,
                                               int candidateID,
                                               int lastLogIndex,
                                               int lastLogTerm) throws IOException{
        this.stopElectionTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        //  Reply false if term < currentTerm
        if (term < currentTerm) {
            this.startElectionTimer();
            return new VoteResult(currentTerm, false);
        }

        this.updateTerm(term);

        //  If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote
        Integer votedFor = this.consensusPersistentState.getVotedFor();
        int lastIndex = this.log.getLastLogIndex();
        if ((votedFor == null || votedFor == candidateID) && (lastIndex <= lastLogIndex && this.log.getLastLogTerm() <= lastLogTerm)) {
            this.consensusPersistentState.setVotedFor(candidateID);
            this.startElectionTimer();
            return new VoteResult(currentTerm, true);
        }

        // Otherwise, reply false
        this.startElectionTimer();
        return new VoteResult(currentTerm, false);
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) {
        return 0; // TODO implementare
    }

    // If RPC request or response contains term T > currentTerm: set currentTerm = T
    private void updateTerm(int term) throws IOException {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
        }
    }

    private synchronized void toCandidate() {
        this.container.changeConsensusModuleImpl(new Candidate(this.id, this.configuration, this.log,
                this.stateMachine, this.container));
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModuleAbstract.ELECTION_TIMEOUT_MAX -
                ConsensusModuleAbstract.ELECTION_TIMEOUT_MIN + 1) + ConsensusModuleAbstract.ELECTION_TIMEOUT_MIN;
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toCandidate();
            }
        }, delay);
    }

    private void stopElectionTimer() {
        this.timer.cancel();
    }

}
