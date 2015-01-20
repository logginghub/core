package com.logginghub.logging.frontend.charting;

import java.awt.Color;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;
import org.jfree.ui.RectangleEdge;

import com.logginghub.logging.frontend.analysis.ChartInterface;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.analysis.PieChart;
import com.logginghub.logging.frontend.charting.model.ChartSeriesFilterModel;
import com.logginghub.logging.frontend.charting.model.ChartSeriesModel;
import com.logginghub.logging.frontend.charting.model.PieChartModel;
import com.logginghub.logging.frontend.charting.model.Stream;
import com.logginghub.logging.frontend.charting.model.StreamListener;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class PieChartView extends MigPanel {

    private static final Logger logger = Logger.getLoggerFor(PieChartView.class);
    private static final long serialVersionUID = 1L;
    private PieChart chart;

    public PieChartView() {
        super("fill", "[fill, grow]", "[fill, grow]");
        setBorder(BorderFactory.createLineBorder(Color.lightGray));
        setBackground(Color.white);
    }

    public void bind(final NewChartingController controller, PieChartModel chartModel) {

//        String type = chartModel.getType().get();
//        if (type != null && type.equals("histogram")) {
//            XYHistogramChart xyhchart = new XYHistogramChart();
//            chart = xyhchart;
//        } else {
//            chart = new XYScatterChart();
//        }
        
        chart = new PieChart();

        chartModel.getTitle().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
            @Override
            public void onPropertyChanged(String oldValue, String newValue) {
                chart.setTitle(newValue);
            }
        });

        String yAxisLabel = chartModel.getyAxisLabel().get();
        String xAxisLabel = chartModel.getxAxisLabel().get();
        double onlyShowValuesAbove = chartModel.getOnlyShowValuesAbove().get();
        boolean forceYZero = chartModel.getForceYZero().get();
        boolean showLegend = chartModel.getShowLegend().get();
        boolean sideLegend = chartModel.getSideLegend().get();

        chartModel.getTop().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                chart.setTopXResults(newValue);
            }
        });
        
        chartModel.getShowOtherSeries().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                chart.setShowOther(newValue);
            }
        });
        
        chartModel.getResets().addListener(new ObservablePropertyListener<Integer>() {
            @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
                clearChartData();
            }
        });
        
//        final JFreeChart jFreeChart = chart.getChart();
//        if (jFreeChart.getPlot() instanceof XYPlot) {
//            final ValueAxis rangeAxis = jFreeChart.getXYPlot().getRangeAxis();
//
//            chartModel.getOnlyShowValuesAbove().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
//                @Override
//                public void onPropertyChanged(Double oldValue, Double newValue) {
//                    chart.setYMinimumFilter(newValue);
//                }
//            });
//
//            chartModel.getWarningThreshold().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
//                @Override
//                public void onPropertyChanged(Double oldValue, Double newValue) {
//                    chart.setWarningThreshold(newValue);
//                }
//            });
//
//            chartModel.getSevereThreshold().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
//                @Override
//                public void onPropertyChanged(Double oldValue, Double newValue) {
//                    chart.setSevereThreshold(newValue);
//                }
//            });
//
//            chartModel.getyAxisLock().addListenerAndNotifyCurrent(new ObservablePropertyListener<Double>() {
//                @Override
//                public void onPropertyChanged(Double oldValue, Double newValue) {
//                    chart.setYAxisLock(newValue);
//                }
//            });
//
//            chartModel.getForceYZero().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
//                @Override
//                public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
//                    NumberAxis axis = (NumberAxis) rangeAxis;
//                    axis.setAutoRangeIncludesZero(newValue);
//                }
//            });
//
//            chartModel.getyAxisLabel().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
//                @Override
//                public void onPropertyChanged(String oldValue, String newValue) {
//                    rangeAxis.setLabel(newValue);
//                }
//            });
//
//            chartModel.getxAxisLabel().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
//                @Override
//                public void onPropertyChanged(String oldValue, String newValue) {
//                    jFreeChart.getXYPlot().getDomainAxis().setLabel(newValue);
//                }
//            });
//        }
//
        
        final JFreeChart jfreeChart = chart.getChart();
        if (jfreeChart.getLegend() != null) {

            chartModel.getShowLegend().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                @Override
                public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                    jfreeChart.getLegend().setVisible(newValue);
                }
            });

            chartModel.getSideLegend().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
                @Override
                public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        jfreeChart.getLegend().setPosition(RectangleEdge.RIGHT);
                    } else {
                        jfreeChart.getLegend().setPosition(RectangleEdge.BOTTOM);
                    }
                }
            });
        }

        chartModel.getDataPoints().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>() {
            @Override
            public void onPropertyChanged(Integer oldValue, Integer newValue) {
                chart.setDatapoints(newValue);
            }
        });

        ObservableList<ChartSeriesModel> matcherModels = chartModel.getMatcherModels();
        matcherModels.addListenerAndNotifyExisting(new ObservableListListener<ChartSeriesModel>() {

            StreamListener<ChunkedResult> listener = new StreamListener<ChunkedResult>() {
                @Override
                public void onNewItem(final ChunkedResult t) {
                    // TODO: consider batching
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            chart.onNewChunkedResult(t);
                        }
                    });

                }
            };

            @Override
            public void onAdded(ChartSeriesModel matcherModel) {
                Stream<ChunkedResult> stream = controller.getResultStreamFor(matcherModel);
                stream.addListener(listener);
                
                matcherModel.getFilters().addListener(new ObservableListListener<ChartSeriesFilterModel>() {
                    @Override public void onRemoved(ChartSeriesFilterModel t) {
                        clearChartData();
                    }
                    @Override public void onCleared() {
                        clearChartData();
                    }
                    @Override public void onAdded(ChartSeriesFilterModel t) {
                        clearChartData();
                    }
                });
                
            }

            @Override
            public void onRemoved(ChartSeriesModel t) {
                logger.info("Unbinding chart series model '{}' from chart", t);
                Stream<ChunkedResult> stream = controller.getResultStreamFor(t);
                stream.removeListener(listener);
                clearChartData();
            }

            @Override
            public void onCleared() {
            }

        });

        add(chart.getComponent());

    }

    public ChartInterface getChart() {
        return chart;
    }

    public void clearChartData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chart.clearChartData();
            }
        });
    }

    public void saveChartData(final File folder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chart.saveChartData(folder);
            }
        });
    }

    public void saveChartImages(final File folder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
}
