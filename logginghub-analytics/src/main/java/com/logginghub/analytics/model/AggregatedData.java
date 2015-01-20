package com.logginghub.analytics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.Log;
import com.logginghub.utils.CompareUtils;

/**
 * Encapsulation of a time based aggregated data series.
 * 
 * @author James
 * 
 */
public class AggregatedData implements Iterable<AggregatedDataPoint>, Serializable {

    private static final long serialVersionUID = 1L;

    private List<AggregatedDataPoint> aggregatedDataPoints = new ArrayList<AggregatedDataPoint>();
    private long start = Long.MAX_VALUE;
    private long end = Long.MIN_VALUE;
    private transient Log log = Log.create(this);
    private String legend;
    private String seriesName;

    public AggregatedData() {}

    public AggregatedData(String seriesName, String legend) {
        this.seriesName = seriesName;
        this.legend = legend;
    }
    
    @Override public String toString() {
        return "AggregatedData [seriesName=" + seriesName + ", legend=" + legend + ", aggregatedDataPoints=" + aggregatedDataPoints.size() + ", start=" + start + ", end=" + end + "]";
    }

    public void add(AggregatedDataPoint currentPoint) {
        start = Math.min(start, currentPoint.getStartTime());
        end = Math.max(end, currentPoint.getEndTime());
        aggregatedDataPoints.add(currentPoint);
    }

    public long getStartTime() {
        return start;
    }

    public long getEndTime() {
        return end;
    }

    public void dump() {
        for (AggregatedDataPoint dataPoint : aggregatedDataPoints) {
            log.info("%10s | %5d", new Date(dataPoint.getStartTime()), dataPoint.getCount());
        }
    }

    public int size() {
        return aggregatedDataPoints.size();

    }

    public AggregatedDataPoint get(int i) {
        return aggregatedDataPoints.get(i);

    }

    public Iterator<AggregatedDataPoint> iterator() {
        return aggregatedDataPoints.iterator();
    }

    public void dump(AggregatedDataKey key) {
        for (AggregatedDataPoint dataPoint : aggregatedDataPoints) {
            System.out.println(String.format("%10s | %5.2f", new Date(dataPoint.getStartTime()), dataPoint.getValue(key)));
        }
    }

    public String getLegend() {
        return legend;
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
                for (AggregatedDataPoint dataPoint : aggregatedDataPoints) {
                    total += dataPoint.getTotal();
                    count += dataPoint.getCount();
                }
                value = total / count;
                break;
            }
            case Count: {
                int count = 0;
                for (AggregatedDataPoint dataPoint : aggregatedDataPoints) {
                    count += dataPoint.getCount();
                }
                value = count;
                break;
            }
            case Sum: {
                double total = 0;
                for (AggregatedDataPoint dataPoint : aggregatedDataPoints) {
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

    /**
     * Merge this new data series into our own
     * 
     * @param aggregatedData
     */
    public void merge(AggregatedData other) {

        // Reset the min/max
        start = Long.MAX_VALUE;
        end = Long.MIN_VALUE;

        // Chuck all of the points into one big list
        List<AggregatedDataPoint> allPoints = new ArrayList<AggregatedDataPoint>();

        allPoints.addAll(this.aggregatedDataPoints);
        allPoints.addAll(other.aggregatedDataPoints);

        // Sort it by time
        Collections.sort(allPoints, new Comparator<AggregatedDataPoint>() {
            public int compare(AggregatedDataPoint o1, AggregatedDataPoint o2) {
                return CompareUtils.compare(o1.getStartTime(), o2.getStartTime());
            }
        });

        // Iterate through merging similar times together
        Iterator<AggregatedDataPoint> iterator = allPoints.iterator();
        AggregatedDataPoint previousPoint = null;
        while (iterator.hasNext()) {
            AggregatedDataPoint next = iterator.next();

            start = Math.min(start, next.getStartTime());
            end = Math.max(end, next.getEndTime());

            if (previousPoint != null) {
                if (previousPoint.getStartTime() == next.getStartTime()) {
                    previousPoint.merge(next);
                    iterator.remove();
                }
            }

            previousPoint = next;
        }

        // That us then
        aggregatedDataPoints = allPoints;
    }

}
