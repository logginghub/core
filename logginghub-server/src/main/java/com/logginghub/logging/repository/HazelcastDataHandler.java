package com.logginghub.logging.repository;

import java.util.List;

import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.logging.repository.cache.AggregatedDataKey;
import com.logginghub.logging.repository.cache.TimeSeriesDataKey;

public class HazelcastDataHandler implements DataHandlerInterface {

    private final String timeSeriesCacheName = "timeseriesdata";
    private final String aggregatedDataCacheName = "aggregateddata";

    public void storeSeries(TimeSeriesDataKey key, TimeSeriesData data) {
//        Map<TimeSeriesDataKey, TimeSeriesData> dataMap = Hazelcast.getMap(timeSeriesCacheName);
//        dataMap.put(key, data);
    }

    public List<TimeSeriesDataKey> querySeries(String seriesName, long fromTime, long toTime) {
//        IMap<TimeSeriesDataKey, TimeSeriesData> dataMap = Hazelcast.getMap(timeSeriesCacheName);

//        Set<TimeSeriesDataKey> keys = (Set<TimeSeriesDataKey>) dataMap.keySet(new TimeSeriesQueryPredicate(seriesName, fromTime, toTime));
//
//        List<TimeSeriesDataKey> sorted = new ArrayList<TimeSeriesDataKey>();
//        sorted.addAll(keys);
//
//        Collections.sort(sorted, new Comparator<TimeSeriesDataKey>() {
//            public int compare(TimeSeriesDataKey o1, TimeSeriesDataKey o2) {
//                return ComparisonChain.start()
//                                      .compare(o1.getStartTime(), o2.getStartTime())
//                                      .compare(o1.getEndTime(), o2.getEndTime())
//                                      .compare(o1.getSeriesName(), o2.getSeriesName())
//                                      .result();
//            }
//        });
//
//        return sorted;
        return null;
    }

    public TimeSeriesData retrieveSeries(TimeSeriesDataKey key) {
//        Map<TimeSeriesDataKey, TimeSeriesData> dataMap = Hazelcast.getMap(timeSeriesCacheName);
//        TimeSeriesData timeSeriesData = dataMap.get(key);
//        return timeSeriesData;
        return null;
    }

//    public static class TimeSeriesQueryPredicate extends AbstractPredicate {
//        private static final long serialVersionUID = 1L;
//
//        private long startTime;
//        private long endTime;
//
//        private String seriesName;
//
//        public TimeSeriesQueryPredicate() {}
//
//        public TimeSeriesQueryPredicate(String seriesName, long startTime, long endTime) {
//            this.seriesName = seriesName;
//            this.startTime = startTime;
//            this.endTime = endTime;
//        }
//
//        public boolean apply(MapEntry entry) {
//            TimeSeriesDataKey key = (TimeSeriesDataKey) entry.getKey();
//            boolean rightSeries = key.getSeriesName().equals(seriesName);
//            boolean startTimeWithinRange = key.getStartTime() >= startTime && key.getStartTime() < endTime;
//            boolean endTimeWithinRange = key.getEndTime() >= startTime && key.getEndTime() < endTime;
//
//            return rightSeries && (startTimeWithinRange || endTimeWithinRange);
//        }
//
//        public void writeData(DataOutput out) throws IOException {
//            out.writeUTF(seriesName);
//            out.writeLong(startTime);
//            out.writeLong(endTime);
//        }
//
//        public void readData(DataInput in) throws IOException {
//                seriesName = in.readUTF();
//                startTime = in.readLong();
//                endTime = in.readLong();
//        }
//
//        @Override public String toString() {
//            return Objects.toStringHelper(this).add("series", seriesName).add("start", startTime).add("end", endTime).toString();
//        }
//    }

    public void storeAggregation(AggregatedDataKey key, AggregatedData data) {
//        Map<AggregatedDataKey, AggregatedData> dataMap = Hazelcast.getMap(aggregatedDataCacheName);
//        dataMap.put(key, data);
    }

    public AggregatedData retrieveAggregation(AggregatedDataKey key) {
//        Map<AggregatedDataKey, AggregatedData> dataMap = Hazelcast.getMap(aggregatedDataCacheName);
//        AggregatedData timeSeriesData = dataMap.get(key);
//        return timeSeriesData;
        return null;

    }

    public List<AggregatedDataKey> queryAggregations(String seriesName, long fromTime, long toTime, long aggregationInterval) {
//        IMap<AggregatedDataKey, TimeSeriesData> dataMap = Hazelcast.getMap(aggregatedDataCacheName);
//
//        Set<AggregatedDataKey> keys = (Set<AggregatedDataKey>) dataMap.keySet(new AggregateQueryPredicate(seriesName,
//                                                                                                          fromTime,
//                                                                                                          toTime,
//                                                                                                          aggregationInterval));
//
//        List<AggregatedDataKey> sorted = new ArrayList<AggregatedDataKey>();
//        sorted.addAll(keys);
//
//        Collections.sort(sorted, new Comparator<AggregatedDataKey>() {
//            public int compare(AggregatedDataKey o1, AggregatedDataKey o2) {
//                return ComparisonChain.start()
//                                      .compare(o1.getStartTime(), o2.getStartTime())
//                                      .compare(o1.getEndTime(), o2.getEndTime())
//                                      .compare(o1.getSeriesName(), o2.getSeriesName())
//                                      .compare(o1.getAggregationInteval(), o2.getAggregationInteval())
//                                      .result();
//            }
//        });
//
//        return sorted;
        return null;
    }

//    public static class AggregateQueryPredicate extends AbstractPredicate {
//        private static final long serialVersionUID = 1L;
//
//        private long startTime;
//        private long endTime;
//        private long aggregationInterval;
//        private String seriesName;
//
//        public AggregateQueryPredicate() {}
//
//        public AggregateQueryPredicate(String seriesName, long startTime, long endTime, long aggregationInterval) {
//            this.seriesName = seriesName;
//            this.startTime = startTime;
//            this.endTime = endTime;
//            this.aggregationInterval = aggregationInterval;
//        }
//
//        public boolean apply(MapEntry entry) {
//            AggregatedDataKey key = (AggregatedDataKey) entry.getKey();
//            boolean rightSeries = key.getSeriesName().equals(seriesName);
//            boolean rightInterval = key.getAggregationInteval() == aggregationInterval;
//            boolean startTimeWithinRange = key.getStartTime() >= startTime && key.getStartTime() < endTime;
//            boolean endTimeWithinRange = key.getEndTime() >= startTime && key.getEndTime() < endTime;
//
//            return rightSeries && rightInterval && (startTimeWithinRange || endTimeWithinRange);
//        }
//
//        public void writeData(DataOutput out) throws IOException {
//            out.writeUTF(seriesName);
//            out.writeLong(startTime);
//            out.writeLong(endTime);
//            out.writeLong(aggregationInterval);
//        }
//
//        public void readData(DataInput in) throws IOException {
//            seriesName = in.readUTF();
//            startTime = in.readLong();
//            endTime = in.readLong();
//            aggregationInterval = in.readLong();
//        }
//
//        @Override public String toString() {
//            return Objects.toStringHelper(this)
//                          .add("series", seriesName)
//                          .add("start", startTime)
//                          .add("end", endTime)
//                          .add("aggregationInterval", aggregationInterval)
//                          .toString();
//        }
//    }
}
