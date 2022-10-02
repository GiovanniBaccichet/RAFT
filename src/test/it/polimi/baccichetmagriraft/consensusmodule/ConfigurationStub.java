package it.polimi.baccichetmagriraft.consensusmodule;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModuleInterface;
import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationStub extends Configuration {

    private List<ConsensusModuleStub> consensusModuleStubs;

    public ConfigurationStub(List<ConsensusModuleStub> stubs) {
        this.consensusModuleStubs = stubs;
    }

    @Override
    public void initialize(int id, ConsensusModuleContainer consensusModuleContainer) {

    }

    @Override
    public ConsensusModuleProxy getConsensusModuleProxy(int id) throws NoSuchProxyException {
        return null;
    }

    @Override
    public Iterator<ConsensusModuleInterface> getIteratorOnAllProxies() {
        List<ConsensusModuleInterface> cModules = new ArrayList<>(this.consensusModuleStubs);
        return cModules.listIterator();
    }

    @Override
    public int getServersNumber() {
        return this.consensusModuleStubs.size() + 1;
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
