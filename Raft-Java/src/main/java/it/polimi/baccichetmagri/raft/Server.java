package it.polimi.baccichetmagri.raft;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.log.Log;
import it.polimi.baccichetmagri.raft.machine.StateMachine;
import it.polimi.baccichetmagri.raft.machine.StateMachineImplementation;
import it.polimi.baccichetmagri.raft.network.Configuration;
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
            log = new Log(Path.of("log"), stateMachine); // Log saved to the current user's home folder (user.dir)
            Configuration configuration = new Configuration();
            ConsensusModule consensusModule = new ConsensusModule(id, configuration, log, stateMachine);
            configuration.initialize(id, consensusModule);
            ServerSocketManager serverSocketManager = new ServerSocketManager(configuration, consensusModule);
            serverSocketManager.run();
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid ID argument, please insert an integer number.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Impossible to open the server socket.");
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
