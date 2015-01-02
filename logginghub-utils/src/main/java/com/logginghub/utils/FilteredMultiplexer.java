package com.logginghub.utils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.filter.Filter;

public class FilteredMultiplexer<A> implements Destination<A> {

    private List<Pair<Filter<A>, Destination<A>>> destinations = new CopyOnWriteArrayList<Pair<Filter<A>, Destination<A>>>();
    private ExceptionHandler exceptionHandler = new LoggerExceptionHandler();

    public void send(A t) {
        for (Pair<Filter<A>, Destination<A>> destination : destinations) {
            if (destination.getA().passes(t)) {
                try {
                    destination.getB().send(t);
                }
                catch (RuntimeException e) {
                    exceptionHandler.handleException("Sending event to multiplexer destination", e);
                }
            }
        }
    }

    public void addDestination(Filter<A> filter, Destination<A> listener) {
        destinations.add(new Pair<Filter<A>, Destination<A>>(filter, listener));
    }

    public void removeDestination(Filter<A> filter, Destination<A> listener) {
        
        Iterator<Pair<Filter<A>, Destination<A>>> iterator = destinations.iterator();
        while (iterator.hasNext()) {
            Pair<com.logginghub.utils.filter.Filter<A>, com.logginghub.utils.Destination<A>> pair = (Pair<com.logginghub.utils.filter.Filter<A>, com.logginghub.utils.Destination<A>>) iterator.next();
            if(pair.getA() == filter && pair.getB() == listener){
                iterator.remove();
            }
        }
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
}
