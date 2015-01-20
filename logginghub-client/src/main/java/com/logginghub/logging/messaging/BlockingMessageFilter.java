package com.logginghub.logging.messaging;

import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.filter.Filter;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by james on 11/11/14.
 */
public abstract class BlockingMessageFilter implements Filter<LoggingMessage>, LoggingMessageListener {
    final Exchanger<LoggingMessage> responseMessageExchanger = new Exchanger<LoggingMessage>();
    private Timeout timeout = Timeout.defaultTimeout;

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public void onNewLoggingMessage(LoggingMessage message) {
        if (passes(message)) {
            try {
                responseMessageExchanger.exchange(message);
            } catch (InterruptedException e) {
            }
        }
    }

    public LoggingMessage await() throws InterruptedException, TimeoutException {
        LoggingMessage exchanged = null;
        exchanged = responseMessageExchanger.exchange(null, timeout.getMillis(), TimeUnit.MILLISECONDS);
        return exchanged;
    }
}
