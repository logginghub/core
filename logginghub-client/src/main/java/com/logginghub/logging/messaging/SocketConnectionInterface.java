package com.logginghub.logging.messaging;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.FilteredMessageSender;
import com.logginghub.logging.interfaces.QueueAwareLoggingMessageSender;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.messaging2.local.MessageBucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Another refactoring facilitation - similar to SocketHubInterface.  It will replace the direct references to
 * SocketConnection in the SocketHub class. I'm doing this now so I have have internal connection (such as the
 * import/export bridge) connecting in a similar way to external connections, and to use the source connection !=
 * destination connection to stop bridges sending events back to themselves.
 */

public interface SocketConnectionInterface
        extends Destination<LogEvent>, FilteredMessageSender, QueueAwareLoggingMessageSender {
    void send(LogEvent logEvent);

    void send(LoggingMessage message) throws LoggingMessageSenderException;

    void close();

    void stop();

    void setConnectionID(int connectionID);

    void setMessagesOutCounter(IntegerStat messagesOut);

    void setLevelFilter(int levelFilter);

    Destination<LoggingMessage> getMessageDestination();

    int getConnectionID();

    LinkedBlockingQueue<LoggingMessage> getWriteQueue();

    void setConnectionType(int i);

    String getConnectionDescription();
    void setConnectionDescription(String description);
}

