package com.logginghub.analytics.charting;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.LineFormatController;
import com.logginghub.analytics.OHLCValue;
import com.logginghub.analytics.demo.DataFunction;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.Pair;
import com.logginghub.utils.StandardDateFormat;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bar that takes care of itself. You just give it [series, value] values and it does the rest.
 * 
 * @author James
 * 
 */
public class BarChartPanel extends JPanel implements ChartPanelInterface {

    private DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private int imageFileHeight = 768;

    private String imageFilename;
    private int imageFileWidth = 1024;
    private LineFormatController lineFormatController;

    private long mostRecentTimeValue;

    private CategoryPlot plot;
    private Map<String, XYSeries> seriesForSource = new HashMap<String, XYSeries>();
    private CategoryAxis xAxis;
    // private long timePeriod = 2 * 60 * 1000;
    // private int datapoints = 1000;
    // private long chunkPeriod = 1000;
    private NumberAxis yAxis;

    private double yAxisLock;

    private float yMinimumFilter = Float.NaN;

    public static String newline = System.getProperty("line.separator");

    // public void setSplineRenderer(boolean splineRenderer) {
    // if (splineRenderer) {
    // xyplot.setRenderer(new XYSplineRenderer());
    // }
    // else {
    // xyplot.setRenderer(new XYLineAndShapeRenderer());
    // }
    // }

    private static final long serialVersionUID = 1L;

    // public void updateValue(String series, long currentTimeMillis, double
    // value) {
    // XYSeries xySeries = getSeriesForSource(series);
    // synchronized (xySeries) {
    // xySeries.update(currentTimeMillis, value);
    // }
    // }

    // public void addValue(String series, long time, double value) {
    // XYSeries xySeries = getSeriesForSource(series);
    // // long chunk = chunk(time);
    // synchronized (xySeries) {
    // xySeries.add(time, value);
    // }
    //
    // mostRecentTimeValue = Math.max(time, mostRecentTimeValue);
    // }

    // private long chunk(long time) {
    // return time - (time % chunkPeriod);
    // }

    // protected XYSeries getSeriesForSource(String label) {
    // XYSeries xySeries;
    // synchronized (seriesForSource) {
    // xySeries = seriesForSource.get(label);
    // if (xySeries == null) {
    // xySeries = new XYSeries(label);
    // int seriesIndex = categoryDataset.getSeriesCount();
    // categoryDataset.addSeries(xySeries);
    //
    // // xySeries.setMaximumItemCount(datapoints);
    // seriesForSource.put(label, xySeries);
    //
    // if (lineFormatController != null) {
    // Paint paint = lineFormatController.allocateColour(label);
    //
    // XYPlot xyPlot = chart.getXYPlot();
    // XYItemRenderer xyir = xyPlot.getRenderer();
    // xyir.setSeriesPaint(seriesIndex, paint);
    //
    // Stroke stroke = lineFormatController.getStroke(label);
    // xyir.setSeriesStroke(seriesIndex, stroke);
    // }
    // }
    // }
    // return xySeries;
    // }

    // private void addUpperValueExceeded(double value, long
    // startOfCurrentChunk) {
    // // Hour hour = new Hour(2, new Day(22, 2, 2010));
    // // Minute minute = new Minute(15, hour);
    // // long d = minute.getFirstMillisecond();
    // //
    // ValueMarker valuemarker3 = new ValueMarker(startOfCurrentChunk);
    // valuemarker3.setPaint(Color.black);
    // valuemarker3.setLabel("Threshold exceeded (" + value + ")");
    // valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    // valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    // xyplot.addDomainMarker(valuemarker3);
    // }

    public BarChartPanel() {

        xAxis = new CategoryAxis("");

        yAxis = new NumberAxis("Count");
        yAxis.setAutoRangeIncludesZero(true);

        // XYSplineRenderer xysplinerenderer = new XYSplineRenderer();
        BarRenderer renderer = new BarRenderer();
        plot = new CategoryPlot(categoryDataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);

        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, ColourUtils.mildBlue);

        // xyplot.setAxisOffset(new RectangleInsets(4D, 4D, 4D, 4D));

        // XYLineAndShapeRenderer xylineandshaperenderer =
        // (XYLineAndShapeRenderer) xyplot.getRenderer();
        // xylineandshaperenderer.setBaseShapesVisible(false);
        // xylineandshaperenderer.setBaseShapesFilled(false);

        chart = new JFreeChart("Please set the title!", JFreeChart.DEFAULT_TITLE_FONT, plot, true);

        
        /*
         * ValueMarker valuemarker1 = new ValueMarker(175D);
         * valuemarker1.setLabelOffsetType(LengthAdjustmentType.EXPAND);
         * valuemarker1.setPaint(Color.red); valuemarker1.setLabel("Target Price");
         * valuemarker1.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
         * valuemarker1.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
         * xyplot.addRangeMarker(valuemarker1);
         */

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        // ChartUtilities.applyCurrentTheme(chart);
        setLayout(new BorderLayout());
        chartPanel = new ChartPanel(chart);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMinimumDrawWidth(0);

