package com.logginghub.logging.frontend.charting;

import java.awt.Color;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import com.logginghub.logging.frontend.analysis.ChartInterface;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.analysis.XYHistogramChart;
import com.logginghub.logging.frontend.analysis.XYScatterChart;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class LineChartView extends MigPanel {

    private static final Logger logger = Logger.getLoggerFor(LineChartView.class);
    private static final long serialVersionUID = 1L;
    private ChartInterface chart;

    public LineChartView() {
        super("fill", "[fill, grow]", "[fill, grow]");
        setBorder(BorderFactory.createLineBorder(Color.lightGray));
        setBackground(Color.white);
    }

    public void bind(final NewChartingController controller, LineChartModel chartModel) {

        String type = chartModel.getType().get();
        if (type != null && type.equals("histogram")) {
            XYHistogramChart xyhchart = new XYHistogramChart();

            // TODO: add this back in?
            // updatesEachSecond.add(xyhchart);
            chart = xyhchart;
        }
        else {
            chart = new XYScatterChart();
        }

        chartModel.getTitle().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override public void onPropertyChanged(String oldValue, String newValue) {
                chart.setTitle(newValue);
            }
        });

        String yAxisLabel = chartModel.getyAxisLabel().get();
        String xAxisLabel = chartModel.getxAxisLabel().get();
        double onlyShowValuesAbove = chartModel.getOnlyShowValuesAbove().get();
        boolean forceYZero = chartModel.getForceYZero().get();
        boolean showLegend = chartModel.getShowLegend().get();
        boolean sideLegend = chartModel.getSideLegend().get();

        if (chart instanceof XYHistogramChart) {
            // TODO : add histogram support back in, but do it in a separate
            // class
            // float minimumBucket = chartNode.getMinimumBucket();
            // float maximumBucket = chartNode.getMaximumBucket();
            // int granularity = chartNode.getGranularity();
            // long timeLimit = chartNode.getTimeLimit();
            // boolean realtimeUpdate = chartNode.isRealtimeUpdate();
            //
            // XYHistogramChart histogramChart = (XYHistogramChart) chart;
            // histogramChart.setMaximumBucket(maximumBucket);
            // histogramChart.setMinimumBucket(minimumBucket);
            // histogramChart.setGranularity(granularity);
            // histogramChart.setTimeLimit(timeLimit);
            // histogramChart.setRealtimeUpdate(realtimeUpdate);
        }

        final JFreeChart jFreeChart = chart.getChart();
        if (jFreeChart.getPlot() instanceof XYPlot) {
            final ValueAxis rangeAxis = jFreeChart.getXYPlot().getRangeAxis();

            chartModel.getOnlyShowValuesAbove().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
                @Override public void onPropertyChanged(Double oldValue, Double newValue) {
                    chart.setYMinimumFilter(newValue);
                }
            });

            chartModel.getWarningThreshold().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
                @Override public void onPropertyChanged(Double oldValue, Double newValue) {
                    chart.setWarningThreshold(newValue);
                }
            });

            chartModel.getSevereThreshold().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
                @Override public void onPropertyChanged(Double oldValue, Double newValue) {
                    chart.setSevereThreshold(newValue);
                }
            });

            chartModel.getyAxisLock().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
                @Override public void onPropertyChanged(Double oldValue, Double newValue) {
                    chart.setYAxisLock(newValue);
                }
            });

            chartModel.getForceYZero().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                    NumberAxis axis = (NumberAxis) rangeAxis;
                    axis.setAutoRangeIncludesZero(newValue);
                }
            });

            chartModel.getyAxisLabel().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                @Override public void onPropertyChanged(String oldValue, String newValue) {
                    rangeAxis.setLabel(newValue);
                }
            });

            chartModel.getxAxisLabel().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                @Override public void onPropertyChanged(String oldValue, String newValue) {
                    jFreeChart.getXYPlot().getDomainAxis().setLabel(newValue);
                }
            });

            if (chart instanceof XYScatterChart) {
                final XYScatterChart xyScatterChart = (XYScatterChart) chart;

                chartModel.getSmoothed().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                    @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            xyScatterChart.setSplineRenderer(true);
                        }
                        else {
                            xyScatterChart.setSplineRenderer(false);
                        }
                    }
                });
            }
        }

        if (jFreeChart.getLegend() != null) {

            chartModel.getShowLegend().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                    jFreeChart.getLegend().setVisible(newValue);
                }
            });

            chartModel.getSideLegend().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        jFreeChart.getLegend().setPosition(RectangleEdge.RIGHT);
                    }
                    else {
                        jFreeChart.getLegend().setPosition(RectangleEdge.BOTTOM);
                    }
                }
            });
        }

        chartModel.getResets().addListener(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                clearChartData();
            }
        });

        chartModel.getDataPoints().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                chart.setDatapoints(newValue);
            }
        });

        ObservableList<ChartSeriesModel> matcherModels = chartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyExisting(new ObservableListListener<ChartSeriesModel>() {

            StreamListener<ChunkedResult> listener = new StreamListener<ChunkedResult>() {
                @Override public void onNewItem(final ChunkedResult t) {
                    // TODO: consider batching
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            chart.onNewChunkedResult(t);
                        }
                    });

                }
            };

            @Override public void onAdded(ChartSeriesModel matcherModel) {

                // String matcherValue = matcherModel.getPattern().get();
                // SourceWildcardChunkedResultFilter filter = new
                // SourceWildcardChunkedResultFilter();
                // filter.setPattern(matcherValue);
                // chart.addFilter(filter);

                // String legend = matcherModel.getLegend().get();
                // if (legend != null) {
                // filter.setLegend(legend);
                // }

                Stream<ChunkedResult> stream = controller.getResultStreamFor(matcherModel);
                stream.addListener(listener);
            }

            @Override public void onRemoved(ChartSeriesModel t, int index) {
                logger.info("Unbinding chart series model '{}' from chart");
                Stream<ChunkedResult> stream = controller.getResultStreamFor(t);
                stream.removeListener(listener);
            }

            @Override public void onCleared() {}

        });

        add(chart.getComponent());

    }

    public ChartInterface getChart() {
        return chart;
    }

    public void clearChartData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                chart.clearChartData();
            }
        });
    }

    public void saveChartData(final File folder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                chart.saveChartData(folder);
            }
        });
    }

    public void saveChartImages(final File folder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                chart.saveChartImage(folder);
            }
        });
    }

    public void addMarker(final long currentTimeMillis, final String string) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chart.addMarker(currentTimeMillis, string);
            }
        });
    }

    // public void tickOver() {
    // SwingUtilities.invokeLater(new Runnable() {
    // @Override
    // public void run() {
    // chart.tickOver();
    // }
    // });
    //
    // }
}
