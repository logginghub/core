package com.logginghub.logging.frontend.views.timetravel;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/**
 * In this demo, the {@link PeriodAxis} class is used to display both date and
 * day-of-the-week labels on a bar chart.
 */
public class HourlyHistoryViewChartPanel extends JPanel {

    private static TimeSeries errorSeries;
    private static TimeSeries warningSeries;
    private static TimeSeries infoSeries;
    private static TimeSeries debugSeries;
    private static TimeSeries totalSeries;
    private NavigationListener navigationListener;
    private IntervalMarker marker;

    /**
     * A demonstration application showing how to create a simple time series
     * chart. This example uses monthly data.
     * 
     * @param title
     *            the frame title.
     */
    public HourlyHistoryViewChartPanel(String title) {
        final JFreeChart chart = createChart(createDataset());
        final ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPopupMenu(null);

        chartPanel.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

            }

            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {

                // org.jfree.chart.entity.ChartEntity chartentity =
                // chartMouseEvent.getEntity();
                // if (chartentity instanceof PlotEntity) {

                Point2D p = chartPanel.translateScreenToJava2D(chartMouseEvent.getTrigger().getPoint());
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) chart.getPlot(); // your plot
                double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());

                if (chartX < marker.getStartValue()) {
                    // Before the start, so increase the range towards the
                    // start
                    marker.setStartValue(chartX);
                }
                else if (chartX > marker.getEndValue()) {
                    // After the end, so increase the range towards the end
                    marker.setEndValue(chartX);
                }
                else {
                    // Must be in the middle somewhere, work out which side
                    // to alter...
                    double distanceFromStart = Math.abs(chartX - marker.getStartValue());
                    double distanceFromEnd = Math.abs(chartX - marker.getEndValue());
                    if (distanceFromStart > distanceFromEnd) {
                        // Nearer the end
                        marker.setEndValue(chartX);
                    }
                    else {
                        marker.setStartValue(chartX);
                    }
                }

                // }
                // else if ((chartentity instanceof XYItemEntity)) {
                //
                // XYItemEntity categoryitementity = (XYItemEntity)
                // chartentity;
                // XYDataset categorydataset =
                // categoryitementity.getDataset();
                // int item = categoryitementity.getItem();
                // }
            }
        });

        add(chartPanel);
    }

    /**
     * Creates a chart.
     * 
     * @param dataset
     *            a dataset.
     * 
     * @return A chart.
     */
    private JFreeChart createChart(XYDataset xydataset) {
        JFreeChart jfreechart = ChartFactory.createTimeSeriesChart("Hourly View", "Time", "Log events", xydataset, true, true, false);

        XYPlot categoryplot = (XYPlot) jfreechart.getPlot();

        GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.red, 1.0F, 1.0F, Color.orange);

        marker = new IntervalMarker(1316336940000f, 1316337300000f, gradientpaint);
        marker.setAlpha(0.5F);
        marker.setLabel("Marker Label");
        marker.setLabelFont(new Font("Dialog", 0, 11));
        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        marker.setLabelOffset(new RectangleInsets(2D, 5D, 2D, 5D));
        categoryplot.addDomainMarker(marker, Layer.BACKGROUND);

        return jfreechart;

    }

    /**
     * Creates a dataset, consisting of two series of monthly data.
     * 
     * @return the dataset.
     */
    @SuppressWarnings("deprecation") private static XYDataset createDataset() {
        
        errorSeries = new TimeSeries("Errors", org.jfree.data.time.Minute.class);
        warningSeries = new TimeSeries("Warnings", org.jfree.data.time.Minute.class);
        infoSeries = new TimeSeries("Info", org.jfree.data.time.Minute.class);
        debugSeries = new TimeSeries("Debug", org.jfree.data.time.Minute.class);
        totalSeries = new TimeSeries("Total", org.jfree.data.time.Minute.class);


        TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
        timeseriescollection.addSeries(errorSeries);
        timeseriescollection.addSeries(warningSeries);
        timeseriescollection.addSeries(infoSeries);
        timeseriescollection.addSeries(debugSeries);
        timeseriescollection.addSeries(totalSeries);
        return timeseriescollection;
    }

    public void setNavigationListener(NavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

//    public void setChunkDetails(List<ChunkDetails> chunkDetails) {
//        errorSeries.clear();
//
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
//        
//        for (ChunkDetails dailySummary : chunkDetails) {
//                      
//            calendar.setTimeInMillis(dailySummary.getChunkStart());
//            System.out.println(calendar.getTime());
//           
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            int month = calendar.get(Calendar.MONTH);
//            int year = calendar.get(Calendar.YEAR);
//            int hour = calendar.get(Calendar.HOUR_OF_DAY);
//            int minute = calendar.get(Calendar.MINUTE);
//            
//            errorSeries.add(new Minute(minute, hour, day, month, year), dailySummary.getErrors());    
//            warningSeries.add(new Minute(minute, hour, day, month, year), dailySummary.getWarnings());
//            debugSeries.add(new Minute(minute, hour, day, month, year), dailySummary.getDebug());
//            infoSeries.add(new Minute(minute, hour, day, month, year), dailySummary.getInfo());
//            totalSeries.add(new Minute(minute, hour, day, month, year), dailySummary.getTotalEvents());
//        }         
//    }
}