        add(chartPanel, BorderLayout.CENTER);
    }

    @Override public void addValue(String key, String string, double value) {
        this.categoryDataset.addValue(value, key, string);
    }

    public void addSeries(String category, IntegerFrequencyCount frequencyCount) {
        List<MutableIntegerValue> sortedValues = frequencyCount.getSortedValues();
        for (MutableIntegerValue value : sortedValues) {
            this.categoryDataset.addValue(value.value, category, value.key.toString());
        }
    }

    public DefaultCategoryDataset getCategoryDataset() {
        return categoryDataset;
    }

    public void addYMarker(double xValue, String label, Color colour, double topOffset) {
        throw new UnsupportedOperationException();
    }

    public void clearChartData() {
        synchronized (seriesForSource) {
            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                xySeries.clear();
            }
        }
    }

    public void complete() {
        String filename = imageFilename;

        if (filename == null) {
            filename = chart.getTitle().getText() + ".png";
        }

        File file = new File(filename);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, imageFileWidth, imageFileHeight);
            System.out.println("Chart written to " + file.getAbsolutePath());
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save png [%s]", file.getAbsolutePath()), e);
        }
    }

    public void createSeries(String seriesName, AggregatedData series, AggregatedDataKey key) {
        throw new UnsupportedOperationException();
    }

    // public void setDatapoints(int datapoints) {
    // this.datapoints = datapoints;
    // }

    public void createSeries(String seriesName, AggregatedData aggregatedData, DataFunction dataFunction) {
        throw new UnsupportedOperationException();
    }

    public void createSeries(String seriesName, GeneralAggregatedData aggregated, AggregatedDataKey key) {
        throw new UnsupportedOperationException();
    }

    @Override public void createSeries(String category, List<Pair<Comparable, Number>> data) {

        for (Pair<Comparable, Number> value : data) {
            this.categoryDataset.addValue(value.getB(), category, value.getA());
        }

    }

    public void createSeries(String seriesName, TimeSeriesData series) {
        createSeries(seriesName, series, 0);
    }

    public void createSeries(String seriesName, TimeSeriesData series, int valueIndex) {
        throw new UnsupportedOperationException();
    }

    public void enableLongCategoryNames() {
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        domainAxis.setMaximumCategoryLabelLines(2);
    }

    public JFreeChart getChart() {
        return chart;
    }

    public JComponent getComponent() {
        return chartPanel;
    }

    public Dataset getDataset() {
        return categoryDataset;
    }

    public Plot getPlot() {
        return this.plot;
    }

    public void saveChartData() {
        StringBuilder builder = new StringBuilder();
        synchronized (seriesForSource) {
            Set<Long> xValues = new HashSet<Long>();

            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                List<XYDataItem> items = xySeries.getItems();
                for (XYDataItem item : items) {
                    double xValue = item.getXValue();
                    long xTimeValue = (long) xValue;
                    xValues.add(xTimeValue);
                }
            }

            List<Long> xValuesList = new ArrayList<Long>(xValues);
            Collections.sort(xValuesList);
            Set<String> keys = seriesForSource.keySet();

            builder.append("Time,");
            for (String seriesKey : keys) {
                builder.append(seriesKey).append(",");
            }
            builder.append(newline);

            for (Long xValue : xValuesList) {
                Date date = new Date(xValue);
                builder.append(date.toString());
                builder.append(",");
                for (String seriesKeys : keys) {
                    XYSeries xySeries = seriesForSource.get(seriesKeys);
                    Double d = findValue(xySeries, xValue);
                    if (d != null) {
                        builder.append(d);
                    }
                    builder.append(",");
                }

                builder.append(newline);
            }
        }

        String filename = chart.getTitle().getText() + ".csv";
        File file = new File(filename);
        FileUtils.write(builder.toString(), file);
        System.out.println("Saved data to " + file.getAbsolutePath());
    }

    // public void createSeries(String seriesName, TimeSeriesData series, int
    // seriesDataIndex) {
    // XYSeries seriesForSource = new XYSeries(seriesName);
    //
    // for (TimeSeriesDataPoint dataPoint : series) {
    // seriesForSource.add(dataPoint.getTime(),
    // dataPoint.getValues()[seriesDataIndex]);
    // }
    //
    // categoryDataset.addSeries(seriesForSource);
    // }

    public void saveChartImage() {
        complete();
    }

    public void setImageFileHeight(int imageFileHeight) {
        this.imageFileHeight = imageFileHeight;
    }

    // public void createSeries(String seriesName, GeneralAggregatedData series,
    // AggregatedDataKey key) {
    //
    // XYSeries seriesForSource = new XYSeries(seriesName);
    //
    // for (GeneralAggregatedDataPoint aggregatedDataPoint : series) {
    // double startValue = aggregatedDataPoint.getStartValue();
    // double value = aggregatedDataPoint.getValue(key);
    // seriesForSource.add(startValue, value);
    // }
    //
    // categoryDataset.addSeries(seriesForSource);
    // }

    // public void createSeries(String seriesName, AggregatedData series,
    // DataFunction dataFunction) {
    //
    // XYSeries seriesForSource = new XYSeries(seriesName);
    //
    // for (AggregatedDataPoint aggregatedDataPoint : series) {
    // long startTime = aggregatedDataPoint.getStartTime();
    // double value =
    // dataFunction.function(aggregatedDataPoint.getValue(dataFunction.getKey()));
    // seriesForSource.add(startTime, value);
    // }
    //
    // categoryDataset.addSeries(seriesForSource);
    // }

    // public void createSeries(String seriesName, AggregatedData series,
    // AggregatedDataKey key) {
    //
    // XYSeries seriesForSource = new XYSeries(seriesName);
    //
    // for (AggregatedDataPoint aggregatedDataPoint : series) {
    // long startTime = aggregatedDataPoint.getStartTime();
    // double value = aggregatedDataPoint.getValue(key);
    // seriesForSource.add(startTime, value);
    // }
    //
    // categoryDataset.addSeries(seriesForSource);
    // }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    // public void setPlainXY() {
    // xyplot.setDomainAxis(new NumberAxis());
    // }

    // public void addYMarker(double xValue, String label, Color colour, double
    // topOffset) {
    //
    // ValueMarker valuemarker3 = new ValueMarker(xValue);
    // valuemarker3.setPaint(colour);
    // valuemarker3.setLabel(label);
    // valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
    // valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
    // valuemarker3.setLabelOffset(new RectangleInsets(topOffset, 0, 0, 0));
    //
    // double upperBound = xyplot.getDomainAxis().getUpperBound();
    // double newBound = Math.max(upperBound, xValue + 10);
    // xyplot.getDomainAxis().setUpperBound(newBound);
    //
    // xyplot.addDomainMarker(valuemarker3);
    // }

    public void setImageFileWidth(int imageFileWidth) {
        this.imageFileWidth = imageFileWidth;
    }

    public void setLegendVisible(boolean visible) {
        chart.getLegend().setVisible(visible);
    }

    public void setLineFormatController(LineFormatController lineFormatController) {
        this.lineFormatController = lineFormatController;
    }

    public void setNotify(boolean notifyOn) {
        chart.setNotify(notifyOn);
    }

    @Override public void setOrientation(PlotOrientation orientation) {
        plot.setOrientation(orientation);
    }

    public void setPlainXY() {
        throw new UnsupportedOperationException();
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void setVerticalXAxisLabels(boolean b) {
        if (b) {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        }
        else {
            xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        }
    }

    public void setXAxisLabel(String label) {
        plot.getDomainAxis().setLabel(label);
    }

    @Override public void setXAxisLogarithmicScale() {}

    public void setYAxisLabel(String yAxisLabel) {
        yAxis.setLabel(yAxisLabel);
    }

    @Override public void setYAxisLogarithmicScale() {
        final LogAxis rangeAxis = new LogAxis("Log(y)");
        rangeAxis.setAutoRange(true);
        plot.setRangeAxis(rangeAxis);
    }

    public void setYMaximum(double value) {
        yAxis.setAutoRange(false);
        yAxis.setRange(0, value);
    }

    public void setYMinimumFilter(float yMinimumFilter) {
        this.yMinimumFilter = yMinimumFilter;
    }

    public void toCSV(File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));

            StandardDateFormat sdf = new StandardDateFormat();

            int columnCount = categoryDataset.getColumnCount();
            int rowCount = categoryDataset.getRowCount();

            for (int row = 0; row < rowCount; row++) {
                for (int column = 0; column < columnCount; column++) {

                    Comparable rowKey = categoryDataset.getRowKey(row);
                    Comparable columnKey = categoryDataset.getColumnKey(column);
                    Number value = categoryDataset.getValue(row, column);

                    writer.write(rowKey.toString());
                    writer.write(",");
                    writer.write(columnKey.toString());
                    writer.write(",");
                    writer.write(value.toString());
                    writer.newLine();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            FileUtils.closeQuietly(writer);
        }
    }

    @Override public void yAxisLock(double value) {
        this.yAxisLock = value;
        yAxis.setAutoRange(false);
        yAxis.setRange(0, value);
    }

    private Double findValue(XYSeries xySeries, long xValue) {
        Double value = null;

        List<XYDataItem> items = xySeries.getItems();
        for (XYDataItem item : items) {
            double itemXValue = item.getXValue();
            long xTimeValue = (long) itemXValue;

            if (xTimeValue == xValue) {
                value = item.getYValue();
                break;
            }
        }

        return value;
    }

    public void updateRange(double value) {
        if (yAxisLock != Double.NaN) {
            if (value >= yAxisLock) {
                yAxis.setAutoRange(true);
            }
            else if (yAxis.getRange().getUpperBound() < yAxisLock) {
                // We've just autoranged back below the threshold, so bring the
                // lock back
                yAxis.setAutoRange(false);
                yAxis.setRange(0, yAxisLock);
            }
        }
    }

    @Override public void addOHLCSeries(String category, List<Pair<Integer, OHLCValue>> values) {}

    @Override public void setShowLegend(boolean showLegend) {
        chart.getLegend().setVisible(showLegend);
    }
    
    @Override public void createStatsSeries(String category, List<Pair<Long, Double>> data) {}
}
