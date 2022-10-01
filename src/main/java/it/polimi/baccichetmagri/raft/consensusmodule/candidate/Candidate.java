package it.polimi.baccichetmagri.raft.consensusmodule.candidate;

import it.polimi.baccichetmagri.raft.consensusmodule.*;
import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.follower.Follower;
import it.polimi.baccichetmagri.raft.consensusmodule.leader.Leader;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Candidate extends ConsensusModule {

    private Timer timer;
    private Election election; // represents the current election
    private final Logger logger;

    public Candidate(int id, ConsensusPersistentState consensusPersistentState, int commitIndex, int lastApplied,
                     Configuration configuration, Log log, StateMachine stateMachine, ConsensusModuleContainer container) {
        super(id, consensusPersistentState, commitIndex, lastApplied, configuration, log, stateMachine, container);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Instancing a CANDIDATE");
        this.timer = new Timer();
        this.logger = Logger.getLogger(Candidate.class.getName());
        this.logger.setLevel(Level.FINE);
    }

    @Override
    public synchronized void initialize() throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Initializing a CANDIDATE");
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(false);
        this.configuration.discardInstallSnapshotReplies(true);
        this.startElection();
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Requesting vote");
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term > currentTerm) { // there is an election occurring in a term > currentTerm, convert to follower
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Converting to FOLLOWER (election lost)");
            this.stopElectionTimer();
            this.election.loseElection();
        }
        return new VoteResult(currentTerm, false);
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit)
        throws IOException {

        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        // reply false if term < currentTerm
        if (term < currentTerm) {
            return new AppendEntryResult(currentTerm, false);
        } else {
            // a new leader has been established, convert to follower and process the request as follower
            this.stopElectionTimer();
            this.election.loseElection();
            Follower follower = new Follower(this.id, this.consensusPersistentState, this.commitIndex, this.lastApplied,
                    this.configuration, this.log, this.stateMachine, this.container);
            return follower.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        }
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        return new ExecuteCommandResult(null, false, null);
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done)
        throws IOException {
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term < currentTerm) {
            return currentTerm;
        } else {
            // a new leader has been established, convert to follower and process the request as follower
            this.stopElectionTimer();
            this.election.loseElection();
            Follower follower = new Follower(this.id, this.consensusPersistentState, this.commitIndex, this.lastApplied,
                    this.configuration, this.log, this.stateMachine, this.container);
            return follower.installSnapshot(term, leaderID, lastIncludedIndex, lastIncludedTerm, offset, data, done);
        }
    }

    @Override
    public String toString() {
        return "CANDIDATE";
    }

    private synchronized void startElection() throws IOException {

        this.logger.log(Level.FINE, "Starting election");
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Starting election");

        // increment current term
        this.consensusPersistentState.setCurrentTerm(this.consensusPersistentState.getCurrentTerm() + 1);

        // vote for self
        this.consensusPersistentState.setVotedFor(this.id);

        // reset election timer
        this.startElectionTimer();

        // send RequestVote RPCs to all other servers
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Send RPCs to other servers");
        this.election = new Election(this.configuration.getServersNumber() / 2 + 1);
        this.election.incrementVotesReceived();
        Iterator<ConsensusModuleInterface> proxies = this.configuration.getIteratorOnAllProxies();
        // here printing
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastLogIndex(); // TO BE CHECKED
//        int lastLogIndex = 13;
        int lastLogTerm = this.log.getLastLogTerm(); // TO BE CHECKED
//        int lastLogTerm = 13;
        List<Thread> requestVoteRPCThreads = new ArrayList<>();

        while(proxies.hasNext()) {
            ConsensusModuleInterface proxy = proxies.next();
            Thread rpcThread = new Thread(() -> {
                try {
                    System.out.println("[" + this.getClass().getSimpleName() + "] " + "Requesting vote to id: " + proxy.getId());
                    VoteResult voteResult = proxy.requestVote(currentTerm, id, lastLogIndex, lastLogTerm);
                    if (!voteResult.isVoteGranted() && voteResult.getTerm() > currentTerm) {
                        this.election.loseElection();
                    } else if (voteResult.isVoteGranted()) {
                        this.election.incrementVotesReceived();
                    }
                } catch (IOException | InterruptedException e) {
                    // error in network communication, assume vote not granted
                    System.out.println("[" + this.getClass().getSimpleName() + "] " + "NETWORK ERROR!");
                    e.printStackTrace();
                }
            });
            requestVoteRPCThreads.add(rpcThread);
            rpcThread.start();
        }

        ElectionOutcome electionOutcome = this.election.getElectionOutcome();
        this.stopElectionTimer();
        for(Thread rpcThread : requestVoteRPCThreads) {
            rpcThread.interrupt();
        }

        this.logger.log(Level.FINE, "Election outcome: " + electionOutcome);
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Election outcome " + electionOutcome);

        switch (electionOutcome) {
            case WON: this.toLeader(); break;
            case LOST: this.toFollower(); break;
            case EXPIRED: this.startElection(); break;
        }
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModule.ELECTION_TIMEOUT_MAX - ConsensusModule.ELECTION_TIMEOUT_MIN + 1) + ConsensusModule.ELECTION_TIMEOUT_MIN + 15000;
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Started election time w/ delay: " + delay);
        // when the timer expires, interrupt all threads where RequestVoteRPC was sent
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread(()->{election.expireElection(); System.out.println("[Candidate] " + "Timer expired -> " + delay);}).start();
            }
        }, delay);
    }

    private synchronized void stopElectionTimer() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "canceling timer");
        this.timer.cancel();
    }

    private void toFollower() {
        this.container.changeConsensusModuleImpl(new Follower(this.id, this.consensusPersistentState, this.commitIndex,
                this.lastApplied, this.configuration, this.log, this.stateMachine, this.container));
    }

    private void toLeader() throws IOException {
        this.container.changeConsensusModuleImpl(new Leader(this.id, this.consensusPersistentState, this.commitIndex, this.lastApplied,
                this.configuration, this.log, this.stateMachine, this.container));
    }

}
