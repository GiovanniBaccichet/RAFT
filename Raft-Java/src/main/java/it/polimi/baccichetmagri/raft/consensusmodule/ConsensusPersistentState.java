package it.polimi.baccichetmagri.raft.consensusmodule;

import com.google.gson.Gson;
import it.polimi.baccichetmagri.raft.utils.ResourcesLoader;

class ConsensusPersistentState { // TODO



    // currentTerm: latest term server has seen (initialized to 0 on first boot, increases monotonically)

    // votedFor: candidateId that received vote in current term (or null if none)

    int getCurrentTerm() {
        ConsensusPersistentStateForGson state = this.loadState();
        return state.getCurrentTerm();
    }

    void setCurrentTerm(int currentTerm) {
        ConsensusPersistentStateForGson state = this.loadState();
        state.setCurrentTerm(currentTerm);
        this.writeState(state);
    }

    Integer getVotedFor() {
        ConsensusPersistentStateForGson state = this.loadState();
        return state.getVotedFor();
    }

    void setVotedFor(Integer votedFor) {
        ConsensusPersistentStateForGson state = this.loadState();
        state.setVotedFor(votedFor);
        this.writeState(state);
    }

    private ConsensusPersistentStateForGson loadState() {
        return ResourcesLoader.loadJson("consensus_persistent_state.json", ConsensusPersistentStateForGson.class);
    }

    private void writeState(ConsensusPersistentStateForGson state) {
        ResourcesLoader.write("consensus_persistent_state.json", new Gson().toJson(state));
    }



    private static class ConsensusPersistentStateForGson {
        private int currentTerm;
        private Integer votedFor;

        ConsensusPersistentStateForGson(int currentTerm, Integer votedFor) {
            this.currentTerm = currentTerm;
            this.votedFor = votedFor;
        }

        public int getCurrentTerm() {
            return currentTerm;
        }

        public Integer getVotedFor() {
            return votedFor;
        }

        public void setCurrentTerm(int currentTerm) {
            this.currentTerm = currentTerm;
        }

        public void setVotedFor(Integer votedFor) {
            this.votedFor = votedFor;
        }
    }
}






