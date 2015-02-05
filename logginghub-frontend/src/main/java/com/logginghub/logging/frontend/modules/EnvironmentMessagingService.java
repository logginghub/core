package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.RequestResponseMessage;
import com.logginghub.utils.Destination;

import java.util.concurrent.Future;

public interface EnvironmentMessagingService {

    /**
     * Send a request message to this environment; any response will be sent back to the destination provided - if and when it arrives.
     */
    <T> void send(RequestResponseMessage request, Destination<LoggingMessage> destination);

    /**
     * Send a request that might result in multiple messages being sent back. The return object is used to in the called to endStreaming to remove the listener when you are done.
     *
     * @param request
     * @param destination
     * @return
     */
    Object sendStreaming(RequestResponseMessage request, Destination<LoggingMessage> destination);

    /**
     * Stop sending messages to the destination passed into a previous call to sendStreaming
     *
     * @param streamingToken
     */
    void stopStreaming(Object streamingToken);

    Future<Boolean> subscribe(String channel, Destination<ChannelMessage> destination);

    /**
     * Fire and forget message send
     *
     * @param message
     * @throws LoggingMessageSenderException
     */
    void send(LoggingMessage message) throws LoggingMessageSenderException;

    void addLogEventListener(LogEventListener logEventListener);

    void removeLogEventListener(LogEventListener logEventListener);


    /**
     * Subscribe to messages sent to us on the private channel
     *
     * @param destination
     */
    void subscribeToPrivateChannel(Destination<ChannelMessage> destination);
    void unsubscribeFromPrivateChannel(Destination<ChannelMessage> destination);

    int getConnectionID();
}
