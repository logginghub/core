package com.logginghub.logging.modules.web;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.TimeKey;

/**
 * Created by james on 01/10/15.
 */
public interface LoggingHubDatabase {

    SummaryStatistics getDailyStats(int year, int month, int day);

    SummaryStatistics getHourlyStats(int year, int month, int day, int hour);

    SummaryStatistics getIntervalStats(long interval, TimeKey timeKey);

    SummaryStatistics getMinutelyStats(int year, int month, int day, int hour, int minute);

    SummaryStatistics getSecondlyStats(int year, int month, int day, int hour, int minute, int second);

    SummaryStatistics getMillisecondlyStats(int year, int month, int day, int hour, int minute, int second, int millisecond);

    SummaryStatistics getIntervalStats(long interval, int year, int month, int day, int hour, int minute, int second, int millisecond);

    void updateFrom(LogEvent event);

    void updateFrom(PatternisedLogEvent event);

    class DailyStats {

    }
}
