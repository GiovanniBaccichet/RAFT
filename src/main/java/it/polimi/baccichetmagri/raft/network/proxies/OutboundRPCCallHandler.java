package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A class that allows to make RPCs to remote servers.
 * @param <T> the type of the REQUEST message
 * @param <S> the type of the REPLY message
 */
class OutboundRPCCallHandler<T extends Message, S extends Message> {
    public static final int REPLY_TIMEOUT = 5000; // in milliseconds
    private boolean discardReplies;
    private final String ip; // ip of the server with which this object is used to communicate
    private final int id;

    OutboundRPCCallHandler(String ip, int id) {
        this.discardReplies = false;
        this.ip = ip;
        this.id = id;
    }

    S makeCall(T requestMsg) throws InterruptedException, IOException {
        try (Socket socket = new Socket(this.ip, ServerSocketManager.RAFT_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            // send the request message over the network and wait for the reply; if timeout expires, redo call
            socket.setSoTimeout(REPLY_TIMEOUT);
            S reply = null;
            String requestString = (new MessageSerializer()).serialize(requestMsg);
            while(reply == null && !discardReplies) {
                out.println(requestString);
                out.flush();
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending REQUEST message " + requestString + " from " + this.ip);
                try {
                    String replyString = in.readLine();
                    System.out.println("\u001B[42m" + "[" + this.getClass().getSimpleName() + "] " + "Received REPLY " + replyString + " from " + "\u001B[0m" + this.ip);
                    reply = (S) (new MessageSerializer()).deserialize(replyString);
                } catch (IOException e) {
                    System.out.println("\u001B[41m" + "\u001B[37m" + "[" + this.getClass().getSimpleName() + "] " + "REPLY TIMEOUT because of " + requestString + "\u001B[0m");
                }

            }
            return reply;
        } catch (BadMessageException e) {
            e.printStackTrace();
        }
        return null;
    }

    void setDiscardReplies(boolean discardReplies) {
        this.discardReplies = discardReplies;
    }
}
