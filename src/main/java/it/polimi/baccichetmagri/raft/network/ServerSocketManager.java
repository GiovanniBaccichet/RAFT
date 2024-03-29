package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.container.ConsensusModuleContainer;
import it.polimi.baccichetmagri.raft.network.configuration.Configuration;
import it.polimi.baccichetmagri.raft.network.proxies.ClientProxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that encapsulates the TCP server socket.
 * It receives the first message from a new connection and creates (in the case of a client) or initializes
 * (in the case of a server) the corresponding proxy object.
 * If the message is "SERVER x", where x is an integer number, the new connection is received from the server with id x;
 * if the message is client the new connection is with a client; otherwise, the new connection is rejected.
 */
public class ServerSocketManager implements Runnable{

    public static final int RAFT_PORT = 43827;
    private final ServerSocket serverSocket;
    private final Configuration configuration;
    private final ConsensusModuleContainer consensusModuleContainer;
    private final Logger logger;

    public ServerSocketManager(Configuration configuration, ConsensusModuleContainer consensusModuleContainer) throws IOException {
        this.serverSocket = new ServerSocket(RAFT_PORT);
        this.configuration = configuration;
        this.consensusModuleContainer = consensusModuleContainer;
        this.logger = Logger.getLogger(ServerSocketManager.class.getName());
    }

    /**
     * Receives messages from the network and creates or initializes the proxies corresponding to the new connections.
     */
    public void run() {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Running the socket on port " + RAFT_PORT);
        while (true) {
            Socket socket = null;
            try {
                // accept new connections and read the first message
                socket = this.serverSocket.accept();
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "Accepted new connection");
                Scanner in = new Scanner(socket.getInputStream());
                String connectMessage = in.nextLine();
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "Received connect message: " + connectMessage);

                if (connectMessage.contains("SERVER")) {
                    // if the message is "SERVER X", where x is an integer number, the connection is requested from
                    // the server with id = X
                    int id = Integer.parseInt(connectMessage.substring(7));
                    Socket finalSocket = socket;
                    (new Thread(() -> this.configuration.getConsensusModuleProxy(id).receiveMethodCall(finalSocket))).start();
                } else if (connectMessage.equals("CLIENT")){
                    //if the message is "CLIENT", the connection is requested from a client
                    new ClientProxy(socket, this.consensusModuleContainer);
                    this.logger.log(Level.WARNING, "Received connection with client");
                    System.out.println("[" + this.getClass().getSimpleName() + "] " + "Client connected");
                } else {
                    socket.close();
                    this.logger.log(Level.WARNING, "Connection refused. Invalid message: " + connectMessage);
                }
            } catch (NumberFormatException | IOException e) { // thrown by Integer.parseInt()
                e.printStackTrace();
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
