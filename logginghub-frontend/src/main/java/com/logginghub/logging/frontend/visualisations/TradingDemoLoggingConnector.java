package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.monitoringbus.MonitoringBus;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.TimerUtils;

public class TradingDemoLoggingConnector {

    private ColourInterpolation colourInterpolation = new ColourInterpolation(ColourUtils.newColourWithAlpha(Color.blue.darker(), 0.8f),
            Color.blue, Color.green, Color.yellow, ColourUtils.newColourWithAlpha(Color.red,0.5f));

    private int perSecondCount = 0;
    private MovingAverage movingAverage = new MovingAverage(3);
    private double ma;

    public void start(final Generator generator) {
        MonitoringBus bus = new MonitoringBus();
        bus.addHub("localhost", 58770);
        
        bus.getEventStream().addListener(new StreamListener<LogEvent>() {
            public void onNewItem(LogEvent event) {

                double value = Double.NaN;
                String message = event.getMessage();
                if (message.startsWith("Process trade completed")) {
                    int pos = 42;
                    int end = message.indexOf(' ', pos);

                    String time = message.substring(pos, end);
                    value = Double.parseDouble(time);
                }

                if (!Double.isNaN(value)) {
                    
                    if(value > 200) {
//                        System.out.println(message);
                    }
                    
                    double fraction = value / 100;

                    int mulitplier = 1;

                    perSecondCount++;

                    int size;
                    if (value < 5) {
                        size = 1;
                    } else if (value < 10) {
                        size = 2;
                    } else if (value < 20) {
                        size = 2;
                    } else if (value < 30) {
                        size = 3;
                    } else if (value < 40) {
                        size = 3;
                    } else if (value < 50) {
                        size = 4;
                    } else if (value < 60) {
                        size = 4;
                    } else {
                        size = 6;
                    }

                    // 0.1 is enough to hit the top of hte screen
                    // 20k tps is the peak

                    double velocity = ma / 150000f;
                    // System.out.println(velocity);
                    // velocity = 0.03;

                    if (velocity > 0.1) {
                        velocity = 0.1;
                    }

                    velocity = 0.12;

                    for (int i = 0; i < mulitplier; i++) {
                        Color background = colourInterpolation.interpolate(fraction);
                        generator.generate(background, fraction, velocity, size);
                    }
                }
            }
        });
        bus.start();

        TimerUtils.everySecond("PerSecondTimer", new Runnable() {
            public void run() {
                movingAverage.addValue(perSecondCount);
//                System.out.println(perSecondCount);
                ma = movingAverage.calculateMovingAverage();
                perSecondCount = 0;

            }
        });

    }
}
