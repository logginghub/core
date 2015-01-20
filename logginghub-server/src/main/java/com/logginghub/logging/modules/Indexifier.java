package com.logginghub.logging.modules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messages.HistoricalIndexResponse;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;

public class Indexifier extends Observable {

    private Multiplexer<HistoricalIndexElement> finishedHistoricalIndexElementMultiplexer = new Multiplexer<HistoricalIndexElement>();
    private static final Logger logger = Logger.getLoggerFor(Indexifier.class);

    private long earliestTime = Long.MAX_VALUE;
    private long latestPeriod = -1;

    private Map<Long, HistoricalIndexElement> counts = new HashMap<Long, HistoricalIndexElement>();
    private long interval;

    public Indexifier(long period) {
        this.interval = period;
    }

    // public static final class HistoricalIndexElement {
    // public int infoCount;
    // public int warningCount;
    // public int severeCount;
    // public int otherCount;
    //
    // public int getTotalCount() {
    // return infoCount + warningCount + severeCount + otherCount;
    // }
    //
    // public void remove(LogEvent event) {
    // int level = event.getLevel();
    // if (level == Logger.severe) {
    // severeCount--;
    // }
    // else if (level == Logger.warning) {
    // warningCount--;
    // }
    // else if (level == Logger.info) {
    // infoCount--;
    // }
    // else {
    // otherCount--;
    // }
    // }
    //
    // public void increment(LogEvent event) {
    // int level = event.getLevel();
    // if (level == Logger.severe) {
    // severeCount++;
    // }
    // else if (level == Logger.warning) {
    // warningCount++;
    // }
    // else if (level == Logger.info) {
    // infoCount++;
    // }
    // else {
    // otherCount++;
    // }
    // }
    //
    // public void set(HistoricalIndexHistoricalIndexElement historicalIndexHistoricalIndexElement)
    // {
    // severeCount = historicalIndexHistoricalIndexElement.getSevereCount();
    // infoCount = historicalIndexHistoricalIndexElement.getInfoCount();
    // warningCount = historicalIndexHistoricalIndexElement.getWarningCount();
    // otherCount = historicalIndexHistoricalIndexElement.getOtherCount();
    // }
    // }

    public void removeEvent(LogEvent event) {
        long intervalStart = TimeUtils.chunk(event.getOriginTime(), interval);
        synchronized (counts) {

            HistoricalIndexElement count = counts.get(intervalStart);
            if (count == null) {
                // Strange...
            }
            else {
                count.remove(event);
                if (count.getTotalCount() == 0) {
                    counts.remove(intervalStart);
                }
            }
        }
    }

    public void addEvent(LogEvent event) {
        HistoricalIndexElement done = addEventAndReturnDone(event);
        if (done != null) {
            finishedHistoricalIndexElementMultiplexer.send(done);
        }
    }

    public HistoricalIndexElement getCurrent() {
        return counts.get(latestPeriod);
    }

    public HistoricalIndexElement add(long time, HistoricalIndexElement value) {
        HistoricalIndexElement done = null;

        long intervalStart = TimeUtils.chunk(time, interval);

        if (latestPeriod == -1) {
            latestPeriod = intervalStart;
        }
        else if (intervalStart > latestPeriod) {
            // The event is for an interval after our current period, making our current period
            // 'complete'
            done = counts.get(latestPeriod);
            latestPeriod = intervalStart;
        }

        synchronized (counts) {

            HistoricalIndexElement count = counts.get(intervalStart);
            if (count == null) {
                count = new HistoricalIndexElement();
                count.setTime(intervalStart);
                count.setInterval(interval);
                counts.put(intervalStart, count);
                logger.finest("Creating chunk '{}'", Logger.toDateString(intervalStart));
            }

            earliestTime = Math.min(intervalStart, earliestTime);
            count.increment(value);
        }

        if (intervalStart < latestPeriod) {
            // This was an update to an old period; send out the old period again
            done = counts.get(intervalStart);
        }

        return done;
    }

    private HistoricalIndexElement createElement(long intervalStart) {
        HistoricalIndexElement count = new HistoricalIndexElement();
        count.setTime(intervalStart);
        count.setInterval(interval);
        counts.put(intervalStart, count);
        logger.finest("Creating chunk '{}'", Logger.toDateString(intervalStart));
        return count;
    }
    
