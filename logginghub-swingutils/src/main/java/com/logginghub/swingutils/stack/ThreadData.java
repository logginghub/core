package com.logginghub.swingutils.stack;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.logging.Logger;

public class ThreadData {

    private static final Logger logger = Logger.getLoggerFor(ThreadData.class);

    private FactoryMap<String, MutableIntegerValue> counts = new FactoryMap<String, MutableIntegerValue>() {
        @Override protected MutableIntegerValue createEmptyValue(String key) {
            return new MutableIntegerValue(key, 0);
        }
    };

    private CircularArrayList<String> methodHistory = new CircularArrayList<String>();
    private CircularArrayList<Thread.State> statuses = new CircularArrayList<Thread.State>();

    private String key;

    private int blocked;

    private int timedWaiting;

    private int waiting;

    private int running;

    private int newState;

    private int terminated;

    public ThreadData(String key) {
        this.key = key;
    }

    public void dump() {

        List<MutableIntegerValue> results = getSortedResults();
        for (MutableIntegerValue mutableIntegerValue : results) {
            logger.info("{} : {}", key, mutableIntegerValue.toString());
        }

    }

    public List<MutableIntegerValue> getSortedResults() {
        List<MutableIntegerValue> results = new ArrayList<MutableIntegerValue>();
        synchronized (counts) {
            results.addAll(counts.values());
        }
        Collections.sort(results, Collections.reverseOrder());
        return results;
    }

    public void update(Snapshot snapshot) {

        String methodToAdd = snapshot.getMethod();
        String methodToRemove = null;

        if(methodToAdd == null) {
            methodToAdd = "<null>";
        }
        
        methodHistory.add(methodToAdd);
        if (methodHistory.size() > 1000) {
            methodToRemove = methodHistory.poll();
        }

        synchronized (counts) {
            counts.get(methodToAdd).increment(1);
            if(methodToRemove != null) {
                counts.get(methodToRemove).increment(-1);
            }
        }

        statuses.add(snapshot.getState());

        if (snapshot.getState() == State.BLOCKED) {
            blocked++;
        }
        else if (snapshot.getState() == State.TIMED_WAITING) {
            timedWaiting++;
        }
        else if (snapshot.getState() == State.WAITING) {
            waiting++;
        }
        else if (snapshot.getState() == State.NEW) {
            newState++;
        }
        else if (snapshot.getState() == State.TERMINATED) {
            terminated++;
        }
        else if (snapshot.getState() == State.RUNNABLE) {
            running++;
        }

        if (statuses.size() > 1000) {
            State removed = statuses.poll();

            if (removed == State.BLOCKED) {
                blocked--;
            }
            else if (removed == State.TIMED_WAITING) {
                timedWaiting--;
            }
            else if (removed == State.WAITING) {
                waiting--;
            }
            else if (removed == State.NEW) {
                newState--;
            }
            else if (removed == State.TERMINATED) {
                terminated--;
            }
            else if (removed == State.RUNNABLE) {
                running--;
            }
        }
    }

    public int getBlocked() {
        return blocked;
    }

    public FactoryMap<String, MutableIntegerValue> getCounts() {
        return counts;
    }

    public String getKey() {
        return key;
    }

    public int getTimedWaiting() {
        return timedWaiting;
    }

    public int getWaiting() {
        return waiting;
    }

    public int getRunning() {
        return running;
    }

}
