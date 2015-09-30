package com.logginghub.logging.frontend.monitoringbus;

import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.HTMLBuilder;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class MinuteAggregator {

    private static Logger logger = Logger.getLoggerFor(MinuteAggregator.class);

    private FactoryMap<Long, SinglePassStatisticsDoublePrecision> perMinuteStats = new FactoryMap<Long, SinglePassStatisticsDoublePrecision>() {
        protected SinglePassStatisticsDoublePrecision createEmptyValue(Long key) {
            return new SinglePassStatisticsDoublePrecision();
        }
    };

    private FactoryMap<Long, MutableIntegerValue> perSecondCounts = new FactoryMap<Long, MutableIntegerValue>() {
        protected MutableIntegerValue createEmptyValue(Long key) {
            return new MutableIntegerValue(key.toString(), 0);
        }
    };

    public void add(double value) {
        long time = System.currentTimeMillis();

        long period = TimeUtils.chunk(time, TimeUtils.minutes);
        perMinuteStats.get(period).addValue(value);

        long second = TimeUtils.chunk(time, TimeUtils.seconds);
        perSecondCounts.get(second).increment(1);
    }

    public void render(HTMLBuilder htmlBuilder) {

        htmlBuilder.table();
        htmlBuilder.tr().th("Time").th("Count").th("Mean").th("Min").th("Max").th("tp90").th("1secMA peak").th("3secMA peak").th("3secMA trough").endTr();

        Set<Long> keySet = perMinuteStats.keySet();
        List<Long> sorted = new ArrayList<Long>();
        sorted.addAll(keySet);
        Collections.sort(sorted);

        Collection<MutableIntegerValue> values = perSecondCounts.values();
        List<MutableIntegerValue> perSeconds = new ArrayList<MutableIntegerValue>();
        perSeconds.addAll(values);
        Collections.sort(perSeconds, new Comparator<MutableIntegerValue>() {
            public int compare(MutableIntegerValue o1, MutableIntegerValue o2) {
                long a = Long.parseLong(o1.key.toString());
                long b = Long.parseLong(o2.key.toString());
                return CompareUtils.compare(a, b);
            }
        });

        for (Long time : sorted) {
            SinglePassStatisticsDoublePrecision stats = perMinuteStats.get(time);
            stats.doCalculations();
            htmlBuilder.tr().td(Logger.toDateString(time)).tdFormat(stats.getCount()).tdFormat(stats.getMean())
                    .tdFormat(stats.getMinimum()).tdFormat(stats.getMaximum()).tdFormat(stats.getPercentiles()[90]);

            long start = time;
            long end = start + TimeUtils.minutes;

            double oneSecHigh = 0;
            double highest = 0;
            double lowest = Double.MAX_VALUE;
            MovingAverage threeSecMA = new MovingAverage(3);
            long last = -1;

            int[] minuteData = new int[60];

            for (MutableIntegerValue value : perSeconds) {
                long a = Long.parseLong(value.key.toString());
                if (a >= start && a < end) {

                    int index = (int) ((a - start) / 1000);
                    minuteData[index] = value.value;
                }
            }

            for (int i = 0; i < minuteData.length; i++) {
                int value2 = minuteData[i];
                threeSecMA.addValue(value2);
                double foo = threeSecMA.calculateMovingAverage();
                if (foo > highest) {
                    highest = foo;
                }

                if (foo < lowest) {
                    lowest = foo;
                }
                
                if(value2 > oneSecHigh) {
                    oneSecHigh = value2;
                }
            }

            htmlBuilder.tdFormat(oneSecHigh).tdFormat(highest).tdFormat(lowest).endTr();

        }

        htmlBuilder.endTable();
    }

}
