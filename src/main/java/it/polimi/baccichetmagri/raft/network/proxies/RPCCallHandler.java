package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;
import it.polimi.baccichetmagri.raft.utils.ConsumerThrowsIOException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A class that allows to make RPCs to remote servers.
 * @param <T> the type of the REQUEST message
 * @param <S> the type of the REPLY message
 */
class RPCCallHandler<T extends Message, S extends Message> {
    public static final int REPLY_TIMEOUT = 5000; // in milliseconds
    private boolean discardReplies;
    private final String ip; // ip of the server with which this object is used to communicate
    private final int id;

    RPCCallHandler(String ip, int id) {
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
            out.println("SERVER " + this.id);
            out.flush();
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "sending connect message to " + this.ip);
            String requestString = (new MessageSerializer()).serialize(requestMsg);
            while(reply == null && !discardReplies) {
                out.println(requestString);
                out.flush();
                System.out.println("[" + this.getClass().getSimpleName() + "] " + "sending request message " + requestString + " from " + this.ip);
                try {
                    String replyString = in.readLine();
                    System.out.println("[" + this.getClass().getSimpleName() + "] " + "received reply " + replyString + "from " + this.ip);
                    reply = (S) (new MessageSerializer()).deserialize(replyString);
                } catch (IOException e) {
                    e.printStackTrace();
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
