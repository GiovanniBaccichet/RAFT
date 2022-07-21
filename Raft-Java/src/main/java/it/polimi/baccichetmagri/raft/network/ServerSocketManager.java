package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;

import java.io.IOException;
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
    private final ConsensusModule consensusModule;
    private final Logger logger;

    public ServerSocketManager(Configuration configuration, ConsensusModule consensusModule) throws IOException {
        this.serverSocket = new ServerSocket(RAFT_PORT);
        this.configuration = configuration;
        this.consensusModule = consensusModule;
        this.logger = Logger.getLogger(ServerSocketManager.class.getName());
    }

    /**
     * Receives messages from the network and creates or initializes the proxies corresponding to the new connections.
     */
    public void run() {
        while (true) {
            Socket socket = null;
            try {
                // accept new connections and read the first message
                socket = this.serverSocket.accept();
                Scanner in = new Scanner(socket.getInputStream());
                String connectMessage = in.nextLine();

                if (connectMessage.contains("SERVER")) {
                    // if the message is "SERVER X", where x is an integer number, the connection is requested from
                    // the server with id = X
                    int id = Integer.parseInt(connectMessage.substring(7));
                    this.logger.log(Level.FINE, "Received connection with server " + id);
                    this.configuration.getConsensusModuleProxy(id).setSocket(socket);
                } else if (connectMessage.equals("CLIENT")){
                    //if the message is "CLIENT", the connection is requested from a client
                    ClientProxy clientProxy = new ClientProxy(socket, this.consensusModule);
                    this.logger.log(Level.FINE, "Received connection with client");
                    clientProxy.run();
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
