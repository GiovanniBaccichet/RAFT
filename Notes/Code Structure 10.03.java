package it.polimi.magri.chiara.raft;

import java.util.List;
import java.util.Map;

class Client {
    public static void main() {

    }
}

class Server {
    private ConsensusModule consensusModule = new Follower();
    private Log log /* = new LogImpl() */;
    private StateMachine stateMachine /* = new StateMachineImpl() */;

    public static void main() {

    }

    public void setConsensusModule(ConsensusModule consensusModule) {
        
    }
}

abstract class ConsensusModule {

    protected ConsensusPersistentState persistentState = new ConsensusPersistentState();
    protected int commitIndex = 0;
    protected int lastApplied = 0;
    protected List<Integer> otherServers;
    protected Log log;

    public void requestVote() {

    }

    public abstract void appendEntries();

    public ConsensusModule method() {

    }
}

class Leader extends ConsensusModule {

    Map<Integer, Integer> nextIndex;
    Map<Integer, Integer> matchIndex;

    @Override
    public void requestVote() {

    }

    @Override
    public void appendEntries() {

    }
}

class Follower extends ConsensusModule {
    @Override
    public void requestVote() {

    }

    @Override
    public void appendEntries() {

    }

    public ConsensusModule method() {
        return new Candidate();
    }
}

class Context {
    private ConsensusModule consensusModule;
    public void m() {
        this.consensusModule = consensusModule.method();
    }
}

class Candidate extends ConsensusModule {
    @Override
    public void requestVote() {

    }

    @Override
    public void appendEntries() {

    }
}

abstract class Log {
    public abstract void appendEntry(LogEntry logEntry);
}

abstract class StateMachine {
    public abstract void executeCommand(Command command);
}

abstract class LogEntry {
    Command command;
    int term;

    @Override
    public String toString() {
        return null;
    }
}

abstract class Command {

}

class ConsensusPersistentState { // handles file reading and writing
    public int getCurrentTerm() {
        return 0;
    }
    public void writeCurrentTerm(int currentTerm) {

    }
    public int getVotedFor() { // anziché usare int si può fare una classe ConsensusId
        return 0;
    }
    public void writeVotedFor(int candidateId) {

    }
}