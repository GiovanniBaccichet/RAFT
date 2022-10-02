package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.network.ServerSocketManager;
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

    RPCCallHandler(String ip) {
        this.repliesQueue = new LinkedBlockingQueue<>();
        this.discardReplies = false;
        this.ip = ip;
    }

    S makeCall(T requestMsg) throws InterruptedException, IOException {
        // send the request message over the network and wait for the reply; if timeout expires, redo call
        Socket socket = new Socket(this.ip, ServerSocketManager.RAFT_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        Scanner in = new Scanner(socket.getInputStream());
        S reply = null;
        while(reply == null) {
            out.println(requestMsg);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending request to server " + this.ip);
            reply = this.repliesQueue.poll(REPLY_TIMEOUT, TimeUnit.MILLISECONDS);
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Received reply from queue from server " + this.ip);
        }
        socket.close();
        return reply;
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
