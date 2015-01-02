package com.logginghub.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generic multiplexer for Source and Destination.
 * 
 * @author James
 * 
 * @param <T>
 */
public class Multiplexer<T> implements Source<T>, Destination<T>, StreamListener<T> {

    private List<Destination<T>> destinations = new CopyOnWriteArrayList<Destination<T>>();
    private ExceptionHandler exceptionHandler = new LoggerExceptionHandler();

    public void send(T t) {
        for (Destination<T> destination : destinations) {
            try {
                destination.send(t);
            }
            catch (RuntimeException e) {
                exceptionHandler.handleException("Sending event to multiplexer destination", e);
            }
        }
    }

    public void addDestination(Destination<T> listener) {
        destinations.add(listener);
    }

    public void removeDestination(Destination<T> listener) {
        destinations.remove(listener);
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }
    
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void onNewItem(T t) {
        send(t);
    }
    
}
