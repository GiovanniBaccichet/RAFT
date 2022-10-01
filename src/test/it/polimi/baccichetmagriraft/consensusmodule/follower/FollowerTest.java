package it.polimi.baccichetmagriraft.consensusmodule.follower;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.machine.CommandImplementation;
import it.polimi.baccichetmagri.raft.machine.StateImplementation;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class FollowerTest {

    StateMachine stateMachine;
    Log log;
    Follower follower;

    @BeforeEach
    void initialize() throws IOException {
        stateMachine = new StateMachineImplementation();
        log =  new Log(Path.of(Log.LOG_FILENAME), stateMachine);
        log.deleteEntriesFrom(1);
        ConsensusPersistentState consensusPersistentState = new ConsensusPersistentState();
        consensusPersistentState.setCurrentTerm(0);
        consensusPersistentState.setVotedFor(null);
        follower = new Follower(1,consensusPersistentState, 0, 0,
                new FollowerTestConfiguration(),log, stateMachine, null);
    }

    @Test
    void testAppendEntries() throws IOException, SnapshottedEntryException {
        // append entry with no existing conflicting entry, leaderCommit > commitIndex
        LogEntry logEntry = new LogEntry(1, new CommandImplementation(3));
        AppendEntryResult result = follower.appendEntries(1, 0, 0, 0, List.of(logEntry), 1);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getTerm());
        assertEquals(1, log.size());
        assertEquals(1, log.getEntryTerm(1));
        assertEquals(3, ((StateImplementation) stateMachine.getState()).getNumber());

        // append 3 entries with no existing conflicting entries, commit only first 2
        List<LogEntry> logEntries = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            logEntries.add(new LogEntry(2, new CommandImplementation(i)));
        }
        result = follower.appendEntries(2, 0, 1, 1, logEntries, 3);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getTerm());
        assertEquals(4, log.size());
        assertEquals(1, log.getEntryTerm(1));
        for (int i = 2; i <= 4; i++) {
            assertEquals(2, log.getEntryTerm(i));
        }
        assertEquals(4, ((StateImplementation) stateMachine.getState()).getNumber());

        // leader term < follower term -> reply false
        result = follower.appendEntries(1, 0, 4, 2, new ArrayList<>(), 3);
        assertFalse(result.isSuccess());
        assertEquals(2, result.getTerm());
        assertEquals(4, log.size());
        assertEquals(1, log.getEntryTerm(1));
        for (int i = 2; i <= 4; i++) {
            assertEquals(2, log.getEntryTerm(i));
        }
        assertEquals(4, ((StateImplementation) stateMachine.getState()).getNumber());

        // log does not contain entry at prevLogIndex -> reply false
        result = follower.appendEntries(2, 0, 5, 1, new ArrayList<>(), 3);
        assertFalse(result.isSuccess());
        assertEquals(2, result.getTerm());
        assertEquals(4, log.size());
        assertEquals(1, log.getEntryTerm(1));
        for (int i = 2; i <= 4; i++) {
            assertEquals(2, log.getEntryTerm(i));
        }
        assertEquals(4, ((StateImplementation) stateMachine.getState()).getNumber());

        // log contains entry at prevLogIndex, but with different term -> reply false
        result = follower.appendEntries(2, 0, 3, 1, new ArrayList<>(), 3);
        assertFalse(result.isSuccess());
        assertEquals(2, result.getTerm());
        assertEquals(4, log.size());
        assertEquals(1, log.getEntryTerm(1));
        for (int i = 2; i <= 4; i++) {
            assertEquals(2, log.getEntryTerm(i));
        }
        assertEquals(4, ((StateImplementation) stateMachine.getState()).getNumber());

        // log contains existing uncommitted entry conflicting with new one -> remove it from log, reply true
        logEntries.clear();
        logEntries.add(new LogEntry(3, new CommandImplementation(3)));
        logEntries.add(new LogEntry(3, new CommandImplementation(4)));
        result = follower.appendEntries(3, 0, 3, 2, logEntries, 5);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getTerm());
        assertEquals(5, log.size());
        assertEquals(1, log.getEntryTerm(1));
        assertEquals(2, log.getEntryTerm(2));
        assertEquals(2, log.getEntryTerm(3));
        assertEquals(3, log.getEntryTerm(4));
        assertEquals(3, log.getEntryTerm(5));
        assertEquals(11, ((StateImplementation) stateMachine.getState()).getNumber());
    }

    @Test
    void testRequestVote() throws  IOException {
        // initialize follower log
        List<LogEntry> logEntries = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            logEntries.add(new LogEntry(i, new CommandImplementation(i)));
        }
        follower.appendEntries(3, 0, 0, 0, logEntries, 0);

        // candidate term < follower term -> reply false
        VoteResult voteResult = follower.requestVote(2, 0, 3, 3);
        assertFalse(voteResult.isVoteGranted());
        assertEquals(3, voteResult.getTerm());

        // candidate log not up-to-date w.r.t. follower log -> reply false
        voteResult = follower.requestVote(3, 0, 3, 2);
        assertFalse(voteResult.isVoteGranted());
        assertEquals(3, voteResult.getTerm());
        voteResult = follower.requestVote(3, 0, 2, 3);
        assertFalse(voteResult.isVoteGranted());
        assertEquals(3, voteResult.getTerm());

        // log up-to-date, follower has not voted yet, and then has voted for same candidate -> reply two times true
        for (int i = 0; i < 2; i++) {
            voteResult = follower.requestVote(4, 0, 4, 4);
            assertTrue(voteResult.isVoteGranted());
            assertEquals(4, voteResult.getTerm());
        }

        // vote already granted to another candidate -> reply false
        voteResult = follower.requestVote(4, 1, 4, 4);
        assertFalse(voteResult.isVoteGranted());
        assertEquals(4, voteResult.getTerm());
    }

}
