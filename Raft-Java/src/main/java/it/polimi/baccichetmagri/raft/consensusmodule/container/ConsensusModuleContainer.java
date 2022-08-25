package it.polimi.baccichetmagri.raft.consensusmodule.container;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.AppendEntryResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.VoteResult;
import it.polimi.baccichetmagri.raft.log.LogEntry;
import it.polimi.baccichetmagri.raft.machine.Command;

import java.io.IOException;
import java.util.List;

public abstract class ConsensusModuleContainer implements ConsensusModuleInterface {
    @Override
    public abstract  VoteResult requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws IOException, InterruptedException;

    @Override
    public abstract AppendEntryResult appendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<LogEntry> logEntries, int leaderCommit) throws IOException, InterruptedException;

    @Override
    public abstract ExecuteCommandResult executeCommand(Command command) throws IOException;

    @Override
    public abstract int installSnapshot(int term, int leaderID, int lastIncludedIndex, int lastIncludedTerm, int offset, byte[] data, boolean done) throws IOException;

    public abstract int getId();

    public abstract void changeConsensusModuleImpl(ConsensusModule consensusModule);
}
