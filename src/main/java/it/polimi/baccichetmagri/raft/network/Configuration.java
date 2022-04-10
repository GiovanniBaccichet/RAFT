package it.polimi.baccichetmagri.raft.network;

import com.google.gson.reflect.TypeToken;
import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.network.exceptions.NoSuchProxyException;
import it.polimi.baccichetmagri.raft.utils.ResourcesLoader;

import java.util.*;

/**
 * A configuration is the set of servers participating in the Raft algorithm.
 */
public class Configuration {

    private List<ConsensusModuleProxy> proxies = new ArrayList<>();

    /**
     * @param id the id of the ConsensusModule of the machine (to not create a connection with itself)
     */
    public Configuration(int id, ConsensusModule consensusModule) {

        // load configuration file with <id, ip> of other servers
        Map<Integer, String> addresses = ResourcesLoader.loadJson("configuration.json",
                new TypeToken<Map<Integer, String>>() {}.getType());

        // create list of proxies
        for (Map.Entry<Integer, String> address : addresses.entrySet()) {
            if (address.getKey() != id) {
                this.proxies.add(new ConsensusModuleProxy(address.getKey(), address.getValue(), consensusModule));
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

    public void changeConfiguration(String configurationJson) {

    }


}
