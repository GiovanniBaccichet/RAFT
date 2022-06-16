package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.consensusmodule.leader.ExecuteCommandDirective;

import java.util.LinkedList;
import java.util.List;

class ExecuteCommandQueue {

    private final List<Integer> indexesQueue = new LinkedList<>();
    private ExecuteCommandDirective directive = ExecuteCommandDirective.PROCEED;


    synchronized ExecuteCommandDirective waitForFollowerReplies(int indexToCommit) throws InterruptedException {
        int i = 0;
        while (i < this.indexesQueue.size()) {
            if (indexToCommit > this.indexesQueue.get(i)) {
                break;
            }
            i++;
        }
        this.indexesQueue.add(i, indexToCommit);
        this.indexesQueue.get(i).wait();
        return this.directive;
    }

    synchronized void notifyFollowerReply() {
        this.indexesQueue.get(0).notify();
    }

    synchronized void notifyAllToInterrupt() {
        this.directive = ExecuteCommandDirective.INTERRUPT;
        for (Integer index : this.indexesQueue) {
            index.notify();
        }
    }
}


