package com.logginghub.logging.frontend.pipeline;

import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class NoopQueueDrainer<T> extends WorkerThread{

    private long lastTime = 0;
    private Queue<T> queue;
    private long waitTimeNanos;

    public NoopQueueDrainer(Queue<T> queue ) {
        super("NoopQueueDrainer::" + queue.toString());
        this.queue = queue;
    }

    @Override
    protected void onRun() throws Throwable {
        long start = System.nanoTime();
        queue.take();
        long elapsed = System.nanoTime() - start;
        this.waitTimeNanos += elapsed;
    }
    
    public void startStats(Logger logger) {
        TimerUtils.everySecond("NoopStats", new Runnable() {
            public void run() {
               long time = System.currentTimeMillis();
                if(lastTime > 0) {
                   
                    long delta = time -lastTime;
                    double waitMS = waitTimeNanos * 1e-6d;
                    double factor = (1 - (waitMS / delta)) * 100.0;
                    System.out.println(factor);
                    waitTimeNanos = 0;
               }
                
                lastTime = time;
               
            }
        });
    }

}
