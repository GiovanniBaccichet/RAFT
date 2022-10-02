package it.polimi.baccichetmagri.raft;

import it.polimi.baccichetmagri.raft.consensusmodule.returntypes.ExecuteCommandResult;
import it.polimi.baccichetmagri.raft.machine.Command;
import it.polimi.baccichetmagri.raft.machine.CommandImplementation;
import it.polimi.baccichetmagri.raft.messages.ExecuteCommandReply;
import it.polimi.baccichetmagri.raft.messages.ExecuteCommandRequest;
import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.messages.MessageType;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Raft client we provide is simply a program that receives commands from the terminal and sends them to the Raft cluster with
 * ExecuteCommand RPCs. As first IP to contact, it uses the first program argument provided.
 * Commands have to be written as "COMMAND x", where x is the number to add to the integer in the toy state machine used as an example.
 * To exit, type EXIT.
 */
public class Client {

    public static String COMMAND = "COMMAND";
    public static String EXIT = "EXIT";

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Client.class.getName());
        logger.setLevel(Level.FINE);

        Socket socket = null;
        try {
            MessageSerializer messageSerializer = new MessageSerializer();
            int messageIdToSend = 1;
            Scanner scanner = new Scanner(System.in);
            String leaderIp = args[0];

            while(true) {
                // read command from terminal
                System.out.println("Insert a command");
                String commandString = scanner.nextLine();

                if (commandString.startsWith(COMMAND)) {
                    boolean done = false;
                    while (!done) {
                        // open connection with the (supposed) leader
                        socket = new Socket(leaderIp, ServerSocketManager.RAFT_PORT);
                        Scanner in = new Scanner(socket.getInputStream());
                        PrintWriter out = new PrintWriter(socket.getOutputStream());

                        // send execute command request
                        Command command = new CommandImplementation(Integer.parseInt(commandString.substring(COMMAND.length() + 1)));
                        logger.log(Level.FINE, "Sending ExecuteCommandRequest to Raft Server " + leaderIp);
                        out.println(messageSerializer.serialize(new ExecuteCommandRequest(command, messageIdToSend)));
                        messageIdToSend++;

                        // receive execute command reply
                        String jsonMessage = in.nextLine();
                        logger.log(Level.FINE, "Received message from Raft Server" + leaderIp + " :\n" + jsonMessage);
                        try {
                            Message message = messageSerializer.deserialize(jsonMessage);
                            if (message.getMessageType().equals(MessageType.ExecuteCommandReply)) {
                                ExecuteCommandReply executeCommandReply = (ExecuteCommandReply) message;
                                ExecuteCommandResult executeCommandResult= executeCommandReply.getExecuteCommandResult();

                                if (executeCommandResult.isValid()) {
                                    System.out.println("Result: " + executeCommandResult.getStateMachineResult());
                                    done = true;
                                } else {
                                    // if the command is not valid, contact the leader advised
                                    // (if no leader is advised, contact the same server again)
                                    logger.log(Level.FINE, "Result not valid, contacting another server...");
                                    if (executeCommandResult.getLeaderIP() != null) {
                                        leaderIp = executeCommandResult.getLeaderIP();
                                    }
                                }
                            } else {
                                logger.log(Level.WARNING, "The message received is not of type ExecuteCommandReply");
                            }

                            // close the connection
                            closeSocket(socket, logger);
                        } catch (BadMessageException e) {
                            logger.log(Level.WARNING, "Bad message received");
                        }

                    }
                } else if (commandString.equals(EXIT)) {
                    if (socket != null) {
                        closeSocket(socket, logger);
                    }
                    break;
                } else {
                    System.out.println("Invalid string inserted.");
                }

            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error in network communication");
            e.printStackTrace();
            if (socket != null) {
                closeSocket(socket, logger);
            }
            System.exit(1);
        }
    }

    private static void closeSocket(Socket socket, Logger logger) {

        try {
            socket.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Impossible to close the socket");
        }
    }
}
