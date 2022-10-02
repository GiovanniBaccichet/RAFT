package it.polimi.baccichetmagriraft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.leader.Leader;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import it.polimi.baccichetmagriraft.consensusmodule.ConfigurationStub;
import it.polimi.baccichetmagriraft.consensusmodule.ConsensusModuleStub;
import it.polimi.baccichetmagriraft.consensusmodule.ContainerStub;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LeaderTest {
    @Test
    void testExecuteCommand() throws IOException {
        // all followers respond positively to the first call of appendEntries()
        ConsensusPersistentState consensusPersistentState = new ConsensusPersistentState();
        consensusPersistentState.setCurrentTerm(0);
        consensusPersistentState.setVotedFor(0);
        List<ConsensusModuleStub> stubs = new ArrayList<>();
        stubs.add(new ConsensusModuleStub(true, 0));
        stubs.add(new ConsensusModuleStub(true, 0));
        ConfigurationStub configurationStub = new ConfigurationStub(stubs);
        StateMachine stateMachine = new StateMachineImplementation();
        Log log = new Log(Path.of(Log.LOG_FILENAME), stateMachine);
        ContainerStub containerStub = new ContainerStub();
        Leader leader = new Leader(0, consensusPersistentState, 0, 0, configurationStub, log, stateMachine,
                containerStub);
    }
}
