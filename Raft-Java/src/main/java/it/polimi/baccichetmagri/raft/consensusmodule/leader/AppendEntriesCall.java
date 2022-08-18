package it.polimi.baccichetmagri.raft.consensusmodule.leader;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.log.snapshot.JSONSnapshot;
import it.polimi.baccichetmagri.raft.log.snapshot.SnapshottedEntryException;
import it.polimi.baccichetmagri.raft.network.ConsensusModuleProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AppendEntriesCall {

    private static final int SNAPSHOT_CHUNK_SIZE = 5*1024; // Send chunks of 5 KB at a time, this parameter needs to be tuned wrt network and storage
    private int nextIndex; // index of the next log entry to send to that server (initialized to leader last log index + 1)
    private int matchIndex; // index of the highest log entry known to be replicated on server (initialized to 0, increases monotonically)
    private final ConsensusModuleProxy proxy;
    private Thread thread;
    private boolean isRunning;
    private final Log log;

    private final int leaderId;

    private int leaderCommit;
    private int term;

    private final List<EntryReplication> entryReplicationsToNotify;

    AppendEntriesCall(int nextIndex, int matchIndex, ConsensusModuleProxy proxy, Log log, int leaderId) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.proxy = proxy;
        this.log = log;
        this.leaderId = leaderId;
        this.entryReplicationsToNotify = new ArrayList<>();
    }

    synchronized void callAppendEntries(int term, int leaderCommit, EntryReplication entryReplication) {
        this.leaderCommit = leaderCommit;
        this.term = term;
        this.entryReplicationsToNotify.add(entryReplication);
        if (!isRunning) {
            isRunning = true;
            this.thread = new Thread(() -> {
                try {
                    boolean done = false;
                    while (!done) {
                        // RETRIEVE LOG ENTRIES TO SEND
                        List<LogEntry> logEntries = null;
                        boolean allEntriesToSendNotSnapshotted = false;
                        int firstIndexToSend = this.nextIndex;

                        while(!allEntriesToSendNotSnapshotted) {
                            try {
                                // the entries to send are the ones from nextIndex to the last one
                                logEntries = log.getEntries(firstIndexToSend, log.getLastLogIndex() + 1);
                                allEntriesToSendNotSnapshotted = true;


                            } catch (SnapshottedEntryException e) { // send snapshot instead of snapshotted entries
                                JSONSnapshot snapshotToSend = log.getJSONSnapshot();
                                try {
                                    this.callInstallSnapshot(snapshotToSend);
                                    firstIndexToSend = snapshotToSend.getLastIncludedIndex() + 1;
                                } catch (ConvertToFollowerException ex) {
                                    notifyFailure();
                                    done = true;
                                }
                            }
                        }

                        if (done) {
                            break;
                        }

                        // CALL APPEND_ENTRIES_RPC ON THE FOLLOWER
                        int prevLogIndex = firstIndexToSend - 1;
                        int prevLogTerm;
                        try {
                            prevLogTerm = this.log.getEntryTerm(prevLogIndex);
                        } catch (SnapshottedEntryException e) {
                            prevLogTerm = this.log.getJSONSnapshot().getLastIncludedTerm();
                        }

                        AppendEntryResult appendEntryResult = proxy.appendEntries(this.term, this.leaderId, prevLogIndex,
                                prevLogTerm, logEntries, this.leaderCommit);

                        if (appendEntryResult.isSuccess()) {
                            // update nextIndex and matchIndex
                            this.nextIndex=  prevLogIndex + logEntries.size() + 1;
                            this.matchIndex =  prevLogIndex + logEntries.size();

                            // notify leader of success
                            notifySuccess();
                            done = true;
                        } else {
                            if (appendEntryResult.getTerm() > this.term) {
                                notifyFailure();
                                done = true;
                            } else {
                                // decrement next index
                                this.nextIndex -= 1;
                            }
                        }
                    }
                } catch (IOException e) {

                }
                this.isRunning = false;
            });
            this.thread.start();
        }
    }

    synchronized void interruptCall() {
        if (this.isRunning && this.thread != null) {
            this.thread.interrupt();
        }
    }

    synchronized int getMatchIndex() {
        return this.matchIndex;
    }

    private void callInstallSnapshot(JSONSnapshot snapshot) throws IOException, ConvertToFollowerException {
        // convert the snapshot object into a byte array
        ByteArrayOutputStream snapshotBytesStream = new ByteArrayOutputStream();
        ObjectOutputStream snapshotObjectStream = new ObjectOutputStream(snapshotBytesStream);
        snapshotObjectStream.writeObject(snapshot);
        snapshotObjectStream.flush();
        byte[] snapshotBytes = snapshotBytesStream.toByteArray();

        // call installSnapshot on the follower
        for (int i = 0; i < snapshotBytes.length / SNAPSHOT_CHUNK_SIZE + 1; i++) {
            int followerTerm = proxy.installSnapshot(this.term, this.leaderId, snapshot.getLastIncludedIndex(), snapshot.getLastIncludedTerm(),
                    i * SNAPSHOT_CHUNK_SIZE, Arrays.copyOfRange(snapshotBytes, i * SNAPSHOT_CHUNK_SIZE, i * (SNAPSHOT_CHUNK_SIZE + 1)),
                    i * (SNAPSHOT_CHUNK_SIZE + 1) >= snapshotBytes.length);
            if (followerTerm > term) {
                throw new ConvertToFollowerException(term);
            }
        }
    }

    private void notifySuccess() {
        for (EntryReplication entryReplication : this.entryReplicationsToNotify) {
            entryReplication.notifySuccessfulReply();
        }
        this.entryReplicationsToNotify.clear();
    }

    private void notifyFailure() {
        for (EntryReplication entryReplication : this.entryReplicationsToNotify) {
            entryReplication.convertToFollower();
        }
        this.entryReplicationsToNotify.clear();
    }

}
