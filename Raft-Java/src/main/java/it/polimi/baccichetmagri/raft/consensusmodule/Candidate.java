package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.network.Configuration;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.IOException;
import java.util.*;

class Candidate extends ConsensusModuleAbstract {

    private final Timer timer;
    private Election election; // represents the current election

    Candidate(int id, Configuration configuration, Log log, StateMachine stateMachine,
              ConsensusModule container) {
        super(id, configuration, log, stateMachine, container);
        this.timer = new Timer();

    }

    @Override
    synchronized void initialize() throws IOException {
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(false);
        this.startElection();
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException {
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        if (term > currentTerm) { // there is an election occurring in a term > currentTerm, convert to follower
            this.stopElectionTimer();
            this.election.loseElection();
        }
        return new VoteResult(currentTerm, false);
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit)
        throws IOException {

        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        // reply false if term < currentTerm
        if (term < currentTerm) {
            return new AppendEntryResult(currentTerm, false);
        } else {
            // a new leader has been established, convert to follower and process the request as follower
            this.stopElectionTimer();
            this.election.loseElection();
            this.configuration.setLeader(leaderID);
            Follower follower = new Follower(this.id, this.configuration, this.log, this.stateMachine, this.container);
            return follower.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
        }
    }

    @Override
    public synchronized ExecuteCommandResult executeCommand(Command command) {
        return new ExecuteCommandResult(null, false, null);
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) {
        return 0; // TODO implementare
    }

    private synchronized void startElection() throws IOException {

        // increment current term
        this.consensusPersistentState.setCurrentTerm(this.consensusPersistentState.getCurrentTerm() + 1);

        // vote for self
        this.consensusPersistentState.setVotedFor(this.id);

        // reset election timer
        this.startElectionTimer();

        // send RequestVote RPCs to all other servers
        this.election = new Election(this.configuration.getServersNumber() / 2
                + this.configuration.getServersNumber() % 2);
        this.election.incrementVotesReceived();
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastLogIndex();
        int lastLogTerm = this.log.getEntryTerm(lastLogIndex);
        List<Thread> requestVoteRPCThreads = new ArrayList<>();

        while(proxies.hasNext()) {
            ConsensusModuleProxy proxy = proxies.next();
            Thread rpcThread = new Thread(() -> {
                try {
                    VoteResult voteResult = proxy.requestVote(currentTerm, id, lastLogIndex, lastLogTerm);
                    if (!voteResult.isVoteGranted() && voteResult.getTerm() > currentTerm) {
                        this.election.loseElection();
                    } else if (voteResult.isVoteGranted()) {
                        this.election.incrementVotesReceived();
                    }
                } catch (IOException e) {
                    // error in network communication, assume vote not granted
                } catch (InterruptedException e) {

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
        switch (electionOutcome) {
            case WON: this.toLeader();
            case LOST: this.toFollower();
            case EXPIRED: this.startElection();
        }
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModuleAbstract.ELECTION_TIMEOUT_MAX -
                ConsensusModuleAbstract.ELECTION_TIMEOUT_MIN + 1) + ConsensusModuleAbstract.ELECTION_TIMEOUT_MIN;
        // when the timer expires, interrupt all threads where RequestVoteRPC was sent
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                election.expireElection();
            }
        }, delay);
    }

    private synchronized void stopElectionTimer() {
        this.timer.cancel();
    }

    private void toFollower() {
        this.container.changeConsensusModuleImpl(new Follower(this.id, this.configuration, this.log,
                this.stateMachine, this.container));
    }

    private void toLeader() {
        this.container.changeConsensusModuleImpl(new Leader(this.id, this.configuration, this.log,
                this.stateMachine, this.container));
    }

}
