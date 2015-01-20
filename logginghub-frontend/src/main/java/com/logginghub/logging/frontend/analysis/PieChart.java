package com.logginghub.logging.frontend.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

import com.logginghub.utils.ArrayListBackedHashMap;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NotImplementedException;
import com.logginghub.utils.WildcardMatcher;

public class PieChart implements ChunkedResultHandler, ComponentProvider, ChartProvider, ChartInterface {
    private JFreeChart chart;
    private ChartPanel chartpanel;
    private int datapoints = 60 * 5;

    private ArrayList<ChunkedResultFilter> filters;
    private int imageFileHeight = 768;
    private String imageFilename;
    private int imageFileWidth = 1024;

    private String matcher;
    // private Map<String, XYSeries> seriesForSource = new HashMap<String, XYSeries>();

    private Color severeColour = ColourUtils.parseColor("Tomato");
    private double severeThreshold = Double.NaN;
    private double upperValueLimit = Double.MAX_VALUE;
    private Color warningColour = ColourUtils.parseColor("Gold");

    private double warningThreshold = Double.NaN;
    private WildcardMatcher wildcardMatcher = new WildcardMatcher();

    private XYPlot xyplot;

    private XYSeriesCollection xyseriescollection = new XYSeriesCollection();

    private double yAxisLock = Double.NaN;

    private double yMinimumFilter = Double.NaN;

    public static String newline = System.getProperty("line.separator");
    private DefaultPieDataset dataset;
    
    private int topXResults = Integer.MAX_VALUE;
    private boolean showOther = false;
    

    public PieChart() {
        dataset = new DefaultPieDataset();
        chart = ChartFactory.createPieChart("Title", dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.white);
        plot.setShadowPaint(Color.white);

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.BOTTOM);

        chartpanel = new ChartPanel(chart);
        chartpanel.setPopupMenu(null);

        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);
    }

    public void addFilter(ChunkedResultFilter filter) {
        if (this.filters == null) {
            this.filters = new ArrayList<ChunkedResultFilter>();
        }

        filters.add(filter);
    }

    public void clearChartData() {
        dataset.clear();
        resultsModel.clear();
    }

    @Override public void complete() {

    }

    public void saveChartImage(File folder) {
        String filename = imageFilename;

        if (filename == null) {
            filename = chart.getTitle().getText() + ".png";
        }

        File file = new File(folder, filename);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, 1280, 720);
            System.out.println("Chart written to " + file.getAbsolutePath());
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to save png [%s]", file.getAbsolutePath()), e);
        }
    }

    public JFreeChart getChart() {
        return chart;
    }

    public ChartPanel getChartpanel() {
        return chartpanel;
    }

    public JComponent getComponent() {
        return chartpanel;
    }

    public ArrayList<ChunkedResultFilter> getFilters() {
        return filters;
    }

    public XYSeriesCollection getXyseriescollection() {
        return xyseriescollection;
    }

    class SeriesValue {
        public SeriesValue(String label) {
            this.label = label;
        }

        String label;
        double value;
    }

    private ArrayListBackedHashMap<String, SeriesValue> resultsModel = new ArrayListBackedHashMap<String, SeriesValue>() {
        protected SeriesValue createEmptyValue(String key) {
            return new SeriesValue(key);
        }
    };

    public void onNewChunkedResult(ChunkedResult result) {

        boolean passes;
        String label = result.getSource();
        if (filters != null) {
            passes = false;
            for (ChunkedResultFilter filter : filters) {
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
        }
        else {
            passes = true;
        }

        double value = result.getValue();
        if (passes) {
            passes = (Double.isNaN(yMinimumFilter) || value > yMinimumFilter);
        }

        if (passes) {
            resultsModel.get(label).value = value;

            Collections.sort(resultsModel.getList(), new Comparator<SeriesValue>() {
                @Override public int compare(SeriesValue o1, SeriesValue o2) {
                    return CompareUtils.compare(o2.value, o1.value);
                }
            });

            dataset.clear();
            List<SeriesValue> list = resultsModel.getList();
            int max = Math.min(list.size(), topXResults);

            for (int i = 0; i < max; i++) {
                SeriesValue seriesValue = list.get(i);
                dataset.setValue(seriesValue.label, seriesValue.value);
            }

            if (showOther) {
                double other = 0;
                int remaining = list.size() - max;
                for (int i = max; i < max + remaining; i++) {
                    SeriesValue seriesValue = list.get(i);
                    other += seriesValue.value;
                }
                dataset.setValue("<Other>", other);
            }

            dataset.sortByValues(SortOrder.DESCENDING);

            getComponent().repaint();
        }
    }

    public void reset() {
        clearChartData();
    }

    public void saveChartData(File folder) {

        StringBuilder builder = new StringBuilder();

        List<Comparable<?>> keys = dataset.getKeys();
        for (Comparable<?> object : keys) {
            Number valueObject = dataset.getValue(object);

            String key = object.toString();
            String value = valueObject.toString();

            builder.append(key).append(",").append(value).append(newline);

        }

        String filename = chart.getTitle().getText() + ".csv";
        File file = new File(folder, filename);
        FileUtils.write(builder.toString(), file);
        System.out.println("Saved data to " + file.getAbsolutePath());
    }

    public void setImageFileHeight(int imageFileHeight) {
        this.imageFileHeight = imageFileHeight;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public void setImageFileWidth(int imageFileWidth) {
        this.imageFileWidth = imageFileWidth;
    }

    public void setMatcher(String matcher) {
        this.matcher = matcher;
        wildcardMatcher.setValue(matcher);
    }

    public void setSevereThreshold(double severeThreshold) {
        this.severeThreshold = severeThreshold;
    }

    public void setSplineRenderer(boolean splineRenderer) {
        if (splineRenderer) {
            xyplot.setRenderer(new XYSplineRenderer());
        }
        else {
            xyplot.setRenderer(new XYLineAndShapeRenderer());
        }
    }

    public void setTitle(String title) {
        chart.setTitle(title);
    }

    public void setUpperValueLimit(double upperValueLimit) {
        this.upperValueLimit = upperValueLimit;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    @Override public void setYAxisLock(double yAxisLock) {
        throw new NotImplementedException();
    }

    public void setYMinimumFilter(double yMinimumFilter) {
        this.yMinimumFilter = yMinimumFilter;
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

    public void addMarker(long time, String marker) {
        ValueMarker valuemarker3 = new ValueMarker(time);
        valuemarker3.setStroke(new BasicStroke(1));
        valuemarker3.setPaint(Color.black);
        if (marker.length() > 0) {
            valuemarker3.setLabel(marker);
        }
        valuemarker3.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        valuemarker3.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        xyplot.addDomainMarker(valuemarker3);
    }

    private void checkThresholds(double value) {
        if (warningThreshold != Double.NaN && value >= warningThreshold) {
            if (severeThreshold != Double.NaN && value >= severeThreshold) {
                chart.setBackgroundPaint(severeColour);
            }
            else {
                chart.setBackgroundPaint(warningColour);
            }
        }
        else {
            chart.setBackgroundPaint(Color.white);
        }
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

    @Override public void setDatapoints(int intValue) {

    }

    public void setTopXResults(int topXResults) {
        this.topXResults = topXResults;
    }

    public int getTopXResults() {
        return topXResults;
    }

    public void setShowOther(boolean showOther) {
        this.showOther = showOther;
    }
    
    public boolean isShowOther() {
        return showOther;
    }
}
