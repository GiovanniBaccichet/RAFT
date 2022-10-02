package it.polimi.baccichetmagriraft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.io.IOException;
import java.util.List;

public class ConsensusModuleStub implements ConsensusModuleInterface {
    private final boolean success;
    private final int term;

    private final int delay;
    private boolean firstCall;

    public ConsensusModuleStub(boolean success, int term, int delay) {
        this.success = success;
        this.term = term;
        this.delay = delay;
        this.firstCall = true;
    }
    public ConsensusModuleStub(boolean success, int term) {
        this(success, term, 0);
    }


    @Override
    public VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException, InterruptedException {
        try {
            if (this.delay > 0 && this.firstCall) {
                this.firstCall = false;
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

        return new VoteResult(this.term, this.success);
    }

    @Override
    public AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) throws IOException, InterruptedException {
        return new AppendEntryResult(this.term, this.success);
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
