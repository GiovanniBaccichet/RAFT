package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.io.IOException;
import java.util.List;

public interface ConsensusModuleInterface {

    /**
     * Invoked by candidates to gather votes.
     * @param term candidate’s term
     * @param candidateID candidate requesting vote
     * @param lastLogIndex index of candidate’s last log entry
     * @param lastLogTerm term of candidate’s last log entry
     * @return a VoteReply containing the current term and a boolean, true if candidate received vote
     */
    VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException, InterruptedException;

    /**
     * Invoked by leader to replicate log entries; also used as heartbeat.
     * @param term leader’s term
     * @param leaderID so follower can redirect clients
     * @param prevLogIndex index of log entry immediately preceding new ones
     * @param prevLogTerm term of prevLogIndex entry
     * @param logEntries log entries to store (empty for heartbeat; may send more than one for efficiency)
     * @param leaderCommit leader’s commitIndex
     * @return an AppendEntryReply containing the current term and a boolean, true if follower contained entry matching prevLogIndex and prevLogTerm
     */
    AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm,
                                    List<LogEntry> logEntries, int leaderCommit) throws IOException, InterruptedException;

    ExecuteCommandResult executeCommand(Command command) throws IOException;

    /**
     * Invoked by the leader to send chunks of a snapshot.json to a follower. Leaders always send chunks in order
     * @param term leader's term
     * @param leaderID needed by followers to redirect clients
     * @param lastIncludedIndex the snapshot.json replaces all entities up through and including this Index
     * @param lastIncludedTerm term of the last included Index of the Log
     * @param offset byte offset where chunk, starting at offset
     * @param data raw bytes where snapshot.json chunk, starting at offset
     * @param done TRUE if this is the last chunk
     * @return currentTerm, for leader to update itself
     */
    int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException;

    int getId();
}
