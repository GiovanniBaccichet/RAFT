package it.polimi.baccichetmagri.raft.network;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ServerSocketManager implements Runnable{

    public static final int PORT = 43827;

    private ServerSocket serverSocket;
    private Configuration configuration;
    private Gson gson;

    public ServerSocketManager(Configuration configuration) throws IOException {
        this.serverSocket = new ServerSocket(PORT);
        this.configuration = configuration;
        this.gson = new Gson();
        (new Thread(this)).start();
    }

    public void run() {
        while (true) {
            try {
                Socket socket = this.serverSocket.accept();
                Scanner in = new Scanner(socket.getInputStream());
                String message = in.nextLine();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
