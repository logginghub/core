package com.logginghub.analytics;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;

import com.logginghub.analytics.charting.AreaChartPanel;
import com.logginghub.analytics.charting.BarChartPanel;
import com.logginghub.analytics.charting.ChartPanelInterface;
import com.logginghub.analytics.charting.OHLCTimeChartPanel;
import com.logginghub.analytics.charting.StackedAreaChartPanel;
import com.logginghub.analytics.charting.StackedBarChartPanel;
import com.logginghub.analytics.charting.XYTimeChartPanel;
import com.logginghub.analytics.demo.DataFunction;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.analytics.model.MultiSeriesAggreatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataContainer;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.Pair;
import com.logginghub.utils.swing.TestFrame;

public class ChartBuilder {

    private ChartPanelInterface panel = new XYTimeChartPanel();
    private Log log = Log.create(this);

    public enum Type {
        OHLC,
        XY,
        Bar,
        StackedBar,
        StackedArea,
        Area
    }

    public static ChartBuilder start(Type chartType) {
        ChartBuilder builder;

        switch (chartType) {
            case Bar:
                builder = new ChartBuilder(new BarChartPanel());
                break;
            case StackedBar:
                builder = new ChartBuilder(new StackedBarChartPanel());
                break;
            case XY:
                builder = new ChartBuilder(new XYTimeChartPanel());
                break;
            case OHLC:
                builder = new ChartBuilder(new OHLCTimeChartPanel());
                break;

            case StackedArea:
                builder = new ChartBuilder(new StackedAreaChartPanel());
                break;
            case Area:
                builder = new ChartBuilder(new AreaChartPanel());
                break;

            default:
                throw new NotImplementedException(chartType.name());
        }
        return builder;

    }

    public static ChartBuilder startOHLC() {
        return new ChartBuilder(new OHLCTimeChartPanel());
    }

    public static ChartBuilder startXY() {
        return new ChartBuilder(new XYTimeChartPanel());
    }

    public static ChartBuilder startStackedBar() {
        return new ChartBuilder(new StackedBarChartPanel());
    }

    public static ChartBuilder startArea() {
        return new ChartBuilder(new AreaChartPanel());
    }

    public static ChartBuilder startStackedArea() {
        return new ChartBuilder(new StackedAreaChartPanel());
    }

    public static ChartBuilder startBar() {
        return new ChartBuilder(new BarChartPanel());
    }

    public ChartBuilder(ChartPanelInterface panel) {
        this.panel = panel;
        panel.setBackground(Color.white);
        panel.getChart().setBackgroundPaint(Color.white);
    }

    public ChartBuilder addSeries(AggregatedData aggregated, AggregatedDataKey key) {
        addSeries(aggregated.getSeriesName(), aggregated, key);
        return this;
    }

    public void addSeries(String string, AggregatedData series, AggregatedDataKey key) {

        log.info("Adding series [%s] to chart with data from [%2$tD %2$tT] to [%3$tD %3$tT] : elements [%4$d]",
                 string,
                 series.getStartTime(),
                 series.getEndTime(),
                 series.size());

        String seriesName = key.name() + " of " + string;
        panel.createSeries(seriesName, series, key);
    }

    public ChartPanelInterface toChart() {
        return panel;
    }

    public ChartBuilder setYMaximum(double value) {
        panel.setYMaximum(value);
        return this;
    }

    public ChartBuilder setTitleFromLegends(MultiSeriesAggreatedData multiSeriesAggreatedData) {
        String valueLegend = multiSeriesAggreatedData.getValueLegend();
        String keysLegend = multiSeriesAggreatedData.getKeysLegend();

        panel.setTitle(valueLegend + " by " + keysLegend);
        return this;
    }

    public ChartBuilder setTitle(String title) {
        panel.setTitle(title);
        return this;
    }

    public ChartBuilder setOrientation(PlotOrientation orientation) {
        panel.setOrientation(orientation);
        return this;
    }

