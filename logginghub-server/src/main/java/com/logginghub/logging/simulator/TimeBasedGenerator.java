package com.logginghub.logging.simulator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.LERP;
import com.logginghub.utils.Multiplexer;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public class TimeBasedGenerator {

    private String[] clients = new String[] { "ClientA", "ClientB", "ClientC", "ClientD" };

    private Random random = new Random();
    private static NumberFormat numberFormat = NumberFormat.getInstance();
    private TimeProvider timeProvider = new SystemTimeProvider();
    private Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

    private double currentDelayNanos = 0.125 * 1e6;;

    private volatile int count = 0;

    private Multiplexer<LogEvent> eventMultiplexer = new Multiplexer<LogEvent>();
    private double factor = 1;
    private WorkerThread targetRateThread;
    private WorkerThread statsThread;
    private WorkerThread generatorThread;

    public TimeBasedGenerator() {
        setupRanges();
    }

    public Multiplexer<LogEvent> getEventMultiplexer() {
        return eventMultiplexer;
    }

    class Range {
        int rangeMin;
        int rangeMax;

        double valueMin;
        double valueMax;

        int[] randomisationDistribution;

        Random random = new Random();

        LERP lerp;

        public Range(int rangeMin, int rangeMax, double valueMin, double valueMax, int[] randomisationDistribution) {
            super();
            this.rangeMin = rangeMin;
            this.rangeMax = rangeMax;
            this.valueMin = valueMin;
            this.valueMax = valueMax;
            this.randomisationDistribution = randomisationDistribution;
            lerp = new LERP(randomisationDistribution);
            // for(int i = 0; i < 11; i++) {
            // System.out.println(i + " : " + lerp.lerp(i / 10d));
            // }
        }

        public boolean contains(double position) {
            return position >= rangeMin && position < rangeMax;

        }

        public double value(double position) {
            double relativePosition = position - rangeMin;
            double positionFactor = relativePosition / ((double) rangeMax - rangeMin);
            double positionValue = valueMin + (positionFactor * (valueMax - valueMin));
            double randomisationFactor = lerp.lerp(positionFactor);
            double halfRandomisationFactor = randomisationFactor / 2;
            double randomValue = -halfRandomisationFactor + (random.nextDouble() * randomisationFactor);
            double finalValue = positionValue + randomValue;
            return finalValue;
        }
    }

    private List<Range> ranges = new ArrayList<Range>();

    public void setupRanges() {
        ranges.add(new Range(0, 100, 1, 4, new int[] { 0 }));
        ranges.add(new Range(100, 500, 4, 10, new int[] { 0, 0, 2 }));
        ranges.add(new Range(500, 1000, 10, 20, new int[] { 2, 3, 4 }));
        ranges.add(new Range(1000, 2000, 20, 50, new int[] { 5, 6, 7 }));
        ranges.add(new Range(2000, 5000, 50, 1000, new int[] { 8, 9, 10 }));
        ranges.add(new Range(5000, 5000000, 1000, 2000, new int[] { 100, 100 }));

        // 0, 100 = 1-4 ms +/- 1ms [1,1,1,1,1,1,1,1,1]
        // 100, 500 = 4-10ms +/- 2ms [1,1,1,1,1,1,1,1,1]
        // 500, 1000 = 10-20ms +/- 8ms [1,1,1,1,1,1,1,1,1]
        // 1000, 2000 = 20 - 50 ms +/- 15 ms [1,1,1,1,1,1,1,1,1]
        // 2000, 5000 = 50 - 1000 ms +/- 30 ms [1,1,1,1,1,1,1,1,500]

    }

    public double getDuration(double position) {

        double value = -1;

        for (Range range : ranges) {
            if (range.contains(position)) {
                value = range.value(position);
                break;
            }
        }

        return value;

    }

    public double getDuration() {

        double value = 1;

        int position = (int) (1e9 / currentDelayNanos);
        for (Range range : ranges) {
            if (range.contains(position)) {
                value = range.value(position);
                break;
            }
        }

        return value;

    }

    public void startGeneratorThread() {

        generatorThread = WorkerThread.executeOngoing("LoggingHub-TimeBasedGenerator-Worker", new Runnable() {
            @Override public void run() {
                long start = System.nanoTime();
                generate(timeProvider.getTime());

                long elapsed = System.nanoTime() - start;

                double incrementalDelay = currentDelayNanos - elapsed;

                try {
                    ThreadUtils.sleepNanos((long) (incrementalDelay - 1590));
                }
                catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
        });

    }

    public void startStatsThread() {
        statsThread = WorkerThread.everySecondDaemon("LoggingHub-TimeBasedGenerator-Stats", new Runnable() {
            @Override public void run() {
                Out.out("{} | {}", Logger.toDateString(timeProvider.getTime()), count);
                count = 0;
            }
        });
    }

    public void startTargetRateThread() {
        targetRateThread = WorkerThread.everySecond("LoggingHub-TimeBasedGenerator-TargetRate", new Runnable() {
            @Override public void run() {
                updateTargetRate();

            }
        });
    }

    private void updateTargetRate() {
        int target = getCountChars();
        currentDelayNanos = 1e9 / target / factor;
    }

    public int getCount() {

        long time = timeProvider.getTime();
        calendar.setTimeInMillis(time);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        int total = year + month + day + hour + minute + seconds;

        return total;

    }

    public int getCountMax() {
        int max = 2 + 0 + 1 + 9 + 9 + 2 + 9 + 2 + 9 + 5 + 9 + 5 + 9;
        return max;
    }

    // private int getYear() {
    // long time = timeProvider.getTime();
    // calendar.setTimeInMillis(time);
    // int year = calendar.get(Calendar.YEAR);
    // return year;
    // }

    public int getCountChars() {

        long time = timeProvider.getTime();
        String formatUTC = TimeUtils.formatUTC(time);
        byte[] bytes = formatUTC.getBytes();

        int count = 0;
        for (byte b : bytes) {
            int value = b - '0';
            count += value;
        }

        return count;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        updateTargetRate();
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    public void setScaleFactor(double factor) {
        this.factor = factor;
        updateTargetRate();
    }

    public double getFactor() {
        return factor;
    }

    public void stop() {
        if (generatorThread != null) {
            generatorThread.stop();
            generatorThread = null;
        }

        if (statsThread != null) {
            statsThread.stop();
            statsThread = null;
        }

        if (targetRateThread != null) {
            targetRateThread.stop();
            targetRateThread = null;
        }
    }

    public void generate(long from, long to) {
        FixedTimeProvider fixedTimeProvider = new FixedTimeProvider(from);
        setTimeProvider(fixedTimeProvider);

        while (fixedTimeProvider.getTime() < to) {

            int countChars = getCountChars();
            double increment = 1000d / countChars;

            // Need to set this to do the duration calculation
            currentDelayNanos = increment * 1e6;

            long millis = fixedTimeProvider.getTime();
            for (int i = 0; i < countChars; i++) {
                generate(millis);
                millis += increment;
            }

            fixedTimeProvider.increment(1000);
        }
    }

    public void generate(long time) {

        int level = Logger.info;
        double duration = getDuration();
        String formatted = format(duration, 1);
        String client = clients[random.nextInt(clients.length)];
        String message = StringUtils.format("Trade successfully processed in {} ms : instrument was EURUSD Spot, user account was '{}' and counterparty account 'FX Desk'",
                                            formatted,
                                            client);
        DefaultLogEvent event = LogEventBuilder.create(time, level, message);
        eventMultiplexer.send(event);
        count++;

    }

    // From :
    // http://stackoverflow.com/questions/10553710/fast-double-to-string-conversion-with-given-precision
    private static final int POW10[] = { 1, 10, 100, 1000, 10000, 100000, 1000000 };

    public static String format(double val, int precision) {
        StringBuilder sb = new StringBuilder();
        if (val < 0) {
            sb.append('-');
            val = -val;
        }
        int exp = POW10[precision];
        long lval = (long) (val * exp + 0.5);
        sb.append(lval / exp).append('.');
        long fval = lval % exp;
        for (int p = precision - 1; p > 0 && fval < POW10[p]; p--) {
            sb.append('0');
        }
        sb.append(fval);
        return sb.toString();
    }
}
