package it.polimi.baccichetmagri.raft.consensusmodule.returntypes;

import it.polimi.baccichetmagri.raft.machine.StateMachineResult;
import it.polimi.baccichetmagri.raft.machine.StateMachineResultImplementation;

public class ExecuteCommandResult {
    private final StateMachineResultImplementation stateMachineResult;
    private final boolean valid;
    private final String leaderIP;

    public ExecuteCommandResult(StateMachineResultImplementation stateMachineResult, boolean valid, String leaderIP) {
        this.stateMachineResult = stateMachineResult;
        this.valid = valid;
        this.leaderIP = leaderIP;
    }

    public StateMachineResult getStateMachineResult() {
        return stateMachineResult;
    }

    public boolean isValid() {
        return valid;
    }

    public String getLeaderIP() {
        return leaderIP;
    }
}
