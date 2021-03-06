package com.logginghub.analytics.charting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.UnitType;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.LineFormatController;
import com.logginghub.analytics.OHLCValue;
import com.logginghub.analytics.demo.DataFunction;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.GeneralAggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.IntegerFrequencyCount;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.Pair;
import com.logginghub.utils.StandardDateFormat;

/**
 * @author James
 * 
 */
public class AreaChartPanel extends JPanel implements ChartPanelInterface {

    private static final long serialVersionUID = 1L;
    // private Map<String, XYSeries> seriesForSource = new HashMap<String, XYSeries>();
    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    private JFreeChart chart;

    private String imageFilename;
    private int imageFileWidth = 1024;
    private int imageFileHeight = 768;
    private ChartPanel chartpanel;

    // private XYPlot xyplot;

    // private long timePeriod = 2 * 60 * 1000;
    // private int datapoints = 1000;
    // private long chunkPeriod = 1000;
    // private NumberAxis yAxis;
    // private long mostRecentTimeValue;
    // private DateAxis xAxis;

    public AreaChartPanel() {

        chart = ChartFactory.createAreaChart("Title", "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.white);
        TextTitle texttitle = new TextTitle("An area chart demonstration.  We use this subtitle as an example of what happens when you get a really long title or subtitle.");
        texttitle.setPosition(RectangleEdge.TOP);
        texttitle.setPadding(new RectangleInsets(UnitType.RELATIVE, 0.050000000000000003D, 0.050000000000000003D, 0.050000000000000003D, 0.050000000000000003D));
        texttitle.setVerticalAlignment(VerticalAlignment.BOTTOM);
        chart.addSubtitle(texttitle);
        CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
        categoryplot.setForegroundAlpha(0.5F);
        categoryplot.setDomainGridlinesVisible(true);
        CategoryAxis categoryaxis = categoryplot.getDomainAxis();
        categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        categoryaxis.setLowerMargin(0.0D);
        categoryaxis.setUpperMargin(0.0D);
        categoryaxis.addCategoryLabelToolTip("Type 1", "The first type.");
        categoryaxis.addCategoryLabelToolTip("Type 2", "The second type.");
        categoryaxis.addCategoryLabelToolTip("Type 3", "The third type.");
        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        numberaxis.setLabelAngle(0.0D);
        ChartUtilities.applyCurrentTheme(chart);

        // xAxis = new DateAxis("Time");
        //
        // yAxis = new NumberAxis("Count");
        // yAxis.setAutoRangeIncludesZero(true);
        //
        // XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        // xyplot = new XYPlot(xyseriescollection, xAxis, yAxis, renderer);
        // xyplot.setBackgroundPaint(Color.white);
        // xyplot.setDomainGridlinePaint(Color.lightGray);
        // xyplot.setRangeGridlinePaint(Color.lightGray);
        //
        //
        // XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer)
        // xyplot.getRenderer();
        // xylineandshaperenderer.setBaseShapesVisible(false);
        // xylineandshaperenderer.setBaseShapesFilled(false);
        //
        // chart = new JFreeChart("Please set the title!", JFreeChart.DEFAULT_TITLE_FONT, xyplot,
        // true);
        //
        // LegendTitle legend = chart.getLegend();
        // legend.setPosition(RectangleEdge.RIGHT);

        // ChartUtilities.applyCurrentTheme(chart);
        setLayout(new BorderLayout());
        chartpanel = new ChartPanel(chart);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMinimumDrawHeight(0);
        chartpanel.setMinimumDrawWidth(0);

        add(chartpanel, BorderLayout.CENTER);
    }

    public void setXAxisLabel(String label) {
        // xyplot.getDomainAxis().setLabel(label);
    }

    public void setYAxisLabel(String yAxisLabel) {
        // yAxis.setLabel(yAxisLabel);
    }

    public void setSplineRenderer(boolean splineRenderer) {
        // if (splineRenderer) {
        // XYSplineRenderer xySplineRenderer = new XYSplineRenderer();
        // xySplineRenderer.setBaseShapesVisible(false);
        // xyplot.setRenderer(xySplineRenderer);
        // }
        // else {
        // XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
        // xyplot.setRenderer(xyLineAndShapeRenderer);
        // }
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void updateValue(String series, long currentTimeMillis, double value) {
        XYSeries xySeries = getSeriesForSource(series);
        synchronized (xySeries) {
            xySeries.update(currentTimeMillis, value);
        }
    }

    public void addValue(String series, long time, double value) {
        // XYSeries xySeries = getSeriesForSource(series);
        // // long chunk = chunk(time);
        // synchronized (xySeries) {
        // xySeries.add(time, value);
        // }
        //
        // mostRecentTimeValue = Math.max(time, mostRecentTimeValue);
    }

    // private long chunk(long time) {
    // return time - (time % chunkPeriod);
    // }

    protected XYSeries getSeriesForSource(String label) {
        // XYSeries xySeries;
        // synchronized (seriesForSource) {
        // xySeries = seriesForSource.get(label);
        // if (xySeries == null) {
        // xySeries = new XYSeries(label);
        // int seriesIndex = xyseriescollection.getSeriesCount();
        // xyseriescollection.addSeries(xySeries);
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
        return null;
    }

    private void addUpperValueExceeded(double value, long startOfCurrentChunk) {
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
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public void setImageFileHeight(int imageFileHeight) {
        this.imageFileHeight = imageFileHeight;
    }

    public void setImageFileWidth(int imageFileWidth) {
        this.imageFileWidth = imageFileWidth;
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

    public JComponent getComponent() {
        return chartpanel;
    }

    public JFreeChart getChart() {
        return chart;
    }

    // public void setDatapoints(int datapoints) {
    // this.datapoints = datapoints;
    // }

    public void clearChartData() {
        // synchronized (seriesForSource) {
        // Collection<XYSeries> values = seriesForSource.values();
        // for (XYSeries xySeries : values) {
        // xySeries.clear();
        // }
        // }
    }

    public static String newline = System.getProperty("line.separator");
    private float yMinimumFilter = Float.NaN;
    private LineFormatController lineFormatController;

    public void saveChartData() {
        // StringBuilder builder = new StringBuilder();
        // synchronized (seriesForSource) {
        // Set<Long> xValues = new HashSet<Long>();
        //
        // Collection<XYSeries> values = seriesForSource.values();
        // for (XYSeries xySeries : values) {
        // List<XYDataItem> items = xySeries.getItems();
        // for (XYDataItem item : items) {
        // double xValue = item.getXValue();
        // long xTimeValue = (long) xValue;
        // xValues.add(xTimeValue);
        // }
        // }
        //
        // List<Long> xValuesList = new ArrayList<Long>(xValues);
        // Collections.sort(xValuesList);
        // Set<String> keys = seriesForSource.keySet();
        //
        // builder.append("Time,");
        // for (String seriesKey : keys) {
        // builder.append(seriesKey).append(",");
        // }
        // builder.append(newline);
        //
        // for (Long xValue : xValuesList) {
        // Date date = new Date(xValue);
        // builder.append(date.toString());
        // builder.append(",");
        // for (String seriesKeys : keys) {
        // XYSeries xySeries = seriesForSource.get(seriesKeys);
        // Double d = findValue(xySeries, xValue);
        // if (d != null) {
        // builder.append(d);
        // }
        // builder.append(",");
        // }
        //
        // builder.append(newline);
        // }
        // }
        //
        // String filename = chart.getTitle().getText() + ".csv";
        // File file = new File(filename);
        // FileUtils.write(builder.toString(), file);
        // System.out.println("Saved data to " + file.getAbsolutePath());
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

    public void saveChartImage() {
        complete();
    }

    public void setYMinimumFilter(float yMinimumFilter) {
        this.yMinimumFilter = yMinimumFilter;
    }

    public void setLegendVisible(boolean visible) {
        chart.getLegend().setVisible(visible);
    }

    public void setLineFormatController(LineFormatController lineFormatController) {
        this.lineFormatController = lineFormatController;
    }
    
    @Override public void createSeries(String seriesName, List<Pair<Comparable, Number>> data) {        
        for (Pair<Comparable, Number> dataPoint : data) {
            dataset.addValue(dataPoint.getB(), seriesName, dataPoint.getA());
        }

    }

    public void createSeries(String seriesName, TimeSeriesData series, int seriesDataIndex) {
        // XYSeries seriesForSource = new XYSeries(seriesName);
        //
        // for (TimeSeriesDataPoint dataPoint : series) {
        // seriesForSource.add(dataPoint.getTime(), dataPoint.getValues()[seriesDataIndex]);
        // }
        //
        // xyseriescollection.addSeries(seriesForSource);
    }

    public void createSeries(String seriesName, TimeSeriesData series) {
        createSeries(seriesName, series, 0);
    }

    public void createSeries(String seriesName, GeneralAggregatedData series, AggregatedDataKey key) {

        // XYSeries seriesForSource = new XYSeries(seriesName);
        //
        // for (GeneralAggregatedDataPoint aggregatedDataPoint : series) {
        // double startValue = aggregatedDataPoint.getStartValue();
        // double value = aggregatedDataPoint.getValue(key);
        // seriesForSource.add(startValue, value);
        // }
        //
        // xyseriescollection.addSeries(seriesForSource);
    }

    public void createSeries(String seriesName, AggregatedData series, DataFunction dataFunction) {

        // XYSeries seriesForSource = new XYSeries(seriesName);
        //
        // for (AggregatedDataPoint aggregatedDataPoint : series) {
        // long startTime = aggregatedDataPoint.getStartTime();
        // double value =
        // dataFunction.function(aggregatedDataPoint.getValue(dataFunction.getKey()));
        // seriesForSource.add(startTime, value);
        // }
        //
        // xyseriescollection.addSeries(seriesForSource);
    }

    public void createSeries(String seriesName, AggregatedData series, AggregatedDataKey key) {
        //
        // XYSeries seriesForSource = new XYSeries(seriesName);
        //
        // for (AggregatedDataPoint aggregatedDataPoint : series) {
        // long startTime = aggregatedDataPoint.getStartTime();
        // double value = aggregatedDataPoint.getValue(key);
        // seriesForSource.add(startTime, value);
        // }
        //
        // xyseriescollection.addSeries(seriesForSource);
    }

    public void setYMaximum(double value) {
        // yAxis.setAutoRange(false);
        // yAxis.setRange(0, value);
    }

    public void setYAutoRangeMinimum(double value) {
        // yAxis.setAutoRange(true);
        // yAxis.setAutoRangeMinimumSize(value);
    }

    public void setPlainXY() {
        // xyplot.setDomainAxis(new NumberAxis());
    }

    public void addYMarker(double xValue, String label, Color colour, double topOffset) {

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
    }

    // @SuppressWarnings("unchecked") public void removeOldDataPoints() {
    // List<XYSeries> series = xyseriescollection.getSeries();
    // for (XYSeries xySeries : series) {
    // while (xySeries.getItemCount() > 0) {
    // XYDataItem xyDataItem = xySeries.getDataItem(0);
    // long itemTime = xyDataItem.getX().longValue();
    // if (mostRecentTimeValue - timePeriod > itemTime) {
    // xySeries.remove(0);
    // }
    // else {
    // // Fast exit, the items will be in time order
    // break;
    // }
    // }
    // }
    // }

    public XYPlot getPlot() {
        // return xyplot;
        return chart.getXYPlot();
    }

    public void toCSV(File file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));

            StandardDateFormat sdf = new StandardDateFormat();

            XYPlot plot = getPlot();
            for (int i = 0; i < plot.getDatasetCount(); i++) {
                XYDataset dataset = plot.getDataset();
                int seriesCount = dataset.getSeriesCount();
                for (int j = 0; j < seriesCount; j++) {

                    int seriesSize = dataset.getItemCount(j);
                    for (int k = 0; k < seriesSize; k++) {
                        long x = dataset.getX(j, k).longValue();
                        double y = dataset.getYValue(j, k);
                        writer.write(sdf.format(new Date(x)));
                        writer.write(",");
                        if (Double.isNaN(y)) {
                            // james : not totally sure about this, should it be zero?
                            writer.write("0");
                        }
                        else {
                            writer.write(Double.toString(y));
                        }
                        writer.newLine();
                    }
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

    public void addSeries(String category, IntegerFrequencyCount frequencyCount) {
        throw new UnsupportedOperationException();
    }

    public void enableLongCategoryNames() {
        throw new UnsupportedOperationException();
    }

    public Dataset getDataset() {
        throw new UnsupportedOperationException();
    }

    public void setNotify(boolean notifyOn) {
        chartpanel.repaint();
        chart.setNotify(notifyOn);
    }

    public void removeOldDataPoints(long timePeriod) {
        // @SuppressWarnings("unchecked") List<XYSeries> series = xyseriescollection.getSeries();
        // for (XYSeries xySeries : series) {
        // while (xySeries.getItemCount() > 0) {
        // XYDataItem xyDataItem = xySeries.getDataItem(0);
        // long itemTime = xyDataItem.getX().longValue();
        // if (mostRecentTimeValue - timePeriod > itemTime) {
        // xySeries.remove(0);
        // }
        // else {
        // // Fast exit, the items will be in time order
        // break;
        // }
        // }
        // }
    }

    public void setVerticalXAxisLabels(boolean b) {
        // if (b) {
        // xAxis.setLabelAngle(90);
        // }
        // else {
        // xAxis.setLabelAngle(0);
        // }
    }

    @Override public void setOrientation(PlotOrientation orientation) {
        // xyplot.setOrientation(orientation);
    }

    public void setXAxisLogarithmicScale() {
        // final LogAxis rangeAxis = new LogAxis("Log(x)");
        // xyplot.setDomainAxis(rangeAxis);
    }

    @Override public void setYAxisLogarithmicScale() {
        // final LogAxis rangeAxis = new LogAxis("Log(y)");
        // xyplot.setRangeAxis(rangeAxis);
    }

    @Override public void yAxisLock(double value) {
        throw new NotImplementedException();
    }

    @Override public void addOHLCSeries(String category, List<Pair<Integer, OHLCValue>> values) {
        throw new NotImplementedException();
    }

    @Override public void addValue(String key, String string, double value) {}

    @Override public void setShowLegend(boolean showLegend) {
        chart.getLegend().setVisible(showLegend);
    }

    @Override public void createStatsSeries(String category, List<Pair<Long, Double>> data) {}

}
