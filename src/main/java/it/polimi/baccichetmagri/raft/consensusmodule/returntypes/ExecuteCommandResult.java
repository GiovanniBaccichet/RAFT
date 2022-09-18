package it.polimi.baccichetmagri.raft.consensusmodule.returntypes;

import it.polimi.baccichetmagri.raft.machine.StateMachineResult;

public class ExecuteCommandResult {
    private final StateMachineResult stateMachineResult;
    private final boolean valid;
    private final String leaderIP;

    public ExecuteCommandResult(StateMachineResult stateMachineResult, boolean valid, String leaderIP) {
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
