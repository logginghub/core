package com.logginghub.utils;

import com.logginghub.utils.logging.LogEvent;
import com.logginghub.utils.logging.Logger;

import java.text.NumberFormat;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class StatBundle {

    private List<Stat> stats = new CopyOnWriteArrayList<Stat>();
    private Timer timer;
    private String prefix = "Stats : ";

    public StatBundle() {

    }

    public StatBundle(String prefix) {
        this.prefix = prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public Stat addStat(Stat stat) {
        stats.add(stat);
        return stat;
    }

    public IntegerStat createIncremental(String string) {
        IntegerStat createStat = createStat(string);
        createStat.setIncremental(true);
        return createStat;
    }

    public IntegerStat createStat(String name) {
        IntegerStat integerStat = new IntegerStat();
        integerStat.setName(name);
        integerStat.setValue(0);
        stats.add(integerStat);
        return integerStat;
    }

    public boolean hasChanged() {
        boolean hasChanged = false;
        for (Stat stat : stats) {
            hasChanged |= stat.hasChanged();
            if (hasChanged) {
                break;
            }
        }
        return hasChanged;
    }

    public String formatValues() {

        NumberFormat nf = NumberFormat.getInstance();

        StringBuilder builder = new StringBuilder();
        String div = "";
        for (Stat stat : stats) {
            // builder.append(div).append(stat.getName()).append("=").append(nf.format(stat.getValue()));
            builder.append(div).append(nf.format(stat.getValue())).append(" ").append(stat.getName());
            div = ", ";
        }

        return builder.toString();
    }

    public void reset() {
        for (Stat stat : stats) {
            stat.reset();
        }
    }

    public void startPerSecond(final Logger logger) {
        final LogEvent context = logger.getCallerContext();

        this.timer = TimerUtils.everySecond("StatsTimer", new Runnable() {
            public void run() {
                if (hasChanged()) {
                    logger.logFromContext(context, Level.INFO.intValue(), "{}{}", prefix, formatValues());
                }
                reset();
            }
        });
    }

    public void startPerSecond(final java.util.logging.Logger logger) {
        this.timer = TimerUtils.everySecond("StatsTimer", new Runnable() {
            public void run() {
                if (hasChanged()) {
                    logger.log(Level.INFO, StringUtils.format("{}{}", prefix, formatValues()));
                }
                reset();
            }
        });
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void addStats(IntegerStat... stats) {
        for (IntegerStat integerStat : stats) {
            this.stats.add(integerStat);
        }
    }

    public void createHeapStat() {
        IntegerStat heapStat = new IntegerStat("% heap", 0) {
            @Override public int getValue() {
                MemorySnapshot snapshot = MemorySnapshot.createSnapshot();
                return (int) snapshot.getAvailableMemoryPercentage();
            }
        };

        addStats(heapStat);
    }

}
