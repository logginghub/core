package com.logginghub.logging.modules.history;

import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatterniserModule;
import com.logginghub.logging.modules.configuration.PatterniserConfiguration;
import com.logginghub.utils.CircularArrayList;
import com.logginghub.utils.Destination;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.Pair;
import com.logginghub.utils.module.ProxyServiceDiscovery;

public class PatternisedEventBuffer implements EventBuffer {

    private PatterniserModule patterniser;
    private int highWaterMark;
    private long watermark = 0;
    private CircularArrayList<Pair<DefaultLogEvent, PatternisedLogEvent>> events = new CircularArrayList<Pair<DefaultLogEvent, PatternisedLogEvent>>(20000);

    public PatternisedEventBuffer(final int highWaterMark) {
        this.highWaterMark = highWaterMark;
        patterniser = new PatterniserModule();
        PatterniserConfiguration patterniserConfiguration = JAXBConfiguration.loadConfiguration(PatterniserConfiguration.class, "patterniser.xml");
        patterniser.configure(patterniserConfiguration, new ProxyServiceDiscovery());
    }

    @Override public int countEvents() {
        return events.size();
    }

    @Override public int countBetween(long start, long end) {
        int count = 0;
        for (int i = 0; i < events.size(); i++) {
            Pair<DefaultLogEvent, PatternisedLogEvent> logEvent = events.get(i);

            long time;

            PatternisedLogEvent b = logEvent.getB();
            if (b == null) {
                DefaultLogEvent a = logEvent.getA();
                time = a.getOriginTime();
            }
            else {
                time = b.getTime();
            }

            if (time >= start && time < end) {
                count++;
            }
        }
        return count;
    }

    @Override public void addEvent(DefaultLogEvent t) {

        int sizeOfAdded;

        Pair<DefaultLogEvent, PatternisedLogEvent> union;
        PatternisedLogEvent patternised = patterniser.patternise(t);
        if (patternised != null) {
            union = new Pair<DefaultLogEvent, PatternisedLogEvent>(null, patternised);
            sizeOfAdded = patternised.estimateSizeOf();
        }
        else {
            union = new Pair<DefaultLogEvent, PatternisedLogEvent>(t, null);
            sizeOfAdded = t.estimateSizeOf();
        }

        events.add(union);

        while (sizeOfAdded + watermark > highWaterMark) {
            Pair<DefaultLogEvent, PatternisedLogEvent> removed = events.remove();
            long sizeOfRemoved;
            PatternisedLogEvent patternisedLogEvent = removed.getB();
            if (patternisedLogEvent != null) {
                sizeOfRemoved = patternisedLogEvent.estimateSizeOf();

            }
            else {
                sizeOfRemoved = removed.getA().estimateSizeOf();
            }

            watermark -= sizeOfRemoved;
        }

        watermark += sizeOfAdded;

    }

    @Override public long getWatermark() {
        return watermark;
    }

    @Override public String toString() {
        return "PatternisedEventBuffer";
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
