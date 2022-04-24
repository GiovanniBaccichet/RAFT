package it.polimi.baccichetmagri.raft;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import it.polimi.baccichetmagri.raft.network.Configuration;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Server.class.getName());
        logger.setLevel(Level.FINE);
        try {
            int id = Integer.parseInt(args[0]);
            Log log = new Log();
            StateMachine stateMachine = new StateMachineImplementation();
            Configuration configuration = new Configuration();
            ConsensusModule consensusModule = new ConsensusModule(id, configuration, log, stateMachine);
            configuration.initialize(id, consensusModule);
            ServerSocketManager serverSocketManager = new ServerSocketManager(configuration);
            serverSocketManager.run();
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid ID argument, please insert an integer number");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Impossible to open the server socket.");
        }
    }

}
