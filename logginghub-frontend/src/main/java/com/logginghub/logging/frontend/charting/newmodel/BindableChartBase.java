package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

import javax.swing.JPanel;
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
public abstract class BindableChartBase extends JPanel {

    private final DateAxis xAxis;
    private final NumberAxis yAxis;
    private final XYSeriesCollection xyseriescollection;
    private final AbstractXYItemRenderer renderer;
    private final XYPlot xyplot;
    private final JFreeChart chart;
    private final ChartPanel chartpanel;

    public BindableChartBase() {
        super(new BorderLayout());

        xAxis = new DateAxis("Timestamp");
        yAxis = new NumberAxis("Count");
        yAxis.setAutoRangeIncludesZero(false);

        xyseriescollection = new XYSeriesCollection();

        //        renderer = new XYStepRenderer();
        renderer = getRenderer();

//        GradientPaint gp0 = new GradientPaint(
//                50.0f, 0.0f, Color.RED,
//                500.0f, 0.0f, Color.GREEN,false
//        );
//        renderer.setSeriesPaint(0, gp0);

        xyplot = new XYPlot(xyseriescollection, xAxis, yAxis, renderer);
        xyplot.setBackgroundPaint(Color.white);
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlinePaint(Color.lightGray);

        xyplot.setDomainCrosshairVisible(true);
        xyplot.setRangeCrosshairVisible(true);

        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, xyplot, true);
        chart.setBackgroundPaint(Color.white);

//        LegendTitle legend = chart.getLegend();
//        legend.setVisible(true);
//        legend.setPosition(RectangleEdge.BOTTOM);

        chartpanel = new ChartPanel(chart);
        chartpanel.setPopupMenu(null);
        chartpanel.setBackground(Color.white);
        chartpanel.setOpaque(true);

        chartpanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartpanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        LegendTitle lt = new LegendTitle(xyplot);
        lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
        lt.setBackgroundPaint(new Color(200, 200, 255, 100));
        lt.setFrame(new BlockBorder(Color.white));
        lt.setPosition(RectangleEdge.BOTTOM);
        XYTitleAnnotation ta = new XYTitleAnnotation(0.98, 0.02, lt, RectangleAnchor.BOTTOM_RIGHT);
        ta.setMaxWidth(0.48);
        xyplot.addAnnotation(ta);

        add(chartpanel, BorderLayout.CENTER);

    }

    protected abstract AbstractXYItemRenderer getRenderer();

    public void bind(final ChartDetailsModel chartDetailsModel, XYChartDataModel dataModel) {

        final long start = System.currentTimeMillis();

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

        final Map<XYSeriesModel, XYSeries> seriesLookup = new FactoryMap<XYSeriesModel, XYSeries>() {
            @Override protected XYSeries createEmptyValue(XYSeriesModel key) {
                String label = key.getLabel().get();
                return new XYSeries(label);
            }
        };

        final Map<XYSeriesModel, ObservableListListener<XYValue>> listenerLookup = new HashMap<XYSeriesModel, ObservableListListener<XYValue>>();

        dataModel.getSeries().addListenerAndNotifyCurrent(new ObservableListListener<XYSeriesModel>() {
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
