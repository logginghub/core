package com.logginghub.logging.repository;

import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.sof.SerialisableObject;

public class TimeFilterDestination implements Destination<SerialisableObject> {

    private Destination<SerialisableObject> decoratee;
    private long start;
    private long end;

    private long passed = 0;
    private long failed = 0;

    public TimeFilterDestination(Destination<SerialisableObject> destination, long start, long end) {
        this.decoratee = destination;
        this.start = start;
        this.end = end;
    }

    @Override public void send(SerialisableObject t) {
        if (t instanceof TimeProvider) {
            TimeProvider timeProvider = (TimeProvider) t;
            long time = timeProvider.getTime();
            if (time >= start && time < end) {
                decoratee.send(t);
                passed++;
            }else{
                failed++;
            }
        }
        else {
            decoratee.send(t);
            passed++;
        }
    }
    
    public long getPassed() {
        return passed;
    }
    
    public long getFailed() {
        return failed;
    }

}
