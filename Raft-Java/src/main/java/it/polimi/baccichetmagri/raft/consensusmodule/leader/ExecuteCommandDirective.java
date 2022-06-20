package it.polimi.baccichetmagri.raft.consensusmodule.leader;

enum ExecuteCommandDirective {
    PROCEED,
    INTERRUPT,

    COMMIT
}
