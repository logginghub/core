package com.logginghub.logging.modules.history;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.Destination;

import java.util.List;

public class PlainEventBuffer implements EventBuffer {

    private int highWaterMark;

    public PlainEventBuffer(final int highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    private long watermark = 0;
    private CircularArrayList<DefaultLogEvent> events = new CircularArrayList<DefaultLogEvent>(20000);

    @Override public int countEvents() {
        return events.size();
    }

    @Override public int countBetween(long start, long end) {
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            LogEvent logEvent = events.get(i);
            long time = logEvent.getOriginTime();
            if (time >= start && time < end) {
                count++;
            }
        }
        return count;
    }

    @Override public void addEvent(DefaultLogEvent t) {
        events.add(t);

        long sizeOfAdded = t.estimateSizeOf();

        while (sizeOfAdded + watermark > highWaterMark) {
            DefaultLogEvent removed = events.remove();
            long sizeOfRemoved = removed.estimateSizeOf();
            watermark -= sizeOfRemoved;
        }

        watermark += sizeOfAdded;

    }

    @Override
    public int sizeof(DefaultLogEvent t) {
        return 0;
    }

    @Override public long getWatermark() {
        return watermark;
    }

    @Override public String toString() {
        return "PlainEventBufferEvaluator";
    }

    @Override public int size() {
        return 0;

    }

    @Override public void extractIndexBetween(List<HistoricalIndexElement> index, long start, long end) {}

    @Override public void extractEventsBetween(Destination<LogEvent> visitor, long start, long end) {}

    @Override public void extractEventsBetween(List<LogEvent> matchingEvents, long start, long end) {
        for (int i = 0; i < events.size(); i++) {
            DefaultLogEvent logEvent = events.get(i);
            long time = logEvent.getOriginTime();
            if (time >= start && time < end) {
                matchingEvents.add(logEvent);
            }
        }
    }

    @Override public int getBlockSequence() {
        return 0;
         
    }

    @Override public void addIndexListener(Destination<HistoricalIndexElement> destination) {}

}
