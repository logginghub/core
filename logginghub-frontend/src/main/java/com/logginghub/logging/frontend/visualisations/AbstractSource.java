package com.logginghub.logging.frontend.visualisations;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.utils.Destination;
import com.logginghub.utils.Source;

public abstract class AbstractSource<T> implements Source<T>{

    private List<Destination<T>> destinations = new CopyOnWriteArrayList<Destination<T>>();
    
    protected void dispatch(T t) {
        for (Destination<T> destination : destinations) {
            destination.send(t);
        }
    }
    
    @Override public void addDestination(Destination<T> listener) {
        destinations.add(listener);
    }

    @Override public void removeDestination(Destination<T> listener) {
        destinations.remove(listener);
    }

}
