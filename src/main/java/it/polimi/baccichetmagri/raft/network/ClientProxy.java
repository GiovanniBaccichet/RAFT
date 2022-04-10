package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.machine.Command;

import java.net.Socket;

public class ClientProxy implements Runnable{

    private Socket socket;

    public ClientProxy(Socket socket) {
        this.socket = socket;
        (new Thread(this)).start();
    }

    public void run() {

    }
}
