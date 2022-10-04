package it.polimi.baccichetmagri.raft.consensusmodule.candidate;

class Election {
    private ElectionOutcome electionOutcome;
    private int positiveVotesReceived;
    private int totalVotesReceived;
    private final int positiveVotesNeeded;
    private final int totalVotesNeeded;

    Election(int positiveVotesNeeded, int totalVotesNeeded) {
        this.electionOutcome = null;
        this.positiveVotesReceived = 0;
        this.totalVotesReceived = 0;
        this.positiveVotesNeeded = positiveVotesNeeded;
        this.totalVotesNeeded = totalVotesNeeded;
    }

    synchronized void incrementVotesReceived(boolean voteGranted) {
        this.totalVotesReceived++;
        if (voteGranted) {
            this.positiveVotesReceived++;
        }
        if (this.positiveVotesReceived >= this.positiveVotesNeeded) {
            this.electionOutcome = ElectionOutcome.WON;
            System.out.println("[" + this.getClass().getSimpleName() + "] " + this.electionOutcome);
            this.notify();
        }
        if(totalVotesReceived == totalVotesNeeded) {
            this.electionOutcome = (this.positiveVotesReceived >= this.positiveVotesNeeded) ? ElectionOutcome.WON : ElectionOutcome.LOST;
            System.out.println("[" + this.getClass().getSimpleName() + "] " + this.electionOutcome);
            this.notify();
        }
    }

    synchronized void loseElection() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "\u001B[43m" + "Election LOST" + "\u001B[0m");
        this.electionOutcome = ElectionOutcome.LOST;
        this.notify();
    }

    synchronized void expireElection() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Election EXPIRED");
        this.electionOutcome = ElectionOutcome.EXPIRED;
        this.notify();
    }

    synchronized boolean isExpired() {
        return this.electionOutcome == ElectionOutcome.EXPIRED;
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
