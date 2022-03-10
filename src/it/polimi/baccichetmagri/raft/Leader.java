package it.polimi.baccichetmagri.raft;

import java.util.Map;

public class Leader extends ConsensusModule {

    private Map<Integer, Integer> nextIndex;
    private Map<Integer, Integer> matchIndex;

}
