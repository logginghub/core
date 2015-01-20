package com.logginghub.logging.messaging;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * These guys can masquerade as "real" socket connections, but can be used for those internal producers and consumers
 * within the hub.  Not all methods are implemented - a very leaky abstraction that needs to be tidied up at some
 * point.
 */
public class InternalConnection implements SocketConnectionInterface {

    private String description;
    private int connectionType;
    private int levelFilter;
    private int connectionID = -1;
    private IntegerStat messagesOut = new IntegerStat("messagesOut",0);

    public InternalConnection(String description, int connectionType) {
        this.description = description;
        this.connectionType = connectionType;
    }

    @Override
    public void send(LogEvent logEvent) {
        messagesOut.increment();
    }

    @Override
    public void send(LoggingMessage message) throws LoggingMessageSenderException {
        messagesOut.increment();
    }

    @Override
    public void close() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setMessagesOutCounter(IntegerStat messagesOut) {
        this.messagesOut = messagesOut;
    }

    @Override
    public Destination<LoggingMessage> getMessageDestination() {
        return null;
    }

    @Override
    public int getConnectionID() {
        return connectionID;
    }

    @Override
    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    @Override
    public LinkedBlockingQueue<LoggingMessage> getWriteQueue() {
        return null;
    }

    @Override
    public String getConnectionDescription() {
        return description;
    }

    @Override
    public void setConnectionDescription(String description) {
        this.description = description;
    }

    @Override
    public int getLevelFilter() {
        return levelFilter;
    }

    @Override
    public void setLevelFilter(int levelFilter) {
        this.levelFilter = levelFilter;
    }

    @Override
    public int getConnectionType() {
        return connectionType;
    }

    @Override
    public void setConnectionType(int i) {
        this.connectionType = i;
    }

    @Override
    public boolean isSendQueueEmpty() {
        return false;
    }
}