    public HistoricalIndexElement addEventAndReturnDone(LogEvent event) {

        HistoricalIndexElement done = null;

        long intervalStart = TimeUtils.chunk(event.getOriginTime(), interval);

        if (latestPeriod == -1) {
            latestPeriod = intervalStart;
        }
        else if (intervalStart > latestPeriod) {
            // The event is for an interval after our current period, making our current period
            // 'complete'
            done = counts.get(latestPeriod);
            latestPeriod = intervalStart;
        }

        synchronized (counts) {

            HistoricalIndexElement count = counts.get(intervalStart);
            if (count == null) {
                count = createElement(intervalStart);
            }

            earliestTime = Math.min(intervalStart, earliestTime);
            count.increment(event);
        }

        if (intervalStart < latestPeriod) {
            // This was an update to an old period; send out the old period again
            done = counts.get(intervalStart);
        }

        return done;
    }

    public long getLatestPeriod() {
        return latestPeriod;
    }

    public Map<Long, HistoricalIndexElement> getCounts() {
        return counts;
    }

    public HistoricalIndexElement getMaximumCount() {

        HistoricalIndexElement max = new HistoricalIndexElement();
        max.setInterval(interval);

        synchronized (counts) {
            Collection<HistoricalIndexElement> values = counts.values();
            for (HistoricalIndexElement other : values) {
                max.setSevereCount(Math.max(max.getSevereCount(), other.getSevereCount()));
                max.setWarningCount(Math.max(max.getWarningCount(), other.getWarningCount()));
                max.setInfoCount(Math.max(max.getInfoCount(), other.getInfoCount()));
                max.setOtherCount(Math.max(max.getOtherCount(), other.getOtherCount()));
            }
        }
        return max;
    }

    public void clearEvents() {
        synchronized (counts) {
            counts.clear();
        }

        earliestTime = Long.MAX_VALUE;
    }

    public void process(HistoricalIndexElement element) {
        logger.finest("Processing element '{}'", element);
        long time = element.getTime();
        HistoricalIndexElement count = counts.get(time);
        if (count == null) {
            // TODO : is this right? Doesn't it need chunking?
            count = createElement(time);
        }

        count.set(element);
        earliestTime = Math.min(time, earliestTime);
    }

    public void processUpdate(HistoricalIndexResponse response) {

        HistoricalIndexElement[] historicalIndexElements = response.getElements();
        if (historicalIndexElements != null && historicalIndexElements.length > 0) {
            logger.fine("Processing update for {} items from '{}' to '{}'",
                        historicalIndexElements.length,
                        Logger.toDateString(historicalIndexElements[0].getTime()),
                        Logger.toDateString(historicalIndexElements[historicalIndexElements.length - 1].getTime()));
        }
        else {
            logger.fine("Processing update for {} items", historicalIndexElements.length);
        }

        synchronized (counts) {
            for (HistoricalIndexElement element : historicalIndexElements) {
                logger.finest("Processing : {}", element);
                process(element);
            }
        }
    }

    public void process(HistoricalIndexResponse response) {
//        clearEvents();
        processUpdate(response);
    }

    public HistoricalIndexElement[] toSortedElements() {
        HistoricalIndexElement[] result;

        synchronized (counts) {
            int size = counts.size();
            int index = 0;
            result = new HistoricalIndexElement[size];
            Set<Entry<Long, HistoricalIndexElement>> entrySet = counts.entrySet();
            for (Entry<Long, HistoricalIndexElement> entry : entrySet) {
                HistoricalIndexElement value = entry.getValue();
                result[index] = value;
                index++;
            }

            Arrays.sort(result, new Comparator<HistoricalIndexElement>() {
                @Override public int compare(HistoricalIndexElement o1, HistoricalIndexElement o2) {
                    return CompareUtils.compareLongs(o1.getTime(), o2.getTime());
                }
            });
        }

        return result;

    }

    public long getEarliestTime() {
        return earliestTime;
    }

    public void addFinishedIntervalDestination(Destination<HistoricalIndexElement> destination) {
        finishedHistoricalIndexElementMultiplexer.addDestination(destination);
    }

    public void removeFinishedIntervalDestination(Multiplexer<HistoricalIndexElement> destination) {
        finishedHistoricalIndexElementMultiplexer.removeDestination(destination);
    }

}
