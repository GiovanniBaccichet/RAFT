package it.polimi.baccichetmagriraft.consensusmodule.follower;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;

import java.util.Iterator;

class FollowerTestConfiguration extends Configuration {
    @Override
    public void initialize(int id, ConsensusModuleContainer consensusModuleContainer) {

    }

    @Override
    public ConsensusModuleProxy getConsensusModuleProxy(int id) throws NoSuchProxyException {
        return null;
    }

    @Override
    public Iterator<ConsensusModuleProxy> getIteratorOnAllProxies() {
        return null;
    }

    @Override
    public int getServersNumber() {
        return 0;
    }

    @Override
    public void changeConfiguration(String configurationJson) {

    }

    @Override
    public String getLeaderIP() {
        return null;
    }

    @Override
    public void setLeader(Integer leaderId) {

    }

    @Override
    public void discardAppendEntryReplies(boolean discard) {

    }

    @Override
    public void discardRequestVoteReplies(boolean discard) {

    }

    @Override
    public void discardInstallSnapshotReplies(boolean discard) {

    }

    @Override
    public String getIp() {
        return null;
    }
}
