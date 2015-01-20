package com.logginghub.logging.generator.nextgen;

import java.util.Random;

import com.logginghub.utils.DoubleValueGenerator;
import com.logginghub.utils.RandomWithAcceleration;
import com.logginghub.utils.Stream;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class SimulatorEventSource {

    private static final Logger logger = Logger.getLoggerFor(SimulatorEventSource.class);
    private RandomWithAcceleration rate;
    private WorkerThread workerThread;
    private Stream<Long> eventStream = new Stream<Long>();
    private int sleepTime = 100;
    private long count = 0;
    private double accumulation = 0;

    public SimulatorEventSource(boolean useRandom, final double minPerSecond, final double maxPerSecond) {

        // TODO : we need to pass the trend length in as well!
        
        final double min = minPerSecond / ((double) (1000 / sleepTime));
        final double max = maxPerSecond / ((double) (1000 / sleepTime));

        double minPeriod = 10 * (1000 / sleepTime);
        double maxPeriod = 10 * (1000 / sleepTime);

        logger.info("MinPerSecond {} MaxPerSecond {} min {} max {} minPeriod {} maxPeriod {}",
                    minPerSecond,
                    maxPerSecond,
                    min,
                    max,
                    minPeriod,
                    maxPeriod);

        DoubleValueGenerator fixedTrend = new DoubleValueGenerator() {
            public double next() {
                return 10 * (1000 / sleepTime);
            }
        };

        DoubleValueGenerator randomTrend = new DoubleValueGenerator() {
            private Random random = new Random();

            public double next() {
                return (2 + random.nextInt(18)) * (1000 / sleepTime);
            }
        };

        DoubleValueGenerator toggleValue = new DoubleValueGenerator() {
            boolean toggle = false;

            public double next() {
                toggle = !toggle;
                if (toggle) return min;
                else return max;
                
            }
        };

        DoubleValueGenerator randomValue = new DoubleValueGenerator() {
            private Random random = new Random();

            public double next() {
                return (10 + random.nextInt(60)) / (1000 / (double) sleepTime);
            }
        };

        if (useRandom) {
            rate = new RandomWithAcceleration(randomTrend, randomValue);
        }
        else {
            rate = new RandomWithAcceleration(fixedTrend, toggleValue);
        }

    }

    public void setCount(long count) {
        this.count = count;
    }
    
    public void start() {
        // StatBundle stats = new StatBundle();
        // final IntegerStat events = stats.createIncremental("events");
        // stats.startPerSecond(logger);

        // TimerUtils.everySecond("test", new Runnable() {
        // public void run() {
        // logger.info("{}", rate.getCurrentValue());
        // }
        // });

        workerThread = WorkerThread.executeOngoing("Source", new Runnable() {

            public void run() {
                accumulation += rate.next();
                while (accumulation > 1) {
                    eventStream.onNewItem(count++);
                    // events.increment();
                    accumulation--;
                }

                // TODO : this is going to make quite jagged load if we look closely enough
                ThreadUtils.sleep(sleepTime);
            }
        });

    }

    public void stop() {
        workerThread.stop();
    }

    public Stream<Long> getEventStream() {
        return eventStream;
    }

}
