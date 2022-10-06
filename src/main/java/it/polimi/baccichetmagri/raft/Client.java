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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static int REPLY_TIMEOUT = 5000;

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Client.class.getName());
        logger.setLevel(Level.FINE);

        Socket socket = null;
        try {
            MessageSerializer messageSerializer = new MessageSerializer();
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
                        socket.setSoTimeout(REPLY_TIMEOUT);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream());

                        // send execute command request
                        out.println("CLIENT");
                        out.flush();
                        System.out.println("[Client] " + "Connection message SENT");
                        CommandImplementation command = new CommandImplementation(Integer.parseInt(commandString.substring(COMMAND.length() + 1)));
                        System.out.println("[Client] " + "COMMAND SENT: " + Integer.parseInt(commandString.substring(COMMAND.length() + 1)));
                        System.out.println("[Client] " + "Sending ExecuteCommandRequest to Raft Leader: " + leaderIp);
                        String replyString = null;
                        while (replyString == null) {
                            String messageToSend = messageSerializer.serialize(new ExecuteCommandRequest(command));
                            out.println(messageToSend);
                            out.flush();
                            System.out.println("[Client] " + "Sending command: " + messageToSend);

                            // receive execute command reply
                            try {
                                replyString = in.readLine();
                            } catch (IOException e) {
                                System.out.println("[Client] " + "Reply timeout expired");
                            }
                            System.out.println("\u001B[42m" + "[Client] " + "Received message from Raft Server: " + leaderIp + " message: " + replyString + "\u001B[0m");
                        }
                        try {
                            Message reply = messageSerializer.deserialize(replyString);
                            if (reply.getMessageType().equals(MessageType.ExecuteCommandReply)) {
                                ExecuteCommandReply executeCommandReply = (ExecuteCommandReply) reply;
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
