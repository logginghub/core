package com.logginghub.logging.frontend.analysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
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

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.WildcardMatcher;
import com.logginghub.utils.vectormaths.Vector2d;
import com.logginghub.utils.vectormaths.Vector2f;

public class XYScatterChart implements ChunkedResultHandler, ComponentProvider, ChartProvider, ChartInterface {
    private JFreeChart chart;
    private ChartPanel chartpanel;
    private int datapoints = 60 * 5;

    private ArrayList<ChunkedResultFilter> filters;
    private int imageFileHeight = 768;
    private String imageFilename;
    private int imageFileWidth = 1024;

    private HighlightableRenderer renderer;
    private String matcher;
    private Map<String, XYSeries> seriesForSource = new HashMap<String, XYSeries>();

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

    private XYTextAnnotation seriesAnnotation = null;

    public static String newline = System.getProperty("line.separator");
    private DateAxis xAxis;
    private NumberAxis yAxis;

    public XYScatterChart() {
        xAxis = new DateAxis("Timestamp");
        yAxis = new NumberAxis("Count");
        yAxis.setAutoRangeIncludesZero(false);

        renderer = new NonSplineRenderer(true, false);
        xyplot = new XYPlot(xyseriescollection, xAxis, yAxis, renderer.getRenderer());
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.lightGray);

        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);

