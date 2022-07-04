package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import java.util.ArrayList;
import java.util.List;

class IndexesToCommit {

    private final List<Integer> indexes = new ArrayList<>();
    private ExecuteCommandDirective directive = ExecuteCommandDirective.PROCEED;


    synchronized ExecuteCommandDirective waitForFollowerReplies(int indexToCommit) throws InterruptedException {
        this.indexes.stream().filter(index -> index == indexToCommit).findAny().orElseGet(() -> {
            this.indexes.add(indexToCommit);
            return this.indexes.get(this.indexes.size() - 1);
        }).wait();
        return this.directive;
    }

    synchronized void notifyFollowerReply(int indexToCommit) {
        this.indexes.stream().filter(index -> index == indexToCommit).findAny().ifPresent(Integer::notify);
    }

    synchronized void removeIndex(int indexToRemove) {
        this.indexes.removeIf(index -> index == indexToRemove);
    }

    synchronized void notifyAllToInterrupt() {
        this.directive = ExecuteCommandDirective.INTERRUPT;
        for (Integer index : this.indexes) {
            index.notify();
        }
    }

    synchronized void notifyIndexesToCommitUpTo(int indexToCommit) {
        for (Integer index : this.indexes) {
            if (index <= indexToCommit) {
                index.notify();
            }
        }
    }
}


