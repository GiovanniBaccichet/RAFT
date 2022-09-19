package it.polimi.baccichetmagri.raft.consensusmodule.follower;

import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.candidate.Candidate;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.LogEntryStatus;
import it.polimi.baccichetmagri.raft.log.snapshot.LogSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.log.snapshot.TemporarySnapshot;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Follower extends ConsensusModule {

    private Timer timer;
    private final Logger logger;

    public Follower(int id, ConsensusPersistentState consensusPersistentState, int commitIndex, int lastApplied,
                    Configuration configuration, Log log, StateMachine stateMachine, ConsensusModuleContainer container) {
        super(id, consensusPersistentState, commitIndex, lastApplied, configuration, log, stateMachine, container);
        this.timer = new Timer();
        this.logger = Logger.getLogger(Follower.class.getName());
    }

    @Override
    public synchronized void initialize() {
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(true);
        this.configuration.discardInstallSnapshotReplies(true);
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
            return new AppendEntryResult(this.consensusPersistentState.getCurrentTerm(), false);
        }

        // If an existing entry conflicts with a new one (same index but different terms), delete the existing entry and all that follow it
        int lastLogIndex = this.log.getLastLogIndex();
        if (prevLogIndex < lastLogIndex) { // some of the entries sent overlap with existing entries:
                                           // if terms are different, they conflict, otherwise they are the same entry
            try {
                boolean conflict = false;
                for (int i = 0; i < logEntries.size() && !conflict && prevLogIndex + i + 1 <= lastLogIndex; i++) {
                    int entryTerm = this.log.getEntryTerm(prevLogIndex + i + 1);
                    if (entryTerm != logEntries.get(i).getTerm()) {
                        this.log.deleteEntriesFrom(prevLogIndex + i + 1);
                        conflict = true;
                    }
                }
            } catch (SnapshottedEntryException e) {
                // if the entry is snapshotted, then there is no need to perform this operation, as the entry is already committed
            }
        }

        lastLogIndex = this.log.getLastLogIndex();
        // Append any new entries not already in the log
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
                try {
                    this.stateMachine.executeCommand(this.log.getEntryCommand(lastApplied));
                } catch (SnapshottedEntryException e) {
                    // can't happen, as the entry hasn't been committed yet
                    e.printStackTrace();
                    Server.shutDown();
                }
            }
        }

        this.startElectionTimer();
        return new AppendEntryResult(this.consensusPersistentState.getCurrentTerm(), true);
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        return new ExecuteCommandResult(null, false,
                this.configuration.getLeaderIP());
    }

    public synchronized VoteResult requestVote(int term,
                                               int candidateID,
                                               int candidateLastLogIndex,
                                               int candidateLastLogTerm) throws IOException{
        this.stopElectionTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        //  Reply false if term < currentTerm
        if (term < currentTerm) {
            this.startElectionTimer();
            return new VoteResult(currentTerm, false);
        }

        this.updateTerm(term);

        // If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote.

        // Raft determines which of two logs is more up-to-date by comparing the index and term of the last entries in the logs.
        // If the logs have last entries with different terms, then the log with the later term is more up-to-date.
        // If the logs end with the same term, then whichever log is longer is more up-to-date.
        Integer votedFor = this.consensusPersistentState.getVotedFor();
        int lastIndex = this.log.getLastLogIndex();
        int lastTerm = this.log.getLastLogTerm();

        boolean upToDate = (lastTerm != candidateLastLogTerm) ? (lastTerm > candidateLastLogTerm) : (lastIndex >= candidateLastLogIndex);
        if ((votedFor == null || votedFor == candidateID) && upToDate) {
            this.consensusPersistentState.setVotedFor(candidateID);
            this.startElectionTimer();
            return new VoteResult(currentTerm, true);
        }

        // Otherwise, reply false
        this.startElectionTimer();
        return new VoteResult(currentTerm, false);
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException {
        this.stopElectionTimer();

        int currentTerm = this.consensusPersistentState.getCurrentTerm();

        // Reply immediately if term < currentTerm.
        if (term < currentTerm) {
            this.startElectionTimer();
            return currentTerm;
        }

        this.configuration.setLeader(leaderID);

        // Write data into snapshot file at given offset
        TemporarySnapshot temporarySnapshot = new TemporarySnapshot();
        temporarySnapshot.writeChunk(data, offset);

        if (done) {
            // Save snapshot file
            LogSnapshot logSnapshot = new LogSnapshot();
            logSnapshot.saveTemporarySnapshot();

            // Discard the entire log
            this.log.deleteEntriesFrom(1);

            //Reset state machine using snapshot contents (TODO and load snapshot’s cluster configuration)
            this.stateMachine.resetState(logSnapshot.getMachineState());
        }

        this.startElectionTimer();
        return currentTerm;
    }

    @Override
    public String toString() {
        return "FOLLOWER";
    }

    // If RPC request or response contains term T > currentTerm: set currentTerm = T
    private void updateTerm(int term) throws IOException {
        if (term > this.consensusPersistentState.getCurrentTerm()) {
            this.consensusPersistentState.setCurrentTerm(term);
        }
    }

    private synchronized void toCandidate() throws IOException {
        (new TemporarySnapshot()).delete(); // when converting to candidate, delete any content of the temporary snapshot
        this.container.changeConsensusModuleImpl(new Candidate(this.id, this.consensusPersistentState, this.commitIndex, this.lastApplied,
                this.configuration, this.log, this.stateMachine, this.container));
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModule.ELECTION_TIMEOUT_MAX -
                ConsensusModule.ELECTION_TIMEOUT_MIN + 1) + ConsensusModule.ELECTION_TIMEOUT_MIN;
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    toCandidate();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Impossible to delete the temporary snapshot file");
                    e.printStackTrace();
                    Server.shutDown();
                }
            }
        }, delay);
    }

    private void stopElectionTimer() {
        this.timer.cancel();
    }

}
