package com.logginghub.logging.frontend;

import java.awt.AWTEvent;
import java.awt.EventQueue;

import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.VisualStopwatchController;
import com.logginghub.utils.logging.Logger;

/**
 * Simple instrumented extension of an awt event queue so we can report the size
 * to let users know when things are running slowly.
 * 
 * @author James
 * 
 */
public class CountingEventQueue extends EventQueue {

    private volatile int count = 0;
    private volatile int processedSinceLastCheck = 0;
    
    private static final Logger logger = Logger.getLoggerFor(CountingEventQueue.class);

    public CountingEventQueue(EventQueue eventQueue) {
        // james - filthy hack - I couldn't find a way to get hold of or set the
        // dispatch thread field on the custom queue. Without that field set,
        // EventQueue.isDispatchThread() will always return true as the custom
        // queue's private dispatchThread field will always be null.
        try {
            ReflectionUtils.setField("dispatchThread", this, ReflectionUtils.getField("dispatchThread", eventQueue));
        }
        catch (Exception e) {
            System.err.println("Failed to hack the dispatchThread in the counting event queue - EDT checks may fail, but the frontend should still run");
        }
    }

    @Override public void postEvent(AWTEvent theEvent) {
        count++;
        super.postEvent(theEvent);
    }

    @Override protected void dispatchEvent(AWTEvent event) {
        Stopwatch start = Stopwatch.start("dispatchEvent");
        super.dispatchEvent(event);
        start.stop();
        if(start.getDurationMillis() > 50) {
            logger.warn("Swing event thread took {} ms to execute {}", start.getFormattedDurationMillis(), event);
            
//            if(event instanceof InvocationEvent){
//                InvocationEvent invocationEvent = (InvocationEvent) event;
//                
//                try {
//                    Runnable runnable = (Runnable)ReflectionUtils.getField("runnable", invocationEvent);
//                    logger.warn("Runnable is {}", runnable);
//                    logger.warn("Source is {}", invocationEvent.getSource());
//                }
//                catch (SecurityException e) {
//                    e.printStackTrace();
//                }
//                catch (NoSuchFieldException e) {
//                    e.printStackTrace();
//                }
//                catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }
//                catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//                
//            }
            
        }
        VisualStopwatchController.getInstance().add(start);
    }

    public int getCount() {
        return count;
    }

    public int getProcessedSinceLastCheck() {
        int last = processedSinceLastCheck;
        processedSinceLastCheck = 0;
        return last;
    }

    @Override public AWTEvent getNextEvent() throws InterruptedException {
        AWTEvent nextEvent = super.getNextEvent();
        // Dont fully understand how this works, so just fudge it for now so it
        // doesn't go negative
        if (count > 0) {
            count--;
        }
        processedSinceLastCheck++;
        return nextEvent;
    }

}
