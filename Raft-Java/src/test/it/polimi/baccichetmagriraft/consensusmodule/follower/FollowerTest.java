package it.polimi.baccichetmagriraft.consensusmodule.follower;

import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class FollowerTest {

    @Test
    void appendEntriesTest() {
        try {
            StateMachine stateMachine = new StateMachineImplementation();
            Follower follower = new Follower(1, new FollowerTestConfiguration(), new Log(Path.of(Log.LOG_FILENAME), stateMachine),
                    stateMachine, null);
        } catch (IOException e) {

        }

    }
}
