package com.logginghub.logging.modules.web;

import com.google.gson.Gson;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.datafiles.SummaryTimeElement;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeKey;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by james on 01/10/15.
 */
public class FirstCutLoggingHubDatabase implements LoggingHubDatabase {

    private static final Logger logger = Logger.getLoggerFor(FirstCutLoggingHubDatabase.class);
    private final File baseFolder;

    private List<Long> intervalsToMaintain = new ArrayList<Long>();

    private Map<String, DirtyFile<?>> cache = new HashMap<String, DirtyFile<?>>();
    private List<DirtyFile> dirtyFiles = new ArrayList<DirtyFile>();
    private Gson gson = new Gson();

    public FirstCutLoggingHubDatabase(File baseFolder) {
        this.baseFolder = baseFolder;
        baseFolder.mkdirs();

        intervalsToMaintain.add(TimeUtils.milliseconds);
        intervalsToMaintain.add(TimeUtils.seconds);
        intervalsToMaintain.add(TimeUtils.minutes(1));
        intervalsToMaintain.add(TimeUtils.hours);
        intervalsToMaintain.add(TimeUtils.days);
    }

    public void flushDirtyFiles() {
        synchronized (dirtyFiles) {
            for (DirtyFile dirtyFile : dirtyFiles) {
                dirtyFile.flush();
            }
        }
    }

    @Override
    public SummaryStatistics getDailyStats(int year, int month, int day) {
        return getIntervalStats(TimeUtils.days, year, month, day, 0, 0, 0, 0);
    }

    @Override
    public SummaryStatistics getHourlyStats(int year, int month, int day, int hour) {
        return getIntervalStats(TimeUtils.hours, year, month, day, hour, 0, 0, 0);
    }

    @Override
    public SummaryStatistics getIntervalStats(long interval, TimeKey timeKey) {
        String key = toFileKey(interval, timeKey);
        return getSummaryStatistics(key);
    }

    private String toFileKey(long interval, TimeKey timeKey) {
        return StringUtils.format("{}/{}/{}/{}/{}/{}/{}/{}",
                                  toInterval(interval),
                                  timeKey.year,
                                  timeKey.month,
                                  timeKey.day,
                                  timeKey.hour,
                                  timeKey.minute,
                                  timeKey.second,
                                  timeKey.millisecond);
    }

    @Override
    public SummaryStatistics getMinutelyStats(int year, int month, int day, int hour, int minute) {
        return getIntervalStats(TimeUtils.minutes, year, month, day, hour, minute, 0, 0);
    }

    @Override
    public SummaryStatistics getSecondlyStats(int year, int month, int day, int hour, int minute, int second) {
        return getIntervalStats(TimeUtils.seconds, year, month, day, hour, minute, second, 0);
    }

    @Override
    public SummaryStatistics getMillisecondlyStats(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        return getIntervalStats(TimeUtils.milliseconds, year, month, day, hour, minute, second, millisecond);
    }

    @Override
    public SummaryStatistics getIntervalStats(long interval, int year, int month, int day, int hour, int minute, int second, int millisecond) {
        TimeKey from = TimeKey.from(year, month, day, hour, minute, second, millisecond);
        String key = toFileKey(interval, from);
        SummaryStatistics summaryStatistics = getSummaryStatistics(key);
        if(summaryStatistics == null) {
            // Return a blank result
            summaryStatistics = new SummaryStatistics(toTime(from), interval);
        }
        return summaryStatistics;
    }

    private long toTime(TimeKey from) {
        Calendar calendar = TimeUtils.calendar(from);
        return calendar.getTimeInMillis();
    }

    @Override
    public void updateFrom(LogEvent event) {

    }

    @Override
    public void updateFrom(PatternisedLogEvent event) {

    }

    private String toInterval(long intervalLength) {
        return TimeUtils.formatIntervalMilliseconds(intervalLength).replace(" ", "");
    }

    private SummaryStatistics getSummaryStatistics(String key) {
        File file = new File(baseFolder, key);
        SummaryStatistics summaryStatistics = null;
        if (file.exists()) {
            logger.info("Looking up stats for '{}' - exists", key);
            try {
                summaryStatistics = gson.fromJson(new FileReader(file), SummaryStatistics.class);
            } catch (FileNotFoundException e) {
                throw new FormattedRuntimeException(e, "Failed to load file '{}'", file.getAbsolutePath());
            }
        } else {
            logger.info("Looking up stats for '{}' - doesn't exist", key);
        }

        return summaryStatistics;
    }

    public void updateFrom(SummaryTimeElement summaryTimeElement) {

        long time = summaryTimeElement.getTime();
        long interval = summaryTimeElement.getIntervalLength();

        // TODO : what if the interval doesn't fit with our intervals to maintain?
        for (Long targetInterval : intervalsToMaintain) {

            if (targetInterval >= interval) {

                long intervalIndex = TimeUtils.chunk(time, targetInterval);

                try {
                    DirtyFile<SummaryStatistics> file = loadSummaryStats(intervalIndex, targetInterval);
                    file.object.updateFrom(summaryTimeElement);
                    dirtyFiles.add(file);
                } catch (FileNotFoundException e) {
                    logger.warn(e, "Failed to load summary statistics");
                }

            }

        }

    }

    private DirtyFile<SummaryStatistics> loadSummaryStats(long intervalIndex, long intervalLength) throws FileNotFoundException {

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(intervalIndex);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);

        SummaryStatistics summaryStatistics;

        String key = toFileKey(intervalLength, TimeKey.from(year, month, day, hour, minute, second, millisecond));

        DirtyFile<?> dirtyFile = cache.get(key);
        if (dirtyFile == null) {

            File file = new File(baseFolder, key);
            if (file.exists()) {
                summaryStatistics = gson.fromJson(new FileReader(file), SummaryStatistics.class);
            } else {
                summaryStatistics = new SummaryStatistics(intervalIndex, intervalLength);
            }
            dirtyFile = new DirtyFile(key, file, summaryStatistics);
            cache.put(key, dirtyFile);
        }

        return (DirtyFile<SummaryStatistics>) dirtyFile;


    }

    class DirtyFile<T> {
        String key;
        File file;
        T object;

        public DirtyFile(String key, File file, T object) {
            this.key = key;
            this.file = file;
            this.object = object;
        }

        public void flush() {
            FileUtils.write(gson.toJson(object), file);
        }
    }
}
