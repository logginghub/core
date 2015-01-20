package com.logginghub.analytics.charting;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.OHLCValue;
import com.logginghub.analytics.demo.DataFunction;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.Pair;

public interface ChartPanelInterface {

    void addSeries(String category, IntegerFrequencyCount frequencyCount);

    void addYMarker(double xValue, String label, Color colour, double topOffset);

    void createSeries(String seriesName, AggregatedData series, AggregatedDataKey key);

    void createSeries(String seriesName, AggregatedData aggregatedData, DataFunction dataFunction);

    void createSeries(String seriesName, GeneralAggregatedData aggregated, AggregatedDataKey key);

    void createSeries(String category, List<Pair<Comparable, Number>> data);
    void createStatsSeries(String category, List<Pair<Long, Double>> data);
    
    void createSeries(String string, TimeSeriesData series);
    

    void createSeries(String seriesName, TimeSeriesData series, int valueIndex);

    void enableLongCategoryNames();

    JFreeChart getChart();

    Component getComponent();

    Dataset getDataset();

    Plot getPlot();

    void setBackground(Color white);

    void setNotify(boolean notifyOn);

    void setOrientation(PlotOrientation orientation);

    void setPlainXY();

    void setTitle(String string);

    void setVerticalXAxisLabels(boolean b);

    void setXAxisLabel(String label);

    void setXAxisLogarithmicScale();

    void setYAxisLabel(String string);

    void setYAxisLogarithmicScale();

    void setYMaximum(double value);

    void toCSV(File file);

    void yAxisLock(double value);

    void addOHLCSeries(String category, List<Pair<Integer, OHLCValue>> values);

    void addValue(String key, String string, double value);

    void setShowLegend(boolean showLegend);

    


}
