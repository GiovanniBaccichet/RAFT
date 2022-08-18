package it.polimi.baccichetmagri.raft.network;

import com.google.gson.reflect.TypeToken;
import it.polimi.baccichetmagri.raft.Server;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;
import it.polimi.baccichetmagri.raft.utils.JsonFilesHandler;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A configuration is the set of servers participating in the Raft algorithm.
 */
public class Configuration {

    private final List<ConsensusModuleProxy> proxies = new ArrayList<>();
    private Integer leaderId;
    private String ip; // ip of this machine
    private Logger logger;

    private ConsensusModule consensusModule;

    /**
     * Creates the proxies of all other servers participating in the Raft algorithm.
     * @param id the id of the ConsensusModuleImpl of the machine (to not create a connection with itself)
     */
    public void initialize(int id, ConsensusModule consensusModule) {
        this.logger = Logger.getLogger(Configuration.class.getName());
        this.consensusModule = consensusModule;

        Map<Integer, String> addresses;
        try {
            // load configuration file with <id, ip> of other servers
            addresses = JsonFilesHandler.read("configuration.json",new TypeToken<Map<Integer, String>>() {}.getType());
            // create list of proxies
            for (Map.Entry<Integer, String> address : addresses.entrySet()) {
                if (address.getKey() != id) {
                    this.proxies.add(new ConsensusModuleProxy(address.getKey(), address.getValue(), consensusModule));
                } else {
                    this.ip = address.getValue();
                }
            }
        } catch (java.io.IOException e) {
            this.logger.log(Level.SEVERE, "Impossible to open the configuration file.");
            e.printStackTrace();
            Server.shutDown();
        }
    }

    /**
     * Returns the proxy corresponding to the provided id.
     * @param id an integer representing the id of the proxy to return
     * @return the proxy corresponding to the provided id
     * @throws NoSuchProxyException if the id provided does not correspond to any server proxy
     */
    public ConsensusModuleProxy getConsensusModuleProxy(int id) throws NoSuchProxyException {
        return this.proxies.stream().filter(proxy -> proxy.getId() == id).findAny().
                orElseThrow(() -> {throw new NoSuchProxyException("no proxy with id " + id);});
    }

    /**
     * Returns an iterator over the collection of all ConsensusModuleProxies.
     * @return an iterator over the collection of all ConsensusModuleProxies
     */
    public Iterator<ConsensusModuleProxy> getIteratorOnAllProxies() {
        return this.proxies.listIterator();
    }

    /**
     * Returns the number of servers participating in the Raft algorithm, even if they are down or not connected with this
     * server.
     * @return the number of servers participating in the Raft algorithm
     */
    public int getServersNumber() {
        return this.proxies.size() + 1; // includes also this server
    }

    public void changeConfiguration(String configurationJson) {
        // TODO se c'è tempo (e voglia), sennò togliere
    }

    /**
     * Returns the IP address of the leader server or null if the leader is not known.
     * @return the IP address of the leader server or null if the leader is not known
     */
    public String getLeaderIP() {
        if (this.leaderId == null) {
            return null;
        } else if (this.leaderId == this.consensusModule.getId()) {
            return this.ip;
        } else {
            return this.getConsensusModuleProxy(this.leaderId).getIp();
        }
    }

    /**
     * Sets as leader id the provided one
     * @param leaderId the id of the leader server, or null if the leader is not known
     */
    public void setLeader(Integer leaderId) {
        this.leaderId = leaderId;
    }

    /**
     * If discard is true, makes all server proxies discard the AppendEntryReply messages;
     * if discard is false, makes all server proxies process the AppendEntryReply messages
     * @param discard a boolean that tells if discard or process the AppendEntryReply messages
     */
    public void discardAppendEntryReplies(boolean discard) {
        for (ConsensusModuleProxy proxy : this.proxies) {
            proxy.discardAppendEntryReplies(discard);
        }
    }

    /**
     * If discard is true, makes all server proxies discard the RequestVoteReply messages;
     * if discard is false, makes all server proxies process the RequestVoteReply messages
     * @param discard a boolean that tells if discard or process the RequestVoteReply messages
     */
    public void discardRequestVoteReplies(boolean discard) {
        for (ConsensusModuleProxy proxy : this.proxies) {
            proxy.discardVoteReplies(discard);
        }
    }

    public void discardInstallSnapshotReplies(boolean discard) {
        for (ConsensusModuleProxy proxy : this.proxies) {
            proxy.discardInstallSnapshotReplies(discard);
        }
    }

    /**
     * Returns the IP address of the machine running this instance of the RAFT algorithm.
     * @return the IP address of the machine running this instance of the RAFT algorithm
     */
    public String getIp() {
        return ip;
    }
}
