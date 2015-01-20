package com.logginghub.logging.generators;

import java.util.Random;

import org.apache.log4j.Level;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.interfaces.AbstractLogEventSource;
import com.logginghub.utils.StoppableThread;
import com.logginghub.utils.ThreadUtils;

public class RandomEventGenerator extends AbstractLogEventSource {

    private StoppableThread thread;
    private Random random = new Random();

    public void start(long delay) {
        thread = ThreadUtils.runWithDelay("RandomEventGenerator", delay, new Runnable() {
            @Override public void run() {
                generateNext();
            }
        });
    }

    public void stop() {
        if (thread != null) {
            thread.stopSafely(10000);
        }
    }

    private void generateNext() {

        String[] operations = new String[] { "ValidateUser", "HandleNewOrder", "GetOrder", "UpdateMarketData" };
        String[] outcomes = new String[] { "succeeded", "completed unsuccessfully", "failed" };

        double[] operationMax = new double[] { 20, 500, 100, 200 };
        double[] operationMin = new double[] { 1, 50, 80, 190 };

        int operationIndex = random.nextInt(operations.length);
        String operation = operations[operationIndex];
        String outcome = outcomes[random.nextInt(outcomes.length)];

        double time = operationMin[operationIndex] + (random.nextDouble() * (operationMax[operationIndex] - operationMin[operationIndex]));
        String summary;

        switch (operationIndex) {
            case 0: {
                summary = "User '12314' validated ok";
                break;
            }
            case 1: {
                summary = String.format("Order was for %d items", random.nextInt(10));
                break;
            }
            case 2: {
                summary = String.format("encoded order size was %d bytes (%d compressed)", random.nextInt(1024), random.nextInt(100));
                break;
            }
            case 3: {
                summary = String.format("updated market data for <instrument>");
                break;
            } default : {
                throw new RuntimeException(String.format("You've missed a switch statment for value %d", operationIndex));
            }
        }
        
        int level = Level.INFO.toInt();

        String message = String.format("%s %s in %.2f ms : %s", operation, outcome, time, summary);
        
        DefaultLogEvent logEvent = new DefaultLogEvent();
        logEvent.setFormattedException("formattedException");
        logEvent.setFormattedObject(new String[] {});
        logEvent.setLevel(level);
        logEvent.setLocalCreationTimeMillis(System.currentTimeMillis());
        logEvent.setLoggerName("loggedName");
        logEvent.setMessage(message);
        logEvent.setSequenceNumber(1);
        logEvent.setSourceAddress("sourceAddress");
        logEvent.setSourceApplication("sourceApplication");
        logEvent.setSourceClassName("sourceClassName");
        logEvent.setSourceHost("sourceHost");
        logEvent.setSourceMethodName("sourceMethodName");
        logEvent.setThreadName("threadName");
        
        
        fireNewLogEvent(logEvent);
    }
}
