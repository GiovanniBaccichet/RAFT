package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import java.util.concurrent.atomic.AtomicBoolean;

class EntryReplication {
    private ExecuteCommandDirective directive;
    private AtomicBoolean toFollower;

    EntryReplication(AtomicBoolean toFollower) {
        this.directive = ExecuteCommandDirective.COMMIT;
        this.toFollower = toFollower;
    }

    synchronized void notifySuccessfulReply() {
        this.notify();
    }

    synchronized void convertToFollower() {
        this.directive = ExecuteCommandDirective.CONVERT_TO_FOLLOWER;
        this.toFollower.set(true);
        this.notify();
    }

    synchronized ExecuteCommandDirective waitForFollowerReplies() throws InterruptedException {
        if (this.directive == null) {
            this.wait();
        }
        return this.directive;
    }
}
