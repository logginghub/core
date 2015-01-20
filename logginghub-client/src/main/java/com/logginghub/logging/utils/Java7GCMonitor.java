package com.logginghub.logging.utils;

import com.logginghub.utils.Destination;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.StringUtils;
import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.Map.Entry;

public class Java7GCMonitor {

    private long totalGcDuration = 0;
    private long churn = 0;
    private Multiplexer<GCEvent> eventMultiplexer = new Multiplexer<GCEvent>();
    private Map<NotificationEmitter, NotificationListener> listeners = new HashMap<NotificationEmitter, NotificationListener>();

    public long getTotalGcDuration() {
        return totalGcDuration;
    }

    public long getChurn() {
        return churn;
    }

    public void resetStats() {
        totalGcDuration = 0;
        churn = 0;
    }

    public boolean isSupported() {
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();

        boolean foundAtLeastOne = false;

        for (GarbageCollectorMXBean gcbean : gcbeans) {
            if (gcbean instanceof NotificationEmitter) {
                foundAtLeastOne = true;
                break;
            }
        }

        return foundAtLeastOne;
    }

    public void installGCMonitoring() {

        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();

        boolean foundAtLeastOne = false;

        for (GarbageCollectorMXBean gcbean : gcbeans) {

            if (gcbean instanceof NotificationEmitter) {
                NotificationEmitter emitter = (NotificationEmitter) gcbean;

                NotificationListener listener = new NotificationListener() {

                    @Override public void handleNotification(Notification notification, Object handback) {
                        if (notification.getType()
                                        .equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {

                            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification
                                    .getUserData());

                            String gctype = info.getGcAction();


                            long totalUsedBefore = 0;
                            long totalUsedAfter = 0;

                            // Get the information about each memory space, and pretty print it
                            Map<String, MemoryUsage> membefore = info.getGcInfo().getMemoryUsageBeforeGc();
                            Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
                            for (Entry<String, MemoryUsage> entry : mem.entrySet()) {

                                String name = entry.getKey();
                                MemoryUsage after = entry.getValue();

                                MemoryUsage before = membefore.get(name);

                                totalUsedAfter += after.getUsed();
                                totalUsedBefore += before.getUsed();

                            }

                            long totalCollected = totalUsedBefore - totalUsedAfter;
                            churn += totalCollected;
                            totalGcDuration += info.getGcInfo().getDuration();

                            GCEvent event = new GCEvent();
                            event.bytes = totalCollected;
                            event.duration = info.getGcInfo().getDuration();
                            event.type = info.getGcAction();
                            event.cause = info.getGcCause();
                            event.name = info.getGcName();
                            eventMultiplexer.send(event);

                        }
                    }
                };

                emitter.addNotificationListener(listener, null, null);
                listeners.put(emitter, listener);
                foundAtLeastOne = true;
            }
        }

        if (!foundAtLeastOne) {
            System.err.println(
                    "WARNING - LoggingHub GCMonitor - failed to find any suitable GC providers to listen to - are you running on a pre Java 7 VM?");
        }

    }

    public void uninstall() {
        Set<Entry<NotificationEmitter, NotificationListener>> entrySet = listeners.entrySet();
        for (Entry<NotificationEmitter, NotificationListener> entry : entrySet) {
            try {
                entry.getKey().removeNotificationListener(entry.getValue());
            } catch (ListenerNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Multiplexer<GCEvent> getEventMultiplexer() {
        return eventMultiplexer;
    }

    public static class GCEvent {
        public long duration;
        public long bytes;
        public String type;
        public String name;
        public String cause;

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("GCEvent{");
            sb.append("duration=").append(duration);
            sb.append(", bytes=").append(bytes);
            sb.append(", type=").append(type);
            sb.append(", name=").append(name);
            sb.append(", cause=").append(cause);

            sb.append('}');
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        Java7GCMonitor monitor = new Java7GCMonitor();
        monitor.getEventMultiplexer().addDestination(new Destination<GCEvent>() {
            @Override public void send(GCEvent gcEvent) {
                System.out.println(gcEvent);
            }
        });
        monitor.installGCMonitoring();

        List<String> overflow = new ArrayList<String>();

        while (true) {
            overflow.add(StringUtils.randomString(10000));
        }
    }
}
