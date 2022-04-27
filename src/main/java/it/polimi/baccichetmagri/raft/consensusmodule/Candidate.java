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

class Candidate extends ConsensusModuleImpl {

    private final Timer timer;
    private List<Thread> requestVoteRPCThreads;

    Candidate(int id, Configuration configuration, Log log, StateMachine stateMachine,
              ConsensusModule container) {
        super(id, configuration, log, stateMachine, container);
        this.timer = new Timer();
    }

    @Override
    synchronized void initialize() {
        this.configuration.discardAppendEntryReplies(true);
        this.configuration.discardRequestVoteReplies(false);
        this.startElection();
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
        return null;
    }

    @Override
    public synchronized AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, LogEntry[] logEntries, int leaderCommit) {

        return null; // TODO cambiare
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) {
        return null;
    }

    private void startElection() {

        // increment current term
        this.consensusPersistentState.setCurrentTerm(this.consensusPersistentState.getCurrentTerm() + 1);

        // vote for self
        this.consensusPersistentState.setVotedFor(this.id);

        // reset election timer
        this.startElectionTimer();

        // send RequestVote RPCs to all other servers
        Election election = new Election(this.configuration.getServersNumber() / 2
                + this.configuration.getServersNumber() % 2);
        election.incrementVotesReceived();
        Iterator<ConsensusModuleProxy> proxies = this.configuration.getIteratorOnAllProxies();
        int currentTerm = this.consensusPersistentState.getCurrentTerm();
        int lastLogIndex = this.log.getLastIndex();
        int lastLogTerm = this.log.getEntryTerm(lastLogIndex);

        while(proxies.hasNext()) {
            ConsensusModuleProxy proxy = proxies.next();
            (new Thread(() -> {
                try {
                    VoteResult voteResult = proxy.requestVote(currentTerm, id, lastLogIndex, lastLogTerm);
                    if (!voteResult.isVoteGranted() && voteResult.getTerm() > currentTerm) {
                        election.loseElection();
                    } else if (voteResult.isVoteGranted()) {
                        election.incrementVotesReceived();
                    }
                } catch (IOException e) {

                } catch (InterruptedException e) {
                    // the thread has been interrupted, so the election has expired
                    election.expireElection();
                }
            })).start();
        }

        ElectionOutcome electionOutcome = election.getElectionOutcome();
        switch (electionOutcome) {
            case WON: this.toLeader();
            case LOST: this.toFollower();
            case EXPIRED: this.startElection();
        }
    }

    private void startElectionTimer() {
        int delay = (new Random()).nextInt(ConsensusModuleImpl.ELECTION_TIMEOUT_MAX -
                ConsensusModuleImpl.ELECTION_TIMEOUT_MIN + 1) + ConsensusModuleImpl.ELECTION_TIMEOUT_MIN;
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startElection();
            }
        }, delay);
    }

    private void stopElectionTimer() {
        this.timer.cancel();
    }

    private void toFollower() {

    }

    private void toLeader() {

    }


}
