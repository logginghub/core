package com.logginghub.logging.frontend.charting.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.logging.Logger;

@Deprecated
// TODO : use the version in utils
public class Stream<T> {

    private static final Logger logger = Logger.getLoggerFor(Stream.class);
    private List<StreamListener<T>> listeners = new CopyOnWriteArrayList<StreamListener<T>>();
    private boolean debug;

    public void addListener(StreamListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(StreamListener<T> listener) {
        listeners.remove(listener);
    }

    public void send(T t) {
        if (debug) {
            logger.info("Streaming : {}", t);
        }
        
        for (StreamListener<T> streamListener : listeners) {
            streamListener.onNewItem(t);
        }
    }

    public void setDebug(boolean b) {
        this.debug = b;
    }

}
