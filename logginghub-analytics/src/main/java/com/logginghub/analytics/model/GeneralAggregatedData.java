package com.logginghub.analytics.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.Log;

/**
 * Encapsulation of a general (non-time based) aggregated data series.
 * @author James
 *
 */
public class GeneralAggregatedData implements Iterable<GeneralAggregatedDataPoint> {

    private List<GeneralAggregatedDataPoint> aggregatedDataPoints = new ArrayList<GeneralAggregatedDataPoint>();
    private double start = Double.MAX_VALUE;
    private double end = -Double.MIN_VALUE;
    private Log log = Log.create(this);
    private String seriesName;

    public GeneralAggregatedData(String seriesName) {
        this.seriesName = seriesName;
    }

    
    
    @Override public String toString() {
        return "GeneralAggregatedData [seriesName=" + seriesName + ", aggregatedDataPoints=" + aggregatedDataPoints.size() + ", start=" + start + ", end=" + end + "]";
    }

    public void add(GeneralAggregatedDataPoint currentPoint) {
        start = Math.min(start, currentPoint.getStartValue());
        end = Math.max(end, currentPoint.getEndValue());
        aggregatedDataPoints.add(currentPoint);
    }

    public double getStart() {
        return start;
    }
    
    public double getEnd() {
        return end;
    }

    public void dump() {
        for (GeneralAggregatedDataPoint dataPoint : aggregatedDataPoints) {
            log.info("%10.2f | %5d", dataPoint.getStartValue(), dataPoint.getCount());
        }
    }

    public int size() {
        return aggregatedDataPoints.size();

    }

    public GeneralAggregatedDataPoint get(int i) {
        return aggregatedDataPoints.get(i);

    }

    public Iterator<GeneralAggregatedDataPoint> iterator() {
        return aggregatedDataPoints.iterator();
    }

    public void dump(AggregatedDataKey key) {
        for (GeneralAggregatedDataPoint dataPoint : aggregatedDataPoints) {
            System.out.println(String.format("%10.2f | %5.2f", dataPoint.getStartValue(), dataPoint.getValue(key)));
        }
    }

    public String getSeriesName() {
        return seriesName;
    }

    public double getValue(AggregatedDataKey key) {

        double value;

        switch (key) {
            case Mean: {
                double total = 0;
                int count = 0;
                for (GeneralAggregatedDataPoint dataPoint : aggregatedDataPoints) {
                    total += dataPoint.getTotal();
                    count += dataPoint.getCount();
                }
                value = total / count;
                break;
            }
            case Count: {
                int count = 0;
                for (GeneralAggregatedDataPoint dataPoint : aggregatedDataPoints) {
                    count += dataPoint.getCount();
                }
                value = count;
                break;
            }
            case Sum: {
                double total = 0;
                for (GeneralAggregatedDataPoint dataPoint : aggregatedDataPoints) {
                    total += dataPoint.getTotal();
                }
                value = total;
                break;
            }
            default:
                throw new RuntimeException("Key '" + key.name() + "' isn't supported");
        }

        return value;
    }
}
