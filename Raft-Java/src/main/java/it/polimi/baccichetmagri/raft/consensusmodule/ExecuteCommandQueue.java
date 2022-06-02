package it.polimi.baccichetmagri.raft.consensusmodule;

import java.util.LinkedList;
import java.util.List;

class ExecuteCommandQueue {

    private final List<Integer> indexesQueue = new LinkedList<>();

    synchronized void waitForEntryCommitted(int index) throws InterruptedException {
        int i = 0;
        while (i < this.indexesQueue.size()) {
            if (index > this.indexesQueue.get(i)) {
                break;
            }
            i++;
        }
        this.indexesQueue.add(i, index);
        this.indexesQueue.get(i).wait();
    }

    synchronized void notifyCommittedEntry() {
        this.indexesQueue.get(0).notify();
    }
}
