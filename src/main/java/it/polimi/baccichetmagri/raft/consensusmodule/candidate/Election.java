package it.polimi.baccichetmagri.raft.consensusmodule.candidate;

class Election {
    private ElectionOutcome electionOutcome;
    private int votesReceived;
    private final int votesNeeded;

    Election(int votesNeeded) {
        this.electionOutcome = null;
        this.votesReceived = 0;
        this.votesNeeded = votesNeeded;
    }

    synchronized void incrementVotesReceived() {
        this.votesReceived++;
        if (this.votesReceived >= this.votesNeeded) {
            this.electionOutcome = ElectionOutcome.WON;
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Election WON");
            this.notify();
        }
    }

    synchronized void loseElection() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Election LOST");
        this.electionOutcome = ElectionOutcome.LOST;
        this.notify();
    }

    synchronized void expireElection() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Election EXPIRED");
        this.electionOutcome = ElectionOutcome.EXPIRED;
        this.notify();
    }

    synchronized ElectionOutcome getElectionOutcome() {
        if (this.electionOutcome == null) {
            try {
                this.wait();
            } catch(InterruptedException e) {
                // should never occur
                e.printStackTrace();
            }
        }
        return this.electionOutcome;
    }
}
