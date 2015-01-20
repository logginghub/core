package com.logginghub.logging.frontend.views.timetravel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

/**
 * In this demo, the {@link PeriodAxis} class is used to display both date and
 * day-of-the-week labels on a bar chart.
 */
public class DailyHistoryViewChartPanel extends JPanel {

    private static TimeSeries timeSeries;
    private static TimeSeriesCollection dataset;
    private MyBarRenderer renderer;
    private NavigationListener navigationListener;

    /**
     * A demonstration application showing how to create a simple time series
     * chart. This example uses monthly data.
     * 
     * @param title
     *            the frame title.
     */
    public DailyHistoryViewChartPanel() {
        JFreeChart chart = createChart(createDataset());
        ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPopupMenu(null);

        chartPanel.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {

                org.jfree.chart.entity.ChartEntity chartentity = chartMouseEvent.getEntity();
                if (!(chartentity instanceof XYItemEntity)) {
                    renderer.setHighlightedItem(-1, -1);
                    return;
                }
                else {
                    XYItemEntity categoryitementity = (XYItemEntity) chartentity;
                    XYDataset categorydataset = categoryitementity.getDataset();
                    int item = categoryitementity.getItem();
                    renderer.setHighlightedItem(0, item);
                }
            }

            public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {

                org.jfree.chart.entity.ChartEntity chartentity = chartMouseEvent.getEntity();
                if ((chartentity instanceof XYItemEntity)) {

                    XYItemEntity categoryitementity = (XYItemEntity) chartentity;
                    XYDataset categorydataset = categoryitementity.getDataset();
                    int item = categoryitementity.getItem();

                    TimeSeriesDataItem dataItem = timeSeries.getDataItem(item);
                    long time = dataItem.getPeriod().getStart().getTime();
                    System.out.println("Selected " + new Date(time).toGMTString() + "(" + time + ")");
                    
                    Calendar gmtcalendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                    Calendar localcalendar = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
                    System.out.println(gmtcalendar.getTimeZone().getDisplayName());
                    System.out.println(localcalendar.getTimeZone().getDisplayName());
                    
                    gmtcalendar.setTimeInMillis(time);
                    localcalendar.setTimeInMillis(time);
                    
                    System.out.println(gmtcalendar.getTime());
                    System.out.println(localcalendar.getTime());
                    
                    // Filthy hack
                    time = time + 1 * 60 * 60 * 1000;
                    
                    System.out.println("Selected " + new Date(time) + "(" + time + ")");
                    
                    if (chartMouseEvent.getTrigger().getClickCount() == 2) {
                        navigationListener.onDaySelected(item, time);
                    }
                }

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
    private JFreeChart createChart(IntervalXYDataset dataset) {

        JFreeChart chart = ChartFactory.createXYBarChart("Historical events",
                                                         "Day",
                                                         true,
                                                         "Events recorded",
                                                         dataset,
                                                         PlotOrientation.VERTICAL,
                                                         true,
                                                         true,
                                                         false);

        XYPlot plot = (XYPlot) chart.getPlot();

        PeriodAxis domainAxis = new PeriodAxis("Day");
        domainAxis.setAutoRangeTimePeriodClass(Day.class);
        PeriodAxisLabelInfo[] info = new PeriodAxisLabelInfo[3];
        info[0] = new PeriodAxisLabelInfo(Day.class, new SimpleDateFormat("d"));
        info[1] = new PeriodAxisLabelInfo(Day.class,
                                          new SimpleDateFormat("E"),
                                          new RectangleInsets(2, 2, 2, 2),
                                          new Font("SansSerif", Font.BOLD, 10),
                                          Color.blue,
                                          false,
                                          new BasicStroke(0.0f),
                                          Color.lightGray);
        info[2] = new PeriodAxisLabelInfo(Month.class, new SimpleDateFormat("MMM"));
        domainAxis.setLabelInfo(info);
        plot.setDomainAxis(domainAxis);

        plot.setDomainGridlinesVisible(true);

        renderer = new MyBarRenderer();
        renderer.setDrawBarOutline(true);
        plot.setRenderer(renderer);

        ChartUtilities.applyCurrentTheme(chart);

        return chart;

    }

    private IntervalXYDataset createDataset() {
        timeSeries = new TimeSeries("Events");
        timeSeries.add(new Day(1, 4, 2006), 14.5);
        timeSeries.add(new Day(2, 4, 2006), 11.5);
        timeSeries.add(new Day(3, 4, 2006), 13.7);
        timeSeries.add(new Day(4, 4, 2006), 10.5);
        timeSeries.add(new Day(5, 4, 2006), 14.9);
        timeSeries.add(new Day(6, 4, 2006), 15.7);
        timeSeries.add(new Day(7, 4, 2006), 11.5);
        timeSeries.add(new Day(8, 4, 2006), 9.5);
        timeSeries.add(new Day(9, 4, 2006), 10.9);
        timeSeries.add(new Day(10, 4, 2006), 14.1);
        timeSeries.add(new Day(11, 4, 2006), 12.3);
        timeSeries.add(new Day(12, 4, 2006), 14.3);
        timeSeries.add(new Day(13, 4, 2006), 19.0);
        timeSeries.add(new Day(14, 4, 2006), 17.9);
        
        dataset = new TimeSeriesCollection();
        dataset.addSeries(timeSeries);
        return dataset;
    }

    static class MyBarRenderer extends XYBarRenderer {

        public void setHighlightedItem(int i, int j) {
            if (highlightRow == i && highlightColumn == j) {
                return;
            }
            else {
                highlightRow = i;
                highlightColumn = j;
                notifyListeners(new RendererChangeEvent(this));
                return;
            }
        }

        public Paint getItemOutlinePaint(int i, int j) {
            if (i == highlightRow && j == highlightColumn) {
                return Color.yellow;
            }
            else {
                return super.getItemOutlinePaint(i, j);
            }
        }

        private int highlightRow;
        private int highlightColumn;

        MyBarRenderer() {
            highlightRow = -1;
            highlightColumn = -1;
        }
    }

    public void setNavigationListener(NavigationListener navigationListener) {
        this.navigationListener = navigationListener;
    }

//    public void setDailySummaries(List<DailySummary> dailySummaries) {
//
//        timeSeries.clear();
//
//        Calendar calendar = new GregorianCalendar();
//        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
//        
//        for (DailySummary dailySummary : dailySummaries) {
//            
//            System.out.println(new Date(dailySummary.getTime()));
//            calendar.setTimeInMillis(dailySummary.getTime());
//            int day = calendar.get(Calendar.DAY_OF_MONTH);
//            int month = calendar.get(Calendar.MONTH) + 1;
//            int year = calendar.get(Calendar.YEAR);
//            
//            System.out.println(calendar);
//            
//            timeSeries.add(new Day(day, month, year), dailySummary.getCount());    
//        } 
//    }
}
