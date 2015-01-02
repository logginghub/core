package com.logginghub.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.filter.Filter;
import com.logginghub.utils.logging.Logger;

public class Stream<T> implements StreamListener<T>, Destination<T>, Source<T> {

    private static final Logger logger = Logger.getLoggerFor(Stream.class);
    
    private List<StreamListener<T>> listeners = new CopyOnWriteArrayList<StreamListener<T>>();
    private List<Destination<T>> destinations = new CopyOnWriteArrayList<Destination<T>>();
    
    private boolean debug;
    private Filter<T> filter;

    public void addListener(Destination<T> destination) {
        destinations.add(destination);
    }
    
    public void removeListener(Destination<T> destination) {
        destinations.remove(destination);
    }
    
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
        
        for (Destination<T> destination : destinations) {
            destination.send(t);
        }
    }

    public void setDebug(boolean b) {
        this.debug = b;
    }

    public void setFilter(Filter<T> filter) {
        this.filter = filter;
    }
    
    public Filter<T> getFilter() {
        return filter;
    }
    
    public void onNewItem(T t) {
        if (filter == null || filter.passes(t)) {
            send(t);
        }
    }

    public void addDestination(Destination<T> destination) {
        destinations.add(destination);
    }

    public void removeDestination(Destination<T> destination) {
        destinations.remove(destination);
    }


}
