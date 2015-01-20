package com.logginghub.logging.frontend.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import com.logginghub.utils.WildcardMatcher;

public class XYHistogramChart implements ChunkedResultHandler, ComponentProvider, ChartProvider, ChartInterface, UpdatesEachSecond {
    private double minimumBucket = 20;
    private double maximumBucket = 30;
    private int granularity = 10;
    private int[] bucketCounts = new int[granularity + 2];

    long timeLimit = 5 * 60 * 1000;
    private boolean realtimeUpdate = false;

    private DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    public static String newline = System.getProperty("line.separator");
    private double yMinimumFilter = Float.NaN;

    private JFreeChart chart;

    private String imageFilename;
    private int imageFileWidth = 1024;
    private int imageFileHeight = 768;
    private ChartPanel chartpanel;

    private double upperValueLimit = Double.MAX_VALUE;
    private XYPlot xyplot;

    private String matcher;
    private WildcardMatcher wildcardMatcher = new WildcardMatcher();
    private ArrayList<ChunkedResultFilter> filters;
    private int datapoints;

    private LinkedList<DataElement> dataElements = new LinkedList<XYHistogramChart.DataElement>();

    class DataElement {
        long time;
        double value;
    }

    public void setMatcher(String matcher) {
        this.matcher = matcher;
        wildcardMatcher.setValue(matcher);
    }

    public void setGranularity(int granularity) {
        this.granularity = granularity;
        rebuildArray();
    }

    private void rebuildArray() {
        clearChartData();
        bucketCounts = new int[granularity + 2];
        updateValues();
    }

    public void setMaximumBucket(double maximumBucket) {
        this.maximumBucket = maximumBucket;
    }

    public void setMinimumBucket(double minimumBucket) {
        this.minimumBucket = minimumBucket;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setRealtimeUpdate(boolean realtimeUpdate) {
        this.realtimeUpdate = realtimeUpdate;
    }

    public XYHistogramChart() {
        chart = ChartFactory.createLineChart("", null, "Count", dataset, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot localCategoryPlot = (CategoryPlot) chart.getPlot();
        localCategoryPlot.setRangePannable(true);
        localCategoryPlot.setRangeGridlinesVisible(false);
        localCategoryPlot.setBackgroundPaint(Color.white);
        localCategoryPlot.setDomainGridlinePaint(Color.lightGray);
        localCategoryPlot.setRangeGridlinePaint(Color.lightGray);

        Object localObject = (NumberAxis) localCategoryPlot.getRangeAxis();
        ((NumberAxis) localObject).setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // ChartUtilities.applyCurrentTheme(chart);
        LineAndShapeRenderer localLineAndShapeRenderer = (LineAndShapeRenderer) localCategoryPlot.getRenderer();
        localLineAndShapeRenderer.setBaseShapesVisible(true);
        localLineAndShapeRenderer.setDrawOutlines(true);
        localLineAndShapeRenderer.setUseFillPaint(true);
        localLineAndShapeRenderer.setBaseFillPaint(Color.white);
        localLineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(1.0F));
        // localLineAndShapeRenderer.setSeriesOutlineStroke(0, new
        // BasicStroke(2.0F));
        // localLineAndShapeRenderer.setSeriesShape(0, new
        // Ellipse2D.Double(-5.0D, -5.0D, 10.0D, 10.0D));

        updateValues();

        ChartPanel moo = new ChartPanel(chart);

        moo.setMaximumDrawWidth(1900);
        moo.setMaximumDrawHeight(1200);

        chartpanel = moo;
    }

    private void updateValues() {
        for (int i = 0; i < bucketCounts.length; i++) {
            String label;

            if (i == 0) {
                label = "<";
            } else if (i == bucketCounts.length - 1) {
                label = ">";
            } else {
                int bottomOfRange = (int) (minimumBucket + ((i - 1) * ((maximumBucket - minimumBucket) / granularity)));
                label = Integer.toString(bottomOfRange);
            }

            dataset.setValue(Integer.valueOf(bucketCounts[i]), "Foo", label);
        }
    }

    public void setSplineRenderer(boolean splineRenderer) {
        if (splineRenderer) {
            xyplot.setRenderer(new XYSplineRenderer());
        } else {
            xyplot.setRenderer(new XYLineAndShapeRenderer());
        }
    }

    public void setUpperValueLimit(double upperValueLimit) {
        this.upperValueLimit = upperValueLimit;
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void addFilter(ChunkedResultFilter filter) {
        if (this.filters == null) {
            this.filters = new ArrayList<ChunkedResultFilter>();
        }

        filters.add(filter);
    }

    public ArrayList<ChunkedResultFilter> getFilters() {
        return filters;
    }

    private void addUpperValueExceeded(double value, long startOfCurrentChunk) {
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
        synchronized (dataset) {
            dataset.clear();
        }

        synchronized (dataElements) {
            dataElements.clear();
        }
    }

    public void saveChartData(File folder) {

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

    public void saveChartImage(File folder) {
        String filename = imageFilename;

        if (filename == null) {
            filename = chart.getTitle().getText() + ".png";
        }

        File file = new File(folder, filename);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, imageFileWidth, imageFileHeight);
            System.out.println("Chart written to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save png [%s]", file.getAbsolutePath()), e);
        }
    }

