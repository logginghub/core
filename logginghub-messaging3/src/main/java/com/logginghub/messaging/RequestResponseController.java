package com.logginghub.messaging;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.messaging.directives.MessageWrapper;
import com.logginghub.messaging.netty.Level1MessageSender;
import com.logginghub.messaging.netty.MessageListener;
import com.logginghub.utils.Timeout;
import com.logginghub.utils.logging.Logger;

public class RequestResponseController implements MessageListener {
    
    private static final Logger logger = Logger.getLoggerFor(RequestResponseController.class);
    private AtomicInteger nextRequestID = new AtomicInteger(1);
    private Timeout timeout = Timeout.defaultTimeout;
    private Timer timer = new Timer("RequestResponseTimeoutTimer", true);
    private Map<Integer, RequestResponseWrapper<?>> wrappersByRequestID = new ConcurrentHashMap<Integer, RequestResponseWrapper<?>>();
    
    class RequestResponseWrapper<T> {
        private CountDownLatch latch = new CountDownLatch(1);
        private ResponseListener<T> listener;

        public RequestResponseWrapper(ResponseListener<T> listener) {
            this.listener = listener;
        }

        public CountDownLatch getLatch() {
            return latch;
        }

        public ResponseListener<T> getListener() {
            return listener;
        }
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }
    
    public void await(int responseID) throws InterruptedException {
        RequestResponseWrapper<?> wrapper = wrappersByRequestID.get(responseID);
        if (wrapper != null) {
            wrapper.getLatch().await();
        }
    }

    public boolean await(long time, TimeUnit units, int responseID) throws InterruptedException {
        RequestResponseWrapper<?> wrapper = wrappersByRequestID.get(responseID);
        if (wrapper != null) {
            return wrapper.getLatch().await(time, units);
        }
        else {
            return true;
        }
    }

    @SuppressWarnings("unchecked") public <T> void onNewMessage(Object message, Level1MessageSender sender) {
        if (message instanceof MessageWrapper) {
            MessageWrapper messageWrapper = (MessageWrapper) message;

            int responseID = messageWrapper.getResponseID();

            RequestResponseWrapper<T> wrapper = (RequestResponseWrapper<T>)wrappersByRequestID.remove(responseID); 
            if (wrapper != null) {
                logger.trace("Request response wrapper found for response ID {}, notifying and releasing latch", responseID);
                wrapper.getListener().onResponse((T) messageWrapper.getPayload());
                wrapper.getLatch().countDown();
            }else{
                logger.trace("No request response wrapper found for response ID {}, this might be an issue", responseID);
            }
        }
    }

    public <T> int registerRequest(ResponseListener<T> listener) {
        final int requestID = nextRequestID.getAndIncrement();
        RequestResponseWrapper<T> wrapper = new RequestResponseWrapper<T>(listener);
        wrappersByRequestID.put(requestID, wrapper);

        timer.schedule(new TimerTask() {
            @Override public void run() {
                timeoutRequest(requestID);
            }
        }, timeout.getUnits().toMillis(timeout.getTime()));
        logger.trace("Request response wrapper registered for response ID {}, starting timeout timer", requestID);

        return requestID;
    }

    protected <T> void timeoutRequest(int responseID) {
        RequestResponseWrapper<T> wrapper = (RequestResponseWrapper<T>) wrappersByRequestID.remove(responseID);
        if (wrapper != null) {
            logger.trace("Timing out request response wrapper for response ID {}", responseID);
            wrapper.getListener().onResponseTimeout();
            wrapper.getLatch().countDown();
        }
    }

}
