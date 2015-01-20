package com.logginghub.analytics.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.StringUtils;

public class TimeSeriesData implements Iterable<TimeSeriesDataPoint>, Serializable {

    private static final long serialVersionUID = 1L;

    private List<TimeSeriesDataPoint> dataPoints = new ArrayList<TimeSeriesDataPoint>();
    private String[] keysLegend;
    private String[] valuesLegend;
    private String legend;
    private String seriesName = "";

    public TimeSeriesData(String seriesName) {
        this.seriesName = seriesName;
    }

    public TimeSeriesData() {}

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public void add(long time, String key, double value) {
        add(new TimeSeriesDataPoint(time, new String[] { key }, new double[] { value }));
    }

    public void add(long time, String[] keys, double[] values) {
        add(new TimeSeriesDataPoint(time, keys, values));
    }

    public void add(TimeSeriesDataPoint dataPoint) {
        dataPoints.add(dataPoint);
    }

    public void sort() {
        Collections.sort(dataPoints, new TimeSeriesTimeComparator());
    }

    /**
     * Returns a new time series extracted from this one, containing copies of
     * the data points >= from and < to.
     * 
     * @param from
     * @param to
     * @return
     */
    public TimeSeriesData extract(long from, long to) {

        TimeSeriesData extract = new TimeSeriesData(seriesName);

        for (TimeSeriesDataPoint timeSeriesDataPoint : dataPoints) {
            long time = timeSeriesDataPoint.getTime();
            if (time >= from && time < to) {
                extract.add(timeSeriesDataPoint.copy());
            }
        }

        return extract;
    }

    public static TimeSeriesData fromFile(File file) {
        throw new NotImplementedException();
    }

    public void toFile(File file) {
        throw new NotImplementedException();
    }

    private void persist(TimeSeriesDataPoint dataPoint) {
        throw new NotImplementedException();
    }

    public int size() {
        return dataPoints.size();
    }

    public int getSize() {
        return dataPoints.size();
    }

    public TimeSeriesDataPoint get(int i) {
        return dataPoints.get(i);
    }

    @Override public String toString() {
        if (dataPoints.size() < 10) {
            return StringUtils.format("TimeSeriesData seriesName={} size={} dataPoints={}", seriesName, size(), dataPoints);
        }
        else {
            return StringUtils.format("TimeSeriesData seriesName={} size={}", seriesName, size());
        }
    }

    public void setKeysLegend(String[] keysLegend) {
        this.keysLegend = keysLegend;
    }

    public void setValuesLegend(String[] valuesLegend) {
        this.valuesLegend = valuesLegend;
    }

    public String[] getKeysLegend() {
        return keysLegend;
    }

    public String[] getValuesLegend() {
        return valuesLegend;
    }

    public Iterator<TimeSeriesDataPoint> iterator() {
        return dataPoints.iterator();
    }

    public void setLegend(String legend) {
        this.legend = legend;
    }

    public String getLegend() {
        return legend;
    }

    public void dumpToCSV(File file) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(file), ',');
            for (TimeSeriesDataPoint timeSeriesDataPoint : dataPoints) {
                long time = timeSeriesDataPoint.getTime();
                String[] keys = timeSeriesDataPoint.getKeys();
                double[] values = timeSeriesDataPoint.getValues();

                String[] line = new String[1 + keys.length + values.length];
                line[0] = Long.toString(time);
                System.arraycopy(keys, 0, line, 1, keys.length);

                for (int i = 0; i < values.length; i++) {
                    line[1 + keys.length + i] = Double.toString(values[i]);
                }

                writer.writeNext(line);
            }

            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write csv output to '%s'", file.getAbsolutePath()), e);
        }
    }

    public TimeSeriesData transform(TimeSeriesDataTransformer transformer) {
        TimeSeriesData transformed = new TimeSeriesData();
        for (TimeSeriesDataPoint point : dataPoints) {
            transformed.add(transformer.transform(point));
        }
        return transformed;
    }

    public void merge(TimeSeriesData other) {
        List<TimeSeriesDataPoint> dataPoints2 = other.dataPoints;
        for (TimeSeriesDataPoint timeSeriesDataPoint : dataPoints2) {
            add(timeSeriesDataPoint);
        }
    }

}
