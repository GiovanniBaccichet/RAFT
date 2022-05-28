package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.StateMachineResult;

import java.io.IOException;

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
                                    LogEntry[] logEntries, int leaderCommit) throws IOException;

    ExecuteCommandResult executeCommand(Command command) throws IOException;
}
