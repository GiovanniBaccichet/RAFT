package it.polimi.baccichetmagriraft.consensusmodule.candidate;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.io.IOException;
import java.util.List;

public class ConsensusModuleStub implements ConsensusModuleInterface {
    boolean voteGranted;
    int term;

    ConsensusModuleStub(boolean voteGranted, int term) {
        this.voteGranted = voteGranted;
        this.term = term;
    }

    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException, InterruptedException {
        return new VoteResult(this.term, this.voteGranted);
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) throws IOException, InterruptedException {
        return null;
    }

    @Override
    public ExecuteCommandResult executeCommand(Command command) throws IOException {
        return null;
    }

    @Override
    public int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException {
        return 0;
    }

    @Override
    public int getId() {
        return 0;
    }
}
