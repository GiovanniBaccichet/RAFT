package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSocketManager implements Runnable{

    public static final int PORT = 43827;

    private final ServerSocket serverSocket;
    private final Configuration configuration;
    private final ConsensusModule consensusModule;

    public ServerSocketManager(Configuration configuration, ConsensusModule consensusModule) throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.configuration = configuration;
        this.consensusModule = consensusModule;
    }

    public void run() { // TODO: running on main thread, no need to call method run() -> change name
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
                    this.configuration.getConsensusModuleProxy(id).setSocket(socket);
                } else if (connectMessage.equals("CLIENT")){
                    //if the message is "CLIENT", the connection is requested from a client
                    ClientProxy clientProxy = new ClientProxy(socket, this.consensusModule);
                } else {
                    socket.close();
                }
            } catch (IOException e) { // thrown by serverSocket.accept()
                e.printStackTrace();
            } catch (NumberFormatException e) { // thrown by Integer.parseInt()
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
