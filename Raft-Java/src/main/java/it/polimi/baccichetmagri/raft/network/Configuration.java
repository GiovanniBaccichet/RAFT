package it.polimi.baccichetmagri.raft.network;

import com.google.gson.reflect.TypeToken;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;
import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.util.*;

/**
 * A configuration is the set of servers participating in the Raft algorithm.
 */
public class Configuration {

    private final List<ConsensusModuleProxy> proxies = new ArrayList<>();
    private Integer leaderId;
    private String ip; // ip of this machine

    /**
     * @param id the id of the ConsensusModuleImpl of the machine (to not create a connection with itself)
     */
    public void initialize(int id, ConsensusModule consensusModule) {

        // load configuration file with <id, ip> of other servers
        Map<Integer, String> addresses = null;
        try {
            addresses = JsonFilesHandler.read("configuration.json",
                    new TypeToken<Map<Integer, String>>() {}.getType());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        // create list of proxies
        for (Map.Entry<Integer, String> address : addresses.entrySet()) {
            if (address.getKey() != id) {
                this.proxies.add(new ConsensusModuleProxy(address.getKey(), address.getValue(), consensusModule));
            } else {
                this.ip = address.getValue();
            }
        }
    }

    public ConsensusModuleProxy getConsensusModuleProxy(int id) throws NoSuchProxyException {
        return this.proxies.stream().filter(proxy -> proxy.getId() == id).findAny().
                orElseThrow(() -> {throw new NoSuchProxyException("no proxy with id " + id);});
    }

    public Iterator<ConsensusModuleProxy> getIteratorOnAllProxies() {
        return this.proxies.listIterator();
    }

    public int getServersNumber() {
        return this.proxies.size() + 1; // includes also this server
    }

    public void changeConfiguration(String configurationJson) {

    }

    public String getLeaderIP() {
        if (this.leaderId == null) {
            return null;
        }
        return this.getConsensusModuleProxy(this.leaderId).getIp();
    }

    public void setLeader(Integer leaderId) {
        this.leaderId = leaderId;
    }

    public void discardAppendEntryReplies(boolean discard) {
        for (ConsensusModuleProxy proxy : this.proxies) {
            proxy.discardAppendEntryReplies(discard);
        }
    }

    public void discardRequestVoteReplies(boolean discard) {
        for (ConsensusModuleProxy proxy : this.proxies) {
            proxy.discardVoteReplies(discard);
        }
    }

    public String getIp() {
        return ip;
    }
}
