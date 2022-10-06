package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import java.util.concurrent.atomic.AtomicBoolean;

class EntryReplication {
    private ExecuteCommandDirective directive;

    EntryReplication() {
        this.directive = ExecuteCommandDirective.COMMIT;
    }

    synchronized void notifySuccessfulReply() {
        this.notify();
    }

    synchronized void convertToFollower() {
        this.directive = ExecuteCommandDirective.CONVERT_TO_FOLLOWER;
        this.notify();
    }

    synchronized ExecuteCommandDirective waitForFollowerReplies() throws InterruptedException {
        this.wait();
        return this.directive;
    }
}
