package com.logginghub.logging.frontend.pipeline;

import com.logginghub.utils.BaseStat;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.Stat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.logging.Logger;

import java.util.concurrent.ArrayBlockingQueue;

public class Queue<In> {

    private ArrayBlockingQueue<In> queue;
    private String name;
    private int processedCount = 0;

    private StatBundle statBundle = new StatBundle();
    private IntegerStat inStat = statBundle.createIntegerStat("in");
    private IntegerStat outStat = statBundle.createIntegerStat("out");

    private Stat queueStat = statBundle.addStat(new BaseStat("size") {
        public Integer getValue() {
            return queue.size();
        }
    });

    public Queue(int limit) {
        queue = new ArrayBlockingQueue<In>(limit);
        
        inStat.setIncremental(true);
        outStat.setIncremental(true);
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void post(In object) throws InterruptedException {
        queue.put(object);
        inStat.increment();
    }

    public void process(In t) {
        queue.add(t);
        inStat.increment();
    }

    public In take() throws InterruptedException {
        In take = queue.take();
        processedCount++;
        outStat.increment();
        return take;
    }

    public int size() {
        return queue.size();
    }

    public In peek() {
        return queue.peek();
    }

    public int getLimit() {
        return queue.remainingCapacity() + queue.size();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void logStats(Logger root) {
        statBundle.startPerSecond(root);
    }
}
