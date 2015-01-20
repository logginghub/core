package com.logginghub.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.JuliLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.interfaces.AbstractLogEventSource;
import com.logginghub.utils.RandomWithMomentum;
import com.logginghub.utils.StringUtils;

/**
 * Produces dummy log events.
 * 
 * @author admin
 */
public class DummyLogEventProducer extends AbstractLogEventSource {

    private String applicationName = "TestApplication";
    private int nextEvent = 0;
    private List<Timer> timers = new ArrayList<Timer>();

    private RandomWithMomentum randomOne = new RandomWithMomentum(0, 2, 30, 5, 10);
    private Random random = new Random();

    public DummyLogEventProducer() {

    }

    public DummyLogEventProducer(String applicationName) {
        setApplicationname(applicationName);
    }

    public void produceEvents(int count) {
        for (int i = 0; i < count; i++) {
            produceEvent();
        }
    }

    public void setApplicationname(String applicationname) {
        this.applicationName = applicationname;
    }

    public void start() {
        produceEventsOnTimer(1, 100, 2);
    }

    public void stop() {
        for (Timer timer : timers) {
            timer.cancel();
        }

        timers.clear();
    }

    public void produceEventsOnTimer(final int count, long periodMS, int threads) {
        stop();

        for (int i = 0; i < threads; i++) {
            Timer timer = new Timer("Timer-" + i, true);
            timer.schedule(new TimerTask() {
                @Override public void run() {
                    for (int j = 0; j < count; j++) {
                        produceEvent();
                    }
                }
            }, periodMS, periodMS);
            timers.add(timer);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private synchronized void produceEvent() {
        nextEvent++;
        if (nextEvent == 10) {
            nextEvent = 0;
        }

        String[] hosts = new String[] { "host1.subdomain.com",
                                       "host2.subdomain.com",
                                       "host3.subdomain.com",
                                       "host4.subdomain.com",
                                       "host5.subdomain.com",
                                       "host6.subdomain.com",
                                       "host7.subdomain.com",
                                       "host8.subdomain.com",
                                       "host9.subdomain.com",
                                       "host10.subdomain.com", };

        DefaultLogEvent event;

        switch (nextEvent) {
            case 0: {
                event = LogEventFactory.createFullLogEvent1(applicationName);
                event.setLevel(Level.FINEST.intValue());
                break;
            }
            case 1: {
                event = LogEventFactory.createFullLogEvent2(applicationName);
                event.setLevel(Level.FINER.intValue());
                break;
            }
            case 2: {
                event = LogEventFactory.createFullLogEvent3(applicationName);
                event.setLevel(Level.FINE.intValue());
                break;
            }
            case 3: {
                event = LogEventFactory.createFullLogEvent1(applicationName);
                event.setLevel(Level.INFO.intValue());
                break;
            }
            case 4: {
                event = LogEventFactory.createFullLogEvent2(applicationName);
                event.setLevel(Level.CONFIG.intValue());
                break;
            }
            case 5: {
                event = LogEventFactory.createFullLogEvent3(applicationName);
                event.setLevel(Level.WARNING.intValue());
                break;
            }
            case 6: {
                event = LogEventFactory.createFullLogEventBig(applicationName);
                event.setLevel(Level.SEVERE.intValue());
                RuntimeException e = new RuntimeException("Oh my god, an exception!");
                e.fillInStackTrace();
                event.setFormattedException(JuliLogEvent.formatException(e));
                break;
            }
            case 7: {
                event = LogEventFactory.createFullLogEvent1(applicationName);
                event.setMessage("This line has the keyword Orange inside it to demonstrate the default phrase highlighter");
                event.setLevel(Level.INFO.intValue());
                break;
            }
            case 8: {
                event = LogEventFactory.createFullLogEvent1(applicationName);
                event.setMessage("This line has the keyword Apple inside it to demonstrate the regex phrase highlighter");
                event.setLevel(Level.INFO.intValue());
                break;
            }
            case 9: {
                event = LogEventFactory.createFullLogEvent1(applicationName);
                event.setSourceHost(hosts[random.nextInt(hosts.length)]);
                event.setMessage(StringUtils.format("random operation A completed successfully in {} ms", randomOne.next()));
                event.setLevel(Level.INFO.intValue());
                break;
            }
            default: {
                throw new RuntimeException("Dodgy bug in the event producer method");
            }
        }

        fireNewLogEvent(event);

        // fireNewLogEvent(tradeEvents.produce());
    }
}
