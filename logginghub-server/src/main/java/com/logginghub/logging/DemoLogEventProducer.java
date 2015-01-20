package com.logginghub.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.generator.LogEventProducer;
import com.logginghub.logging.generator.MessageProducer;
import com.logginghub.logging.generator.configs.TradeBlotterMessageConfig;
import com.logginghub.logging.generator.configs.TradingEntryProcessorMessageConfig;
import com.logginghub.logging.generator.configs.TradingMessageConfig;
import com.logginghub.logging.interfaces.AbstractLogEventSource;
import com.logginghub.utils.RandomWithMomentum;
import com.logginghub.utils.StringUtils;

/**
 * Produces dummy log events.
 * 
 * @author admin
 */
public class DemoLogEventProducer extends AbstractLogEventSource {

    private List<Timer> timers = new ArrayList<Timer>();

    double tradeRate = 10;
    double blotterRate = 0.2;

    double tradeCounter = 0;
    double blotterCounter = 0;
    double rateChangeCounter = 0;

    private LogEventProducer tradeEvents = new LogEventProducer("Appserver", 1, new MessageProducer(new TradingMessageConfig()));
    private LogEventProducer blotterEvents = new LogEventProducer("Appserver", 1, new MessageProducer(new TradeBlotterMessageConfig()));
    private LogEventProducer entryProcessorEvents = new LogEventProducer("Cacheserver", 4, new MessageProducer(new TradingEntryProcessorMessageConfig()));

    private RandomWithMomentum tradeRateChanger = new RandomWithMomentum(0, 1, 100, 5, 10);
    private RandomWithMomentum blotterRateChanger = new RandomWithMomentum(0, 1, 5, 5, 10);

    private long lastTime = -1;

    public DemoLogEventProducer() {

    }

    public void produceEvents(int count) {
        for (int i = 0; i < count; i++) {
            produceEvent();
        }
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

    public void produceEventsOnTimer(int count, long periodMS, int threads) {
        stop();

        for (int i = 0; i < threads; i++) {
            Timer timer = new Timer("Timer-" + i, true);
            timer.schedule(new TimerTask() {
                @Override public void run() {
                    produceEvent();
                }
            }, periodMS, periodMS);
            timers.add(timer);
        }
    }

    // //////////////////////////////////////////////////////////////////
    // Private methods
    // //////////////////////////////////////////////////////////////////

    private synchronized void produceEvent() {

        long time = System.currentTimeMillis();

        if (lastTime != -1) {

            long elapsed = time - lastTime;

            tradeCounter += elapsed;
            blotterCounter += elapsed;

            double tradeDelay = 1000d / tradeRate;

            while (tradeCounter > tradeDelay) {
                DefaultLogEvent tradeEvent = tradeEvents.produce();
                produceMatchingCacheEvent(tradeEvent);
                fireNewLogEvent(tradeEvent);
                tradeCounter -= tradeDelay;
            }

            double blotterDelay = 1000d / blotterRate;
            while (blotterCounter > blotterDelay) {
                fireNewLogEvent(blotterEvents.produce());
                blotterCounter -= blotterDelay;
            }

            rateChangeCounter += elapsed;
            if (rateChangeCounter > 1000) {
                tradeRate = tradeRateChanger.next();
                blotterRate = blotterRateChanger.next();
                rateChangeCounter -= 1000;
            }

        }

        lastTime = time;
    }

    private void produceMatchingCacheEvent(DefaultLogEvent tradeEvent) {
        String message = tradeEvent.getMessage();
        String searchString = "txid was '";
        String txID = StringUtils.between(message, searchString, "'");
        
        DefaultLogEvent produced = entryProcessorEvents.produce();
        
        String cacheMessage = produced.getMessage();
        int index = cacheMessage.indexOf(searchString);
        int end = cacheMessage.indexOf('\'', index + searchString.length());
        
        String hacked = cacheMessage.substring(0, index + searchString.length());
        hacked += txID;
        hacked += cacheMessage.substring(end, cacheMessage.length());
        
        produced.setMessage(hacked);
        
        fireNewLogEvent(produced);
        
        
    }
}
