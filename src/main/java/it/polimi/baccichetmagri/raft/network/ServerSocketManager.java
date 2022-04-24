package it.polimi.baccichetmagri.raft.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSocketManager implements Runnable{

    public static final int PORT = 43827;

    private ServerSocket serverSocket;
    private Configuration configuration;
    private MessageSerializer messageSerializer;

    public ServerSocketManager(Configuration configuration) throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.configuration = configuration;
        this.messageSerializer = new MessageSerializer();
    }

    public void run() {
        while (true) {
            try {
                // accept new connections and read the first message
                Socket socket = this.serverSocket.accept();
                Scanner in = new Scanner(socket.getInputStream());
                String connectMessage = in.nextLine();

                if (connectMessage.contains("SERVER")) {
                    // if the message is "SERVER X", where x is an integer number, the connection is requested from
                    // the server with id = X
                    int id = Integer.parseInt(connectMessage.substring(7));
                    this.configuration.getConsensusModuleProxy(id).setSocket(socket);
                } else if (connectMessage.equals("CLIENT")){
                    //if the message is "CLIENT", the connection is requested from a client
                    ClientProxy clientProxy = new ClientProxy(socket);
                } else {
                    socket.close();
                }
            } catch (IOException e) { // thrown by serverSocket.accept()
                e.printStackTrace();
            } catch (NumberFormatException e) { // thrown by Integer.parseInt()
                e.printStackTrace();
            }
        }
    }
}
