package com.logginghub.analytics.charting;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.File;
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

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.LineFormatController;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.AggregatedDataPoint;
import com.logginghub.utils.FileUtils;

public class DifferencesChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Map<String, TimeSeries> seriesForSource = new HashMap<String, TimeSeries>();
    
    private TimeSeriesCollection differenceCollection = new TimeSeriesCollection();
    private TimeSeriesCollection standardCollection = new TimeSeriesCollection();
    
    private JFreeChart chart;

    private String imageFilename;
    private int imageFileWidth = 1024;
    private int imageFileHeight = 768;
    private XYTimeChartPanel chartpanel;

    private XYPlot xyplot;

    private long timePeriod = 2 * 60 * 1000;
    private int datapoints = 1000;
    private long chunkPeriod = 1000;
    private NumberAxis yAxis;
    private long mostRecentTimeValue;

    public DifferencesChartPanel() {

        DateAxis numberaxis = new DateAxis("Time");

        yAxis = new NumberAxis("Count");
        yAxis.setAutoRangeIncludesZero(true);

        XYLineAndShapeRenderer standardRenderer = new XYLineAndShapeRenderer();
        standardRenderer.setBaseShapesVisible(false);
        standardRenderer.setBaseShapesFilled(false);
        standardRenderer.setSeriesStroke(0, new BasicStroke(2, 1,1));
        
        XYDifferenceRenderer differenceRenderer = new XYDifferenceRenderer();
        differenceRenderer.setBaseSeriesVisible(true);
        differenceRenderer.setPositivePaint(new Color(0,0,200,50));
        differenceRenderer.setNegativePaint(new Color(0,0,200,50));
        differenceRenderer.setPaint(new Color(0,0,200,50));
                
        
        xyplot = new XYPlot(differenceCollection, numberaxis, yAxis, differenceRenderer);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.lightGray);
        xyplot.setForegroundAlpha(1F);
        
        xyplot.setDataset(0, differenceCollection);
        xyplot.setDataset(1, standardCollection);
        
        xyplot.setRenderer(0, differenceRenderer);
        xyplot.setRenderer(1, standardRenderer);
        

        chart = new JFreeChart("Running threads", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        setLayout(new BorderLayout());
        ChartPanel moo = new ChartPanel(chart);
        moo.setMaximumDrawHeight(Integer.MAX_VALUE);
        moo.setMaximumDrawWidth(Integer.MAX_VALUE);
        moo.setMinimumDrawHeight(0);
        moo.setMinimumDrawWidth(0);

        add(moo, BorderLayout.CENTER);
    }

    public void setYAxisLabel(String yAxisLabel) {
        yAxis.setLabel(yAxisLabel);
    }

 
    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void updateValue(String series, long currentTimeMillis, double value) {
        TimeSeries xySeries = getSeriesForSource(series);
        synchronized (xySeries) {
            long chunk = chunk(currentTimeMillis);
            Millisecond ms = new Millisecond(new Date(chunk));
            xySeries.update(ms, value);
        }
    }

    public void addValue(String series, long time, double value) {
        TimeSeries xySeries = getSeriesForSource(series);
        long chunk = chunk(time);
        synchronized (xySeries) {
            Millisecond ms = new Millisecond(new Date(chunk));
            xySeries.add(ms, value);
        }

        mostRecentTimeValue = Math.max(time, mostRecentTimeValue);
    }

    private long chunk(long time) {
        return time - (time % chunkPeriod);
    }

    protected TimeSeries getSeriesForSource(String label) {
        TimeSeries xySeries;
        synchronized (seriesForSource) {
            xySeries = seriesForSource.get(label);
            if (xySeries == null) {
                xySeries = new TimeSeries(label);
                int seriesIndex = standardCollection.getSeriesCount();
                standardCollection.addSeries(xySeries);

                xySeries.setMaximumItemCount(datapoints);
                seriesForSource.put(label, xySeries);

                if (lineFormatController != null) {
                    Paint paint = lineFormatController.allocateColour(label);

                    XYPlot xyPlot = chart.getXYPlot();
                    XYItemRenderer xyir = xyPlot.getRenderer();
                    xyir.setSeriesPaint(seriesIndex, paint);

                    Stroke stroke = lineFormatController.getStroke(label);
                    xyir.setSeriesStroke(seriesIndex, stroke);
                }
            }
        }
        return xySeries;
    }
    
    protected TimeSeries getSeriesForSourceDifference(String label) {
        TimeSeries xySeries;
        synchronized (seriesForSource) {
            xySeries = seriesForSource.get(label);
            if (xySeries == null) {
                xySeries = new TimeSeries(label);
                int seriesIndex = differenceCollection.getSeriesCount();
                differenceCollection.addSeries(xySeries);

                xySeries.setMaximumItemCount(datapoints);
                seriesForSource.put(label, xySeries);

                if (lineFormatController != null) {
                    Paint paint = lineFormatController.allocateColour(label);

                    XYPlot xyPlot = chart.getXYPlot();
                    XYItemRenderer xyir = xyPlot.getRenderer();
                    xyir.setSeriesPaint(seriesIndex, paint);

                    Stroke stroke = lineFormatController.getStroke(label);
                    xyir.setSeriesStroke(seriesIndex, stroke);
                }
            }
        }
        return xySeries;
    }

    private void addUpperValueExceeded(double value, long startOfCurrentChunk) {
        // Hour hour = new Hour(2, new Day(22, 2, 2010));
        // Minute minute = new Minute(15, hour);
        // long d = minute.getFirstMillisecond();
        //
        ValueMarker valuemarker3 = new ValueMarker(startOfCurrentChunk);
        valuemarker3.setPaint(Color.black);
        valuemarker3.setLabel("Threshold exceeded (" + value + ")");
        valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        xyplot.addDomainMarker(valuemarker3);
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

    public void setDatapoints(int datapoints) {
        this.datapoints = datapoints;
    }

    public void clearChartData() {
        synchronized (seriesForSource) {
            Collection<TimeSeries> values = seriesForSource.values();
            for (TimeSeries xySeries : values) {
                xySeries.clear();
            }
        }
    }

    public static String newline = System.getProperty("line.separator");
    private float yMinimumFilter = Float.NaN;
    private LineFormatController lineFormatController;

    public void saveChartData() {
        StringBuilder builder = new StringBuilder();
        synchronized (seriesForSource) {
            Set<Long> xValues = new HashSet<Long>();

            Collection<TimeSeries> values = seriesForSource.values();
            for (TimeSeries xySeries : values) {
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
                    TimeSeries xySeries = seriesForSource.get(seriesKeys);
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

    private Double findValue(TimeSeries xySeries, long xValue) {
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

    @SuppressWarnings("unchecked") public void removeOldDataPoints() {
        List<XYSeries> series = standardCollection.getSeries();
        for (XYSeries xySeries : series) {
            while (xySeries.getItemCount() > 0) {
                XYDataItem xyDataItem = xySeries.getDataItem(0);
                long itemTime = xyDataItem.getX().longValue();
                if (mostRecentTimeValue - timePeriod > itemTime) {
                    xySeries.remove(0);
                }
                else {
                    // Fast exit, the items will be in time order
                    break;
                }
            }
        }
    }

    public void addDifferencesSeries(AggregatedData data, AggregatedDataKey low, AggregatedDataKey high) {
        TimeSeries lowSeries = getSeriesForSourceDifference(low.name());        
        TimeSeries highSeries = getSeriesForSourceDifference(high.name());
        for (AggregatedDataPoint dataPoint : data) {
            lowSeries.add(new Millisecond(new Date(dataPoint.getStartTime())), dataPoint.getValue(low));
            highSeries.add(new Millisecond(new Date(dataPoint.getStartTime())), dataPoint.getValue(high));
        }
    }

}
