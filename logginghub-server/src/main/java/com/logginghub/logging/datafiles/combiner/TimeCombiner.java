package com.logginghub.logging.datafiles.combiner;

import com.logginghub.logging.datafiles.aggregation.TimeAggregation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.Out;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.logging.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by james on 17/09/15.
 */
public class TimeCombiner implements Destination<TimeAggregation> {

    private long time;
    private long intervalLength;
    private long count;

    private FactoryMap<String, IntegerFrequencyCount> frequencyCounts = new FactoryMap<String, IntegerFrequencyCount>() {
        @Override
        protected IntegerFrequencyCount createEmptyValue(String key) {
            return new IntegerFrequencyCount();
        }
    };

    private FactoryMap<String, SinglePassStatisticsDoublePrecision> statistics = new FactoryMap<String, SinglePassStatisticsDoublePrecision>() {
        @Override
        protected SinglePassStatisticsDoublePrecision createEmptyValue(String key) {
            return new SinglePassStatisticsDoublePrecision(key);
        }
    };

    public TimeCombiner(long time, long intervalLength) {
        this.time = time;
        this.intervalLength = intervalLength;
    }

    public void send(TimeAggregation aggregation) {
        count += aggregation.getCount();

        // Merge in the frequency counts
        Map<String, IntegerFrequencyCount> otherCounts = aggregation.getFrequencyCounts();
        for (Entry<String, IntegerFrequencyCount> entry : otherCounts.entrySet()) {
            String label = entry.getKey();
            IntegerFrequencyCount counts = entry.getValue();
            frequencyCounts.get(label).count(counts);
        }

        // Merge in the high level stats
        Collection<SinglePassStatisticsDoublePrecision> otherStatistics = aggregation.getStatistics();
        for (SinglePassStatisticsDoublePrecision statistic : otherStatistics) {
            String key = statistic.getName();
            statistics.get(key).merge(statistic);
        }

    }

    public long getTime() {
        return time;
    }

    public void dump() {

        if (count > 0) {
            Out.out("Time : {}    : count={}", Logger.toTimeString(time), count);


            for (Entry<String, IntegerFrequencyCount> frequencyCountEntry : frequencyCounts.entrySet()) {
                Out.out("    {}", frequencyCountEntry.getKey());
                IntegerFrequencyCount value = frequencyCountEntry.getValue();
                List<MutableIntegerValue> sortedValues = value.getSortedValues();
                int count = 0;
                for (MutableIntegerValue sortedValue : sortedValues) {
                    Out.out("        {} = {}", sortedValue.key, sortedValue.value);
                    count++;
                    if (count > 5) {
                        break;
                    }
                }
            }

            for (SinglePassStatisticsDoublePrecision stats : statistics.values()) {
                Out.out("    {}", stats.getName());
                Out.out("           {}", stats);
            }
        }
    }
}
