package it.polimi.baccichetmagri.raft;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainerImpl;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.configuration.ConfigurationImpl;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static Log log;

    private static Logger logger;

    public static void main(String[] args) {
        logger = Logger.getLogger(Server.class.getName());
        logger.setLevel(Level.FINE);
        try {
            int id = Integer.parseInt(args[0]);
            StateMachine stateMachine = new StateMachineImplementation();
            log = new Log(Path.of(Log.LOG_FILENAME), stateMachine);
            Configuration configuration = new ConfigurationImpl();
            ConsensusModuleContainer consensusModuleContainer = new ConsensusModuleContainerImpl(id, configuration, log, stateMachine);
            configuration.initialize(id, consensusModuleContainer);
            ServerSocketManager serverSocketManager = new ServerSocketManager(configuration, consensusModuleContainer);
            new Thread(serverSocketManager).start();
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid ID argument, please insert an integer number.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Impossible to open the server socket.");
            e.printStackTrace();
        }
    }

    /**
     * Used to shut down server, closing also the Log filechannel
     */
    public static void shutDown() {
        try {
            log.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Impossible to close the log.");
        }
        System.exit(0);
    }

}