//        renderer.setBaseShapesVisible(false);
//        renderer.setBaseShapesFilled(false);

        // renderer.setBaseToolTipGenerator(new TooltipItemGenerator());

        chart = new JFreeChart("XYSplineRenderer", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        chart.setBackgroundPaint(Color.white);

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.BOTTOM);

        chartpanel = new ChartPanel(chart);
        chartpanel.setPopupMenu(null);
        chartpanel.setBackground(Color.white);
        chartpanel.setOpaque(true);

        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        chartpanel.addChartMouseListener(chartMouseListener);

        chartpanel.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                renderer.setHighlightedSeries(-1);
                if (seriesAnnotation != null) {
                    seriesAnnotation.setX(0);
                    seriesAnnotation.setY(0);
                }
            }
        });

    }

    public void addFilter(ChunkedResultFilter filter) {
        if (this.filters == null) {
            this.filters = new ArrayList<ChunkedResultFilter>();
        }

        filters.add(filter);
    }

    public void clearChartData() {
        synchronized (seriesForSource) {
            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                xySeries.clear();
            }
        }
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

    public XYSeries getSeriesForSource(String label) {
        XYSeries xySeries;
        synchronized (seriesForSource) {
            xySeries = seriesForSource.get(label);
            if (xySeries == null) {
                xySeries = new XYSeries(label);
                xyseriescollection.addSeries(xySeries);
                xySeries.setMaximumItemCount(datapoints);
                seriesForSource.put(label, xySeries);
            }
        }
        return xySeries;
    }

    public XYSeriesCollection getXyseriescollection() {
        return xyseriescollection;
    }

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
            addValue(label, result.getStartOfCurrentChunk(), value);
            getComponent().repaint();
        }
    }

    public void addValue(String label, long time, double value) {

        final XYSeries xySeries = getSeriesForSource(label);

        final long startOfCurrentChunk = time;
        if (value > upperValueLimit) {
            value = upperValueLimit;
            addUpperValueExceeded(value, startOfCurrentChunk);
        }

        final double finalValue = value;

        // TODO : introduce result batching, as this needs to be done in the
        // swing thread to be
        // safe
        addValue(xySeries, startOfCurrentChunk, finalValue);
    }

    private void addValue(final XYSeries xySeries, final long time, final double value) {
        xySeries.add(time, value);
        checkThresholds(value);
        updateRange(value, xySeries);
    }

    private void updateRange(double value, XYSeries xySeries) {
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

    public void reset() {
        synchronized (seriesForSource) {
            xyseriescollection.removeAllSeries();
            seriesForSource.clear();
        }
    }

    public void saveChartData(File folder) {
        int seriesCount = xyplot.getDataset().getSeriesCount();

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
                    if (d != null && !Double.isNaN(d)) {
                        builder.append(d);
                    }
                    builder.append(",");
                }

                builder.append(newline);
            }
        }

        String filename = chart.getTitle().getText() + ".csv";
        File file = new File(folder, filename);
        FileUtils.write(builder.toString(), file);
        System.out.println("Saved data to " + file.getAbsolutePath());
    }

    public void setDatapoints(int datapoints) {
        this.datapoints = datapoints;

        // Update the existing series
        synchronized (seriesForSource) {
            Collection<XYSeries> values = seriesForSource.values();
            for (XYSeries xySeries : values) {
                xySeries.setMaximumItemCount(datapoints);
            }
        }
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

        HighlightableRenderer renderer;

        if (splineRenderer) {
            renderer = new SplineRenderer();
        }
        else {
            renderer = new NonSplineRenderer(true, false);
        }

//        renderer.setShapesVisible(false);
//        renderer.setShapesFilled(false);

        this.renderer = renderer;
        
        xyplot.setRenderer(renderer.getRenderer());

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
        this.yAxisLock = yAxisLock;
        if (Double.isNaN(yAxisLock)) {
            yAxis.setAutoRange(true);
        }
        else {
            yAxis.setAutoRange(false);
            yAxis.setRange(0, yAxisLock);
        }
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

    // @Override
    // public void tickOver() {
    // // Update the existing series
    // synchronized (seriesForSource) {
    // Collection<XYSeries> values = seriesForSource.values();
    // for (XYSeries xySeries : values) {
    // // xySeries.add(System.currentTimeMillis()+3000, null);
    // }
    // }
    // }

    private ChartMouseListener chartMouseListener = new ChartMouseListener() {

        public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

            double mouseX = chartMouseEvent.getTrigger().getX();
            double mouseY = chartMouseEvent.getTrigger().getY();

            double lowestLineDistance = Double.MAX_VALUE;

            ChartEntity nearestEntity = null;
            ChartEntity nearestEntityByLine = null;

            Map<Integer, Vector2f> previousEntityBySeries = new HashMap<Integer, Vector2f>();

            Vector2f nearestPoint = null;

            EntityCollection entities = chartpanel.getChartRenderingInfo().getEntityCollection();

            if (entities != null) {

                int entityCount = entities.getEntityCount();
                for (int i = 0; i < entityCount; i++) {
                    ChartEntity entity = entities.getEntity(i);

                    if (entity instanceof XYItemEntity) {

                        XYItemEntity xyItemEntity = (XYItemEntity) entity;
                        int seriesIndex = xyItemEntity.getSeriesIndex();

                        Shape area = entity.getArea();

                        Rectangle bounds = area.getBounds();

                        double centerX = bounds.getCenterX();
                        double centerY = bounds.getCenterY();

                        Vector2f mousePoint = new Vector2f((float) mouseX, (float) mouseY);
                        Vector2f currentPoint = new Vector2f((float) centerX, (float) centerY);
                        Vector2f previousPoint = previousEntityBySeries.get(seriesIndex);

                        if (previousPoint != null) {

                            double distance = distanceFromPointToSegment((float) mouseX,
                                                                         (float) mouseY,
                                                                         currentPoint.x,
                                                                         currentPoint.y,
                                                                         previousPoint.x,
                                                                         previousPoint.y);

                            if (distance < lowestLineDistance) {
                                lowestLineDistance = distance;
                                nearestEntityByLine = entity;

                                nearestPoint = GetClosetPoint(currentPoint, previousPoint, mousePoint, true);
                            }
                        }

                        previousEntityBySeries.put(seriesIndex, currentPoint);
                    }
                }
            }

            nearestEntity = nearestEntityByLine;

            if (nearestEntity != null) {
                XYItemEntity xyItemEntity = (XYItemEntity) nearestEntity;

                XYDataset dataset = xyItemEntity.getDataset();
                int seriesIndex = xyItemEntity.getSeriesIndex();
                int item = xyItemEntity.getItem();

                double x = dataset.getX(seriesIndex, item).doubleValue();
                double y = dataset.getY(seriesIndex, item).doubleValue();

                x = nearestPoint.x;
                y = nearestPoint.y;

                // Convert the coordinate values into actual chart values for the annotation
                Rectangle2D greyChartArea = chartpanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                XYPlot plot = (XYPlot) chart.getPlot();

                x = ((DateAxis) plot.getDomainAxis()).java2DToValue((double) x, greyChartArea, plot.getDomainAxisEdge());
                y = ((NumberAxis) plot.getRangeAxis()).java2DToValue((double) y, greyChartArea, plot.getRangeAxisEdge());

                String series = dataset.getSeriesKey(seriesIndex).toString();

                if (seriesAnnotation == null) {
                    seriesAnnotation = new XYTextAnnotation(series, x, y);
                    xyplot.addAnnotation(seriesAnnotation);
                }
                else {
                    seriesAnnotation.setText(series);
                    seriesAnnotation.setX(x);
                    seriesAnnotation.setY(y);
                    seriesAnnotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
                }

                renderer.setHighlightedSeries(xyItemEntity.getSeriesIndex());
            }

        }

        public void chartMouseClicked(ChartMouseEvent chartmouseevent) {}

    };

    // from : http://www.gamedev.net/topic/444154-closest-point-on-a-line/
    public static Vector2d GetClosetPoint(Vector2d A, Vector2d B, Vector2d P, boolean segmentClamp) {
        Vector2d AP = P.subtract(A);
        Vector2d AB = B.subtract(A);
        double ab2 = AB.x * AB.x + AB.y * AB.y;
        double ap_ab = AP.x * AB.x + AP.y * AB.y;
        double t = ap_ab / ab2;
        if (segmentClamp) {
            if (t < 0.0f) t = 0.0f;
            else if (t > 1.0f) t = 1.0f;
        }
        Vector2d Closest = A.add(AB.mult(t));
        return Closest;
    }

    public static Vector2f GetClosetPoint(Vector2f A, Vector2f B, Vector2f P, boolean segmentClamp) {
        Vector2f AP = P.subtract(A);
        Vector2f AB = B.subtract(A);
        double ab2 = AB.x * AB.x + AB.y * AB.y;
        double ap_ab = AP.x * AB.x + AP.y * AB.y;
        double t = ap_ab / ab2;
        if (segmentClamp) {
            if (t < 0.0f) t = 0.0f;
            else if (t > 1.0f) t = 1.0f;
        }
        Vector2f Closest = A.add(AB.mult((float) t));
        return Closest;
    }

    // from :
    // http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
    public static double distanceFromPointToSegment(float x, float y, float x1, float y1, float x2, float y2) {

        float A = x - x1;
        float B = y - y1;
        float C = x2 - x1;
        float D = y2 - y1;

        float dot = A * C + B * D;
        float len_sq = C * C + D * D;
        float param = dot / len_sq;

        float xx, yy;

        if (param < 0 || (x1 == x2 && y1 == y2)) {
            xx = x1;
            yy = y1;
        }
        else if (param > 1) {
            xx = x2;
            yy = y2;
        }
        else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        float dx = x - xx;
        float dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
