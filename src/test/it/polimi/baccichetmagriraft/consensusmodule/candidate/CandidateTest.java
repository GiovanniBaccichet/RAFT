package it.polimi.baccichetmagriraft.consensusmodule.candidate;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusPersistentState;
import it.polimi.baccichetmagri.raft.consensusmodule.candidate.Candidate;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CandidateTest {

    private Candidate candidate;
    private ContainerStub container;


    void setUp(List<ConsensusModuleStub> stubs) throws IOException {
        ConsensusPersistentState consensusPersistentState = new ConsensusPersistentState();
        consensusPersistentState.setVotedFor(null);
        consensusPersistentState.setCurrentTerm(0);
        CandidateTestConfiguration configuration = new CandidateTestConfiguration(stubs);
        StateMachine stateMachine = new StateMachineImplementation();
        Log log = new Log(Path.of(Log.LOG_FILENAME), stateMachine);
        log.deleteEntriesFrom(1);
        container = new ContainerStub();
        candidate = new Candidate(0, consensusPersistentState, 0, 0, configuration,
                log, stateMachine, container);
        container.changeConsensusModuleImpl(candidate);
        candidate.initialize();
    }

    @Test
    void testElectionWon() throws IOException {
        List<ConsensusModuleStub> stubs = new ArrayList<>();
        stubs.add(new ConsensusModuleStub(true, 0));
        stubs.add(new ConsensusModuleStub(false, 0));
        setUp(stubs);
        assertEquals("LEADER", container.getConsensusModuleType());
    }

    @Test
    void testElectionLostBecauseOfVotes() throws IOException {
        List<ConsensusModuleStub> stubs = new ArrayList<>();
        stubs.add(new ConsensusModuleStub(false, 0));
        stubs.add(new ConsensusModuleStub(false, 0));
        setUp(stubs);
        assertEquals("FOLLOWER", container.getConsensusModuleType());
    }

    @Test
    void testElectionLostBecauseOfTerm() throws IOException {
        List<ConsensusModuleStub> stubs = new ArrayList<>();
        stubs.add(new ConsensusModuleStub(false, 2));
        stubs.add(new ConsensusModuleStub(false, 2));
        setUp(stubs);
        assertEquals("FOLLOWER", container.getConsensusModuleType());
    }

    @Test
    void testElectionExpiredAndThenWon() throws IOException, InterruptedException {
        List<ConsensusModuleStub> stubs = new ArrayList<>();
        stubs.add(new ConsensusModuleStub(false, 0, ConsensusModule.ELECTION_TIMEOUT_MAX * 10));
        stubs.add(new ConsensusModuleStub(true, 0, ConsensusModule.ELECTION_TIMEOUT_MAX * 10));
        setUp(stubs);
        Thread.sleep(ConsensusModule.ELECTION_TIMEOUT_MAX + 100);
        assertEquals("LEADER", container.getConsensusModuleType());
    }
}
