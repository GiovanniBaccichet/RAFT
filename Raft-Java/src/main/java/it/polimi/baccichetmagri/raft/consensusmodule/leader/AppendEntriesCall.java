package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

class AppendEntriesCall {

    private int nextIndex;
    private int matchIndex;
    private ConsensusModuleProxy proxy;
    private Thread thread;
    private boolean isRunning;

    AppendEntriesCall(int nextIndex, int matchIndex, ConsensusModuleProxy proxy) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.proxy = proxy;
    }

    void callAppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, Log log, int leaderCommit, IndexesToCommit indexesToCommit) {
        if (!isRunning) {
            isRunning = true;

        }
    }

    void interruptCall() {

    }

}
