package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class Follower extends ConsensusModuleImpl {

    private Timer timer;

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
                                                        LogEntry[] logEntries,
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
        if (term > currentTerm) {
            this.updateTerm(term);
        }

        //  Reply false  if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm
        if (!this.log.containsEntry(prevLogIndex, prevLogTerm)) {
            this.startElectionTimer();
            return new AppendEntryResult(currentTerm, false);
        }

        // If an existing entry conflicts with a new one (same index but different terms), delete the existing entry and all that follow it
        boolean conflict = false;
        for (int i = prevLogIndex + 1; i < logEntries.length && !conflict; i++) {
            int entryTerm = this.log.getEntryTerm(i);
            if (entryTerm != term) {
                this.log.deleteEntriesFrom(i);
                conflict = true;
            }
        }

        // Append any new entries not already in the log
        int lastLogIndex = this.log.getLastLogIndex();
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

        this.updateTerm(currentTerm);

        //  If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote
        Integer votedFor = this.consensusPersistentState.getVotedFor();
        int lastIndex = this.log.getLastLogIndex();
        if ((votedFor == null || votedFor == candidateID) && (lastIndex <= lastLogIndex && this.log.getEntryTerm(lastIndex) <= lastLogTerm)) {
            this.consensusPersistentState.setVotedFor(candidateID);
            this.startElectionTimer();
            return new VoteResult(currentTerm, true);
        }

        this.startElectionTimer();
        return new VoteResult(currentTerm, false);
    }


    // If RPC request or response contains term T > currentTerm: set currentTerm = T (ALREADY follower)
    @Override
    protected void updateTerm(int term) {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
        }
    }

    private synchronized void toCandidate() {
        this.container.changeConsensusModuleImpl(new Candidate(this.id, this.configuration, this.log,
                this.stateMachine, this.container));
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModuleImpl.ELECTION_TIMEOUT_MAX -
                ConsensusModuleImpl.ELECTION_TIMEOUT_MIN + 1) + ConsensusModuleImpl.ELECTION_TIMEOUT_MIN;
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