    public ChartBuilder addSeries(MultiSeriesAggreatedData multiSeriesAggreatedData, AggregatedDataKey key) {

        List<AggregatedData> orderedData = multiSeriesAggreatedData.getOrderedData();
        for (AggregatedData aggregatedData : orderedData) {
            String seriesName = aggregatedData.getSeriesName() + " " + key.name();
            panel.createSeries(seriesName, aggregatedData, key);
        }

        return this;
    }

    public ChartBuilder addSeries(MultiSeriesAggreatedData multiSeriesAggreatedData, DataFunction dataFunction) {
        List<AggregatedData> orderedData = multiSeriesAggreatedData.getOrderedData();
        for (AggregatedData aggregatedData : orderedData) {
            String seriesName = aggregatedData.getSeriesName() + " " + dataFunction.getName();
            panel.createSeries(seriesName, aggregatedData, dataFunction);
        }

        return this;
    }

    public void showInFrame() {
        TestFrame.show(panel.getComponent());
    }

    public ChartBuilder addSeries(String seriesName, GeneralAggregatedData aggregated, AggregatedDataKey key) {
        panel.createSeries(seriesName, aggregated, key);
        return this;
    }

    public void toTestFrame() {
        showInFrame();
    }

    public ChartBuilder setPlainXY() {
        panel.setPlainXY();
        return this;

    }

    public ChartBuilder toCSV(File file) {
        panel.toCSV(file);
        return this;
    }

    public ChartBuilder toPng(File file, int width, int height) {
        try {
            ChartUtilities.saveChartAsPNG(file, panel.getChart(), width, height);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ChartBuilder toPng(File file) {
        toPng(file, 1000, 600);
        return this;
    }

    public ChartBuilder setXAxisLabel(String label) {
        panel.setXAxisLabel(label);
        return this;
    }

    public ChartBuilder setYAxisLabel(String string) {
        panel.setYAxisLabel(string);
        return this;
    }

    public ChartBuilder addSeries(TimeSeriesDataContainer series, int valueIndex) {
        Set<String> seriesNames = series.getSeriesNames();
        for (String string : seriesNames) {
            panel.createSeries(string, series.getSeries(string));
        }
        return this;
    }

    public ChartBuilder addSeries(String seriesName, TimeSeriesData series, int valueIndex) {
        panel.createSeries(seriesName, series, valueIndex);
        return this;
    }

    public ChartBuilder addYMarker(double xValue, String label, Color colour, double topOffset) {
        if (Double.isNaN(xValue) || Double.isInfinite(xValue)) {
            // Dont think so
        }
        else {
            panel.addYMarker(xValue, label, colour, topOffset);
        }
        return this;

    }

    public ChartBuilder addFrequencyCountSeries(String category, IntegerFrequencyCount frequencyCount) {
        panel.addSeries(category, frequencyCount);
        return this;
    }

    public ChartBuilder enableLongCategoryNames() {
        panel.enableLongCategoryNames();
        return this;

    }

    public ChartBuilder setVerticalXAxisLabels(boolean b) {
        panel.setVerticalXAxisLabels(b);
        return this;
    }

    public ChartBuilder setXAxisLogarithmicScale() {
        panel.setXAxisLogarithmicScale();
        return this;
    }

    public ChartBuilder setYAxisLogarithmicScale() {
        panel.setYAxisLogarithmicScale();
        return this;
    }
       
    public ChartBuilder addSeries(String category, List<Pair<Comparable, Number>> data) {
        panel.createSeries(category, data);
        return this;
    }
       
    public ChartBuilder addStatsSeries(String category, List<Pair<Long, Double>> data) {
        panel.createStatsSeries(category, data);
        return this;
    }
    
    public ChartBuilder yAxisLock(double value) {
        panel.yAxisLock(value);
        return this;
    }

    public ChartBuilder addOHLCSeries(String category, List<Pair<Integer, OHLCValue>> values) {
        panel.addOHLCSeries(category, values);
        return this;
    }

    public ChartBuilder addValue(String key, String string, double value) {
        panel.addValue(key, string, value);
        return this;
    }

    public ChartBuilder setShowLegend(boolean showLegend) {
        panel.setShowLegend(showLegend);
        return this;
    }


}
