package com.logginghub.logging.modules.web;

import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.TimeKey;
import com.logginghub.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 02/10/15.
 */
public class DatabaseHelper {
    private final LoggingHubDatabase database;

    public DatabaseHelper(LoggingHubDatabase database) {
        this.database = database;
    }

    public List<SummaryStatistics> query(TimeKey key, long intervalLength, int count) {

        List<SummaryStatistics> results = new ArrayList<SummaryStatistics>();
        for (int i = 0; i < count; i++) {

            SummaryStatistics stats;

            if (intervalLength == TimeUtils.milliseconds) {
                stats = database.getMillisecondlyStats(key.year, key.month, key.day, key.hour, key.minute, key.second, i);
            } else if (intervalLength == TimeUtils.seconds) {
                stats = database.getSecondlyStats(key.year, key.month, key.day, key.hour, key.minute, i);
            } else if (intervalLength == TimeUtils.minutes) {
                stats = database.getMinutelyStats(key.year, key.month, key.day, key.hour, i);
            } else if (intervalLength == TimeUtils.hours) {
                stats = database.getHourlyStats(key.year, key.month, key.day, i);
            } else if (intervalLength == TimeUtils.days) {
                stats = database.getDailyStats(key.year, key.month, i);
            } else  {
                throw new NotImplementedException("Don't know how to find data from the database for interval length '{}'", intervalLength);
            }

            results.add(stats);
        }

        return results;
    }

    public List<SingleSeriesSummaryStatistics> extractPatternStats(int patternId, List<SummaryStatistics> stats) {
        List<SingleSeriesSummaryStatistics> singleSeries = new ArrayList<SingleSeriesSummaryStatistics>(stats.size());
        for (SummaryStatistics stat : stats) {

            long count;
            Long patternCount = stat.getPatternCounts().get(patternId);
            if(patternCount != null) {
                count = patternCount;
            }else{
                count = 0;
            }

            singleSeries.add(new SingleSeriesSummaryStatistics(stat.getTime(), stat.getIntervalLength(), count));

        }
        return singleSeries;
    }

    public List<SingleSeriesSummaryStatistics> extractOverallStats(List<SummaryStatistics> stats) {
        List<SingleSeriesSummaryStatistics> singleSeries = new ArrayList<SingleSeriesSummaryStatistics>(stats.size());
        for (SummaryStatistics stat : stats) {
            long count = stat.getCount();
            singleSeries.add(new SingleSeriesSummaryStatistics(stat.getTime(), stat.getIntervalLength(), count));

        }
        return singleSeries;
    }
}
