package com.logginghub.logging.modules.history;

import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.DictionaryLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Dictionary;

public class DictionaryEventBuffer implements EventBuffer {

    private Dictionary dictionary = new Dictionary();
    private long watermark = 0;
    private CircularArrayList<DictionaryLogEvent> events = new CircularArrayList<DictionaryLogEvent>(20000);
    private long highWaterMark = 0;
    
    public DictionaryEventBuffer(long highWaterMark) {
        this.highWaterMark = highWaterMark;
    }

    @Override public int countEvents() {
        return events.size();
    }

    @Override public int countBetween(long start, long end) {
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            DictionaryLogEvent logEvent = events.get(i);
            long time = logEvent.getOriginTime();
            if (time >= start && time < end) {
                count++;
            }
        }
        return count;
    }

    @Override public void addEvent(DefaultLogEvent t) {
        DictionaryLogEvent dictionaryLogEvent = DictionaryLogEvent.fromLogEvent(t, dictionary);
        events.add(dictionaryLogEvent);

        long sizeOfAdded = dictionaryLogEvent.estimateSizeOf();

        while (sizeOfAdded + watermark > highWaterMark) {
            DictionaryLogEvent removed = events.remove();
            long sizeOfRemoved = removed.estimateSizeOf();
            watermark -= sizeOfRemoved;
        }

        watermark += sizeOfAdded;

    }

    @Override public long getWatermark() {
        return watermark;
    }

    @Override public String toString() {
        return "DictionaryEventBuffer";
    }

    @Override public int size() {
        return 0;
         
    }

    @Override public void extractIndexBetween(List<HistoricalIndexElement> index, long start, long end) {}

    @Override public void extractEventsBetween(Destination<LogEvent> visitor, long start, long end) {}

    @Override public void extractEventsBetween(List<LogEvent> matchingEvents, long start, long end) {}

    @Override public int getBlockSequence() {
        return 0;
         
    }

    @Override public void addIndexListener(Destination<HistoricalIndexElement> destination) {}

}
