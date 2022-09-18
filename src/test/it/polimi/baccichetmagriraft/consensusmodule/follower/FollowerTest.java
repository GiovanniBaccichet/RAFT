package it.polimi.baccichetmagriraft.consensusmodule.follower;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.CommandImplementation;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

class FollowerTest {

    StateMachine stateMachine;
    Log log;
    Follower follower;

    @BeforeEach
    void initialize() throws IOException {
        stateMachine = new StateMachineImplementation();
        log =  new Log(Path.of(Log.LOG_FILENAME), stateMachine);
        follower = new Follower(1, new ConsensusPersistentState(), 0, 0,
                new FollowerTestConfiguration(),log, stateMachine, null);
    }

    @Test
    void testAppendEntries() throws IOException {
        // append entry with no existing conflicting entry, leaderCommit > commitIndex -> TRUE
        LogEntry logEntry = new LogEntry(1, new CommandImplementation(3));
        AppendEntryResult result = follower.appendEntries(1, 0, 0, 0, List.of(logEntry), 1);
        assertTrue(result.isSuccess());
    }
}
