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
import java.util.Arrays;
import java.util.List;

class AppendEntriesCall {

    private static final int SNAPSHOT_CHUNK_SIZE = 5*1024; // Send chunks of 5 KB at a time, this parameter needs to be tuned wrt network and storage
    private int nextIndex; // index of the next log entry to send to that server (initialized to leader last log index + 1)
    private int matchIndex; // index of the highest log entry known to be replicated on server (initialized to 0, increases monotonically)
    private final ConsensusModuleProxy proxy;
    private Thread thread;
    private boolean isRunning;

    AppendEntriesCall(int nextIndex, int matchIndex, ConsensusModuleProxy proxy) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.proxy = proxy;
    }

    synchronized void callAppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, Log log, int leaderCommit, IndexesToCommit indexesToCommit) {
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
                                    this.callInstallSnapshot(term, leaderID, prevLogIndex, snapshotToSend, indexesToCommit);
                                    firstIndexToSend = snapshotToSend.getLastIncludedIndex() + 1;
                                } catch (ConvertToFollowerException ex) {
                                    // TODO: CONVERT LEADER TO FOLLOWER -> NEED SYNCHRONIZATION MECHANISM
                                }
                            }
                        }

                        // CALL APPEND_ENTRIES_RPC ON THE FOLLOWER
                        AppendEntryResult appendEntryResult = proxy.appendEntries(term, leaderID, prevLogIndex, prevLogTerm, logEntries, leaderCommit);
                        if (appendEntryResult.isSuccess()) {
                            // update nextIndex and matchIndex
                            this.nextIndex=  prevLogIndex + logEntries.size() + 1;
                            this.matchIndex =  prevLogIndex + logEntries.size();
                            for (int i = prevLogIndex + 1; i < prevLogIndex + logEntries.size() + 1; i++) {
                                // TODO: NOTIFY LEADER OF SUCCESS
                            }
                            done = true;
                        } else {
                            // decrement next index
                            this.nextIndex -= 1;
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

    private void callInstallSnapshot(int term, int leaderID, int prevLogIndex, JSONSnapshot snapshot, IndexesToCommit indexesToCommit) throws IOException, ConvertToFollowerException {
        // convert the snapshot object into a byte array
        ByteArrayOutputStream snapshotBytesStream = new ByteArrayOutputStream();
        ObjectOutputStream snapshotObjectStream = new ObjectOutputStream(snapshotBytesStream);
        snapshotObjectStream.writeObject(snapshot);
        snapshotObjectStream.flush();
        byte[] snapshotBytes = snapshotBytesStream.toByteArray();

        // call installSnapshot on the follower
        for (int i = 0; i < snapshotBytes.length / SNAPSHOT_CHUNK_SIZE + 1; i++) {
            int followerTerm = proxy.installSnapshot(term, leaderID, snapshot.getLastIncludedIndex(), snapshot.getLastIncludedTerm(),
                    i * SNAPSHOT_CHUNK_SIZE, Arrays.copyOfRange(snapshotBytes, i * SNAPSHOT_CHUNK_SIZE, i * (SNAPSHOT_CHUNK_SIZE + 1)),
                    i * (SNAPSHOT_CHUNK_SIZE + 1) >= snapshotBytes.length);
            if (followerTerm > term) {
                throw new ConvertToFollowerException(term);
            }
        }

        // notify leader of success of installed snapshot
        // TODO
    }

}
