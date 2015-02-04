package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by james on 02/02/15.
 */
public class DualChart extends JPanel {

    private final ChartPanel chartpanel;
    private final JFreeChart chart;
    private final DateAxis xAxis;

    private final XYSeriesCollection xyseriescollection1;
    private final XYSeriesCollection xyseriescollection2;

    public DualChart() {
        setLayout(new BorderLayout());

        xyseriescollection1 = new XYSeriesCollection();
        xyseriescollection2 = new XYSeriesCollection();

        // create plot ...
//        XYDataset data1 = createDataset1();
        XYItemRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
//        renderer1.setBaseToolTipGenerator(new StandardXYToolTipGenerator(StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT, new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));
        renderer1.setSeriesStroke(0, new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        renderer1.setSeriesPaint(0, Color.blue);

        xAxis = new DateAxis("Time");

        ValueAxis rangeAxis = new NumberAxis("");
        XYPlot plot1 = new XYPlot(xyseriescollection1, null, rangeAxis, renderer1);
        plot1.setBackgroundPaint(Color.lightGray);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setRangeGridlinePaint(Color.white);

        // Embed legend in the first plot
        LegendTitle lt = new LegendTitle(plot1);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(200, 200, 255, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.02, 0.02, lt, RectangleAnchor.BOTTOM_LEFT);
//        ta.setMaxWidth(0.48);
        plot1.addAnnotation(ta);

        // add a second dataset and renderer...

        XYBarRenderer renderer2 = new XYBarRenderer();
//        {
//            public Paint getItemPaint(int series, int item) {
//                XYDataset dataset = getPlot().getDataset();
//                if (dataset.getYValue(series, item) >= 0.0) {
//                    return Color.red;
//                } else {
//                    return Color.green;
//                }
//            }
//        };

        renderer2.setSeriesPaint(0, Color.red);
        renderer2.setDrawBarOutline(false);

        XYPlot plot2 = new XYPlot(xyseriescollection2, null, new NumberAxis(""), renderer2);
        plot2.setBackgroundPaint(Color.lightGray);
        plot2.setDomainGridlinePaint(Color.white);
        plot2.setRangeGridlinePaint(Color.white);

        // Embed legend in the first plot
        LegendTitle lt2 = new LegendTitle(plot2);
        lt2.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt2.setBackgroundPaint(new Color(200, 200, 255, 100));
        lt2.setFrame(new BlockBorder(Color.white));
        lt2.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta2 = new XYTitleAnnotation(0.02, 0.02, lt2, RectangleAnchor.BOTTOM_LEFT);
//        ta2.setMaxWidth(0.48);
        plot2.addAnnotation(ta2);

        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(xAxis);
        cplot.add(plot1, 3);
        cplot.add(plot2, 2);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);
        cplot.setDomainPannable(true);

        // return a new chart containing the overlaid plot...
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
//        TextTitle source = new TextTitle("Source: http://www.publicdebt.treas.gov/opd/opdhisms.htm", new Font("Dialog", Font.PLAIN, 10));
//        source.setPosition(RectangleEdge.BOTTOM);
//        source.setHorizontalAlignment(HorizontalAlignment.RIGHT);
//        chart.addSubtitle(source);

//        LegendTitle legend = new LegendTitle(cplot);
//        chart.addSubtitle(legend);

        ChartUtilities.applyCurrentTheme(chart);
        renderer2.setBarPainter(new StandardXYBarPainter());
        renderer2.setShadowVisible(false);

        chartpanel = new ChartPanel(chart);
        chartpanel.setPopupMenu(null);
        chartpanel.setBackground(Color.white);
        chartpanel.setOpaque(true);

        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);


        add(chartpanel, BorderLayout.CENTER);
    }


    public void bind(final ChartDetailsModel chartDetailsModel, XYChartDataModel dataModel1, XYChartDataModel dataModel2) {

        chartDetailsModel.getTitle().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                if (StringUtils.isNotNullOrEmpty(newValue)) {
                    chart.setTitle(newValue);
                }
            }
        });

        chartDetailsModel.getSubtitle().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                if (StringUtils.isNotNullOrEmpty(newValue)) {
                    List<Title> subtitles = new ArrayList<Title>();
                    subtitles.add(new TextTitle(newValue));
                    chart.setSubtitles(subtitles);
                }
            }
        });

        chartDetailsModel.getEndTime().addListenerAndNotifyCurrent(new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long oldValue, Long newValue) {
                xAxis.setRange(chartDetailsModel.getStartTime().get(), newValue);
            }
        });

        bindData(dataModel1, xyseriescollection1);
        bindData(dataModel2, xyseriescollection2);
    }

    private void bindData(XYChartDataModel dataModel1, final XYSeriesCollection xyseriescollection) {
        final Map<XYSeriesModel, XYSeries> seriesLookup = new FactoryMap<XYSeriesModel, XYSeries>() {
            @Override protected XYSeries createEmptyValue(XYSeriesModel key) {
                String label = key.getLabel().get();
                return new XYSeries(label);
            }
        };

        final Map<XYSeriesModel, ObservableListListener<XYValue>> listenerLookup = new HashMap<XYSeriesModel, ObservableListListener<XYValue>>();

        dataModel1.getSeries().addListenerAndNotifyCurrent(new ObservableListListener<XYSeriesModel>() {
            @Override public void onAdded(XYSeriesModel XYSeriesModel) {
                final XYSeries xySeries = seriesLookup.get(XYSeriesModel);
                xyseriescollection.addSeries(xySeries);

                ObservableListListener<XYValue> listener = new ObservableListListener<XYValue>() {
                    @Override public void onAdded(XYValue xyValue) {
                        xySeries.add(xyValue.x, xyValue.y);
                    }

                    @Override public void onRemoved(XYValue xyValue, int index) {
                        xySeries.remove(index);
                    }

                    @Override public void onCleared() {
                        xySeries.clear();
                    }
                };

                XYSeriesModel.getValues().addListenerAndNotifyCurrent(listener);
                listenerLookup.put(XYSeriesModel, listener);

            }

            @Override public void onRemoved(XYSeriesModel XYSeriesModel, int index) {
                xyseriescollection.removeSeries(seriesLookup.remove(XYSeriesModel));
                XYSeriesModel.getValues().removeListener(listenerLookup.remove(XYSeriesModel));
            }

            @Override public void onCleared() {
                xyseriescollection.removeAllSeries();
            }
        });
    }
}
