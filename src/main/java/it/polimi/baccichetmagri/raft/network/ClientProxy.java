package it.polimi.baccichetmagri.raft.network;

import it.polimi.baccichetmagri.raft.consensusmodule.ConsensusModule;
import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.messages.ExecuteCommandRequest;
import it.polimi.baccichetmagri.raft.messages.ExecuteCommandReply;
import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.MessageType;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientProxy implements Runnable{

    private final Socket socket;
    private final ConsensusModule consensusModule;

    public ClientProxy(Socket socket, ConsensusModule consensusModule) {
        this.socket = socket;
        this.consensusModule = consensusModule;
        (new Thread(this)).start();
    }

    public void run() {

        Logger logger = Logger.getLogger(ClientProxy.class.getName());
        logger.setLevel(Level.FINE);

        Message message;

        try {

            Scanner in = new Scanner(this.socket.getInputStream());
            String jsonMessage = in.nextLine();
            message = new MessageSerializer().deserialiaze(jsonMessage);

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error in parsing the message from the client. Socket closed" +
                    " and message not processed.");
            this.closeSocket();
            return;
        }

        logger.log(Level.FINE, "Received message from client: " + message);

        if (message.getMessageType() == MessageType.ExecuteCommandRequest) {

            ExecuteCommandResult executeCommandResult = this.consensusModule.executeCommand(
                    ((ExecuteCommandRequest) message).getCommand());

            try {
                PrintWriter out = new PrintWriter(this.socket.getOutputStream());
                out.println(new ExecuteCommandReply(executeCommandResult, message.getMessageId()));
            } catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, "Error in sending the response to the client. Socket closed.");
                this.closeSocket();
                return;
            }

        }

        this.closeSocket();
    }

    private void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
