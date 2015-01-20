package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.monitoringbus.MonitoringBus;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.TimerUtils;

public class DemoLoggingConnector {

    private ColourInterpolation colourInterpolation = new ColourInterpolation(ColourUtils.newColourWithAlpha(Color.blue.darker(), 0.8f),
                                                                              Color.blue,
                                                                              Color.green,
                                                                              Color.yellow,
                                                                              ColourUtils.newColourWithAlpha(Color.red, 0.5f));

    private int perSecondCount = 0;
    private MovingAverage movingAverage = new MovingAverage(3);
    private double ma;

    private MonitoringBus bus;

    public DemoLoggingConnector() {}

    public MonitoringBus getBus() {
        return bus;
    }

    public void start(final Generator generator) {
        bus = new MonitoringBus();
        bus.addDemoSource();

        bus.addPattern("TradeEntryProcessor", "TradeEntryProcessor completed successfully in {time} ms :  txid was '[tid]'");
        bus.addPattern("ProcessTrade",
                       "ProcessTrade completed successfully in {time} ms :  txid was '[tid]', account was '[account]', instrument was '[instrument]', quantity was '{quantity}', status was '[status]'");

        // bus.getPatternisedStream().addListener(new StreamListener<PatternisedEvent>() {
        // @Override public void onNewItem(PatternisedEvent t) {
        // System.out.println(t.getPatternName());
        // }
        // });

        bus.getEventStream().addListener(new StreamListener<LogEvent>() {
            public void onNewItem(LogEvent event) {
                if (event.getSourceHost().endsWith("1")) {
                    // Out.out("{} | {} | {}", event.getSourceHost(), event.getSourceApplication(),
                    // event.getMessage());
                }
            }
        });
        bus.start();

        TimerUtils.everySecond("PerSecondTimer", new Runnable() {
            public void run() {
                movingAverage.addValue(perSecondCount);
                // System.out.println(perSecondCount);
                ma = movingAverage.calculateMovingAverage();
                perSecondCount = 0;

            }
        });

    }
}
