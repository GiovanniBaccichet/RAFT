package it.polimi.baccichetmagri.raft.network.proxies;

import it.polimi.baccichetmagri.raft.messages.Message;
import it.polimi.baccichetmagri.raft.utils.ConsumerThrowsIOException;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A class that allows to make RPCs to remote servers.
 * @param <T> the type of the request message
 * @param <S> the type of the reply message
 */
class RPCCallHandler<T extends Message, S extends Message> {
    public static final int REPLY_TIMEOUT = 1000; // in milliseconds

    private int nextMsgId;
    private final BlockingQueue<S> repliesQueue;
    private boolean discardReplies;

    RPCCallHandler() {
        this.nextMsgId = 0;
        this.repliesQueue = new LinkedBlockingQueue<>();
        this.discardReplies = false;
    }

    S makeCall(T requestMsg, ConsumerThrowsIOException<T> msgSender) throws InterruptedException, IOException {
        // add id to the request message to identify the couple request-reply
        requestMsg.setMessageId(this.nextMsgId);
        this.nextMsgId++;

        // send the request message over the network and wait for the reply; if timeout expires, redo call
        S reply = null;
        while(reply == null) {
            System.out.println("[" + this.getClass().getSimpleName() + "] " + "Sending message: " + requestMsg);
            msgSender.accept(requestMsg); // send the message
            reply = this.repliesQueue.poll(REPLY_TIMEOUT, TimeUnit.MILLISECONDS);
            if (reply != null && reply.getMessageId() != requestMsg.getMessageId()) {
                reply = null;
            }
        }

        return reply;
    }

    void receiveReply(S replyMsg) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + "Received reply " + replyMsg);
        if (!this.discardReplies) {
            this.repliesQueue.add(replyMsg);
        }
    }

    void setDiscardReplies(boolean discardReplies) {
        this.discardReplies = discardReplies;
    }
}
