package com.logginghub.logging.datafiles.aggregation;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.Out;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision.StatisticsSnapshot;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by james on 17/09/15.
 */
public class TimeAggregation implements Destination<PatternisedLogEvent>, SerialisableObject {

    private static NumberFormat numberFormat = NumberFormat.getInstance();
    public long time;
    private ValueStripper2 stripper;
    private List<String> labels;
    private long count = 0;

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

    public TimeAggregation(long time, ValueStripper2 stripper) {
        this.time = time;
        this.stripper = stripper;

        labels = stripper.getLabels();
    }

    public TimeAggregation() {
    }

    public long getCount() {
        return count;
    }

    public Map<String, IntegerFrequencyCount> getFrequencyCounts() {
        return frequencyCounts;
    }

    public Collection<SinglePassStatisticsDoublePrecision> getStatistics() {
        return statistics.values();
    }

    public long getTime() {
        return time;
    }

    public void dump() {

        if (count > 1) {
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

    @Override
    public void read(SofReader reader) throws SofException {
        int field =0;
        this.time = reader.readLong(field++);
        this.count = reader.readLong(field++);

        frequencyCounts.clear();
        int frequencyCountsLength = reader.readInt(field++);
        for(int  i = 0;i < frequencyCountsLength; i++) {
            String key = reader.readString(field++);
            IntegerFrequencyCount value = (IntegerFrequencyCount)reader.readObject(field++);
            frequencyCounts.put(key, value);
        }

        int statsCount = reader.readInt(field++);
        for(int  i = 0;i < statsCount; i++) {
            String key = reader.readString(field++);
            StatisticsSnapshot snapshot = (StatisticsSnapshot)reader.readObject(field++);
            SinglePassStatisticsDoublePrecision statisticsDoublePrecision = new SinglePassStatisticsDoublePrecision();
            statisticsDoublePrecision.fromSnapshot(snapshot);
            statistics.put(key, statisticsDoublePrecision);
        }
    }

    @Override
    public void write(SofWriter writer) throws SofException {
        int field =0;
        writer.write(field++, time);
        writer.write(field++, count);

        Set<Entry<String, IntegerFrequencyCount>> frequencyEntries = frequencyCounts.entrySet();
        writer.write(field++, frequencyEntries.size());
        for (Entry<String, IntegerFrequencyCount> entry : frequencyEntries) {
            writer.write(field++, entry.getKey());
            writer.write(field++, entry.getValue());
        }

        Set<Entry<String, SinglePassStatisticsDoublePrecision>> statisticsEntries = statistics.entrySet();
        writer.write(field++, statistics.size());
        for (Entry<String, SinglePassStatisticsDoublePrecision> statisticsEntry : statisticsEntries) {
            writer.write(field++, statisticsEntry.getKey());
            writer.write(field++, statisticsEntry.getValue().getSnapshot());
        }

    }

    @Override
    public void send(PatternisedLogEvent patternisedLogEvent) {
        count++;

        String[] variables = patternisedLogEvent.getVariables();
        for (int i = 0; i < variables.length; i++) {

            boolean isNumericField;
            String key;
            // TODO : go back one level in the process and work out why some patterns end up with more values than labels
            //                if (i < labels.size()) {
            key = labels.get(i);
            isNumericField = stripper.isNumericField(i);
            //                } else {
            //                    key = "??";
            //                    isNumericField = false;
            //                }

            String variable = variables[i];
            if (isNumericField) {
                try {
                    Number parse = numberFormat.parse(variable);
                    statistics.get(key).addValue(parse.doubleValue());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else {
                frequencyCounts.get(key).count(variable, 1);
            }
        }
    }
}
