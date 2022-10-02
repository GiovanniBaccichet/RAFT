package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
import it.polimi.baccichetmagri.raft.network.exceptions.BadMessageException;
import it.polimi.baccichetmagri.raft.network.messageserializer.MessageSerializer;
import it.polimi.baccichetmagri.raft.utils.ConsumerThrowsIOException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
    private final BlockingQueue<S> repliesQueue;
    private boolean discardReplies;
    private final String ip; // ip of the server with which this object is used to communicate
    private final int id;

    RPCCallHandler(String ip, int id) {
        this.repliesQueue = new LinkedBlockingQueue<>();
        this.discardReplies = false;
        this.ip = ip;
        this.id = id;
    }

    S makeCall(T requestMsg) throws InterruptedException, IOException {
        try {
            // send the request message over the network and wait for the reply; if timeout expires, redo call
            Socket socket = new Socket(this.ip, ServerSocketManager.RAFT_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            Scanner in = new Scanner(socket.getInputStream());
            S reply = null;
            out.println("SERVER " + this.id);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "sending connect message to " + this.ip);
            String requestMsgString = (new MessageSerializer()).serialize(requestMsg);
            out.println(requestMsgString);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "sending request message " + requestMsgString + " from " + this.ip);
            String replyString = in.nextLine();
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "received reply " + replyString + "from " + this.ip);

        /*while(reply == null) {
            String requestMsgString = (new MessageSerializer()).serialize(requestMsg);
            out.println(requestMsgString);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending request to server " + this.ip);
            reply = this.repliesQueue.poll(REPLY_TIMEOUT, TimeUnit.MILLISECONDS);
        }*/
            socket.close();
            return (S) (new MessageSerializer()).deserialize(replyString);
        } catch (BadMessageException e) {
            e.printStackTrace();
        }
        return null;
    }

    void receiveReply(S replyMsg) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "📬 Received reply: " +  (new MessageSerializer()).serialize(replyMsg));
        if (!this.discardReplies) {
            this.repliesQueue.add(replyMsg);
        }
    }

    void setDiscardReplies(boolean discardReplies) {
        this.discardReplies = discardReplies;
    }
}
