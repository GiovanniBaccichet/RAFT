package it.polimi.baccichetmagri.raft.consensusmodule;

import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.io.IOException;

public class ConsensusPersistentState {



    // currentTerm: latest term server has seen (initialized to 0 on first boot, increases monotonically)

    // votedFor: candidateId that received vote in current term (or null if none)

    public int getCurrentTerm() throws IOException {
        ConsensusPersistentStateForGson state = this.readState();
        return state.getCurrentTerm();
    }

    public void setCurrentTerm(int currentTerm) throws IOException {
        ConsensusPersistentStateForGson state = this.readState();
        state.setCurrentTerm(currentTerm);
        this.writeState(state);
    }

    public Integer getVotedFor() throws IOException {
        ConsensusPersistentStateForGson state = this.readState();
        return state.getVotedFor();
    }

    public void setVotedFor(Integer votedFor) throws IOException {
        ConsensusPersistentStateForGson state = this.readState();
        state.setVotedFor(votedFor);
        this.writeState(state);
    }

    private ConsensusPersistentStateForGson readState() throws IOException {
        return JsonFilesHandler.read("consensus_persistent_state.json", ConsensusPersistentStateForGson.class);
    }

    private void writeState(ConsensusPersistentStateForGson state) throws IOException {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Writing state on file");
        JsonFilesHandler.write("consensus_persistent_state.json", state);
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