    public void setYMinimumFilter(double yMinimumFilter) {
        this.yMinimumFilter = yMinimumFilter;
    }

    public void onNewChunkedResult(ChunkedResult result) {
        boolean passes;
        String label = result.getSource();
        if (getFilters() != null) {
            passes = false;
            for (ChunkedResultFilter filter : getFilters()) {
                passes |= filter.passes(result);
                if (passes) {
                    if (filter instanceof LabelOverride) {
                        String overrideLabel = ((LabelOverride) filter).getLabel();
                        if (overrideLabel != null) {
                            label = overrideLabel;
                        }
                    }
                    break;
                }
            }
        } else {
            passes = true;
        }

        if (passes) {
            DataElement element = new DataElement();
            element.value = result.getValue();
            element.time = result.getStartOfCurrentChunk();
            synchronized (dataElements) {
                dataElements.addLast(element);
            }

            double value = result.getValue();
            int bucket = getBucketID(value);
            bucketCounts[bucket]++;
            if (realtimeUpdate) {
                synchronized (dataset) {
                    dataset.setValue(Integer.valueOf(bucketCounts[bucket]), "Foo", "" + bucket);
                }
            }

            // xySeries.addOrUpdate(bucket, bucketCounts[bucket]);
            // double[][] array = xySeries.toArray();
            // System.out.println(Arrays.toString(array[0]));
        }
    }

    private int getBucketID(double value) {
        int index;
        if (value < minimumBucket) {
            // The 'under' bucket
            index = 0;
        } else if (value > maximumBucket) {
            // The 'over' bucket
            index = granularity + 1;
        } else {
            double inRange = value - minimumBucket;
            double range = maximumBucket - minimumBucket;
            index = (int) (granularity * (inRange / range));
            // Shift things by one to accomdate the 'under' bucket
            index++;
        }

        return index;
    }

    public void update() {
        long cutoffTime = System.currentTimeMillis() - timeLimit;
        synchronized (dataElements) {
            Iterator<DataElement> iterator = dataElements.iterator();
            boolean keepGoing = true;
            while (iterator.hasNext() && keepGoing) {
                DataElement next = iterator.next();
                if (next.time < cutoffTime) {
                    iterator.remove();
                    if (realtimeUpdate) {
                        removeElement(next);
                    }
                } else {
                    keepGoing = false;
                }
            }
        }

        if (!realtimeUpdate) {
            updateValues();
        }
    }

    private void removeElement(DataElement next) {
        int bucket = getBucketID(next.value);
        bucketCounts[bucket]--;
        dataset.setValue(Integer.valueOf(bucketCounts[bucket]), "Foo", "" + bucket);
    }

    public void setWarningThreshold(double warningThreshold) {
    }

    public void setSevereThreshold(double severeThreshold) {
    }

    @Override
    public void setYAxisLock(double getyAxisLock) {
    }

    @Override
    public void addMarker(long time, String marker) {
        // Hour hour = new Hour(2, new Day(22, 2, 2010));
        // Minute minute = new Minute(15, hour);
        // long d = minute.getFirstMillisecond();
        //
        ValueMarker valuemarker3 = new ValueMarker(time);
        valuemarker3.setPaint(Color.black);
        valuemarker3.setLabel(marker);
        valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        xyplot.addDomainMarker(valuemarker3);        
    }

//    @Override
//    public void tickOver() {
//        // TODO Auto-generated method stub
//        
//    }
}
