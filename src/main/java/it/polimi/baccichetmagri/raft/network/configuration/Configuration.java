package it.polimi.baccichetmagri.raft.network.configuration;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.network.proxies.ConsensusModuleProxy;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;

import java.util.Iterator;

public abstract class Configuration {
    public abstract void initialize(int id, ConsensusModuleContainer consensusModuleContainer);

    public abstract ConsensusModuleProxy getConsensusModuleProxy(int id) throws NoSuchProxyException;

    public abstract Iterator<ConsensusModuleProxy> getIteratorOnAllProxies();

    public abstract int getServersNumber();

    public abstract void changeConfiguration(String configurationJson);

    public abstract String getLeaderIP();

    public abstract void setLeader(Integer leaderId);

    public abstract void discardAppendEntryReplies(boolean discard);

    public abstract void discardRequestVoteReplies(boolean discard);

    public abstract void discardInstallSnapshotReplies(boolean discard);

    public abstract String getIp();
}
