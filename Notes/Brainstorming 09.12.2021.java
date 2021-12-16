
// ============================ CANDIDATE ===============================================

Candidate :: startElection() {
    int favorVotes=1; // Voted for itself
	this.term++;
    this.votedFor = this.id;
    Timer timer = new Timer(this::timerCallback);
    this.electionTimer.start(); // Start election timeout
    // callback if elapsed time

    for(Server server : this.otherServers) { // Send vote request to all servers
       [term, voteGranted] = server.requestVoteRPC(this.term, this.ID, this.lastLogIndex, this.lastLogTerm); // Change to RPC syntax; execute in parallel
       if (voteGranted) {
           favorVotes++;
       }
    }
    if (favorVotes>server.size()/2) {
        this.actualLeader(); // If this node gets half of the votes, plus 1, it becomes the leader
    }
}

cadidate::timerCallback() {
    this.startElection();
} // vedere lambda functions

Candidate::appendEntriesRPC() {

} // for a candidate to become a follower

// ============================ SERVER ===============================================

class ReqVoteResult {
    int term;
    bool voteGranted;

    ReqVoteResult(term, votegranted) { // costruttore

    }
}

Server:: appendEntriesRPC(int term, ...) {
    this.checkTerm(term);
    ......
}

Server :: ReqVoteResult requestVoteRPC(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
    this.checkTerm(term);

    if (term < this.term) {
        return new ReqVoteResult(this.term,false);
    }
    if ((this.votedFor==NULL || this.votedFor == candidateId) && log up to date) {
        return new ReqVoteResult(this.term,true);
    }

    return new ReqVoteResult(this.term,false);;
}

Server :: checkTerm(term) {
    if(this.currentTerm < term) {
        this.currentTerm = term;
        this.toFollower():
    }
}
