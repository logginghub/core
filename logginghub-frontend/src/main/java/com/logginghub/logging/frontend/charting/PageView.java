package com.logginghub.logging.frontend.charting;

import com.logginghub.logging.frontend.charting.model.LineChartModel;
import com.logginghub.logging.frontend.charting.model.PageModel;
import com.logginghub.logging.frontend.charting.model.PieChartModel;
import com.logginghub.logging.frontend.charting.model.TableChartModel;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PageView extends JPanel {

    private static final long serialVersionUID = 1L;
    private NewChartingController controller;
    private PageModel model;
    private MigLayout layout;
    // private List<LineChartModelView> charts = new CopyOnWriteArrayList<LineChartModelView>();

    private Map<LineChartModel, LineChartView> lineChartCounterparts = new HashMap<LineChartModel, LineChartView>();
    private Map<PieChartModel, PieChartView> pieChartCounterparts = new HashMap<PieChartModel, PieChartView>();
    private Map<TableChartModel, TableChartView> tableChartCounterparts = new HashMap<TableChartModel, TableChartView>();

    public PageView() {
        layout = new MigLayout("fill", "[fill, grow]", "[fill, grow]");
        setLayout(layout);
        setBackground(Color.white);
    }

    public void bind(final NewChartingController controller, PageModel pageModel) {
        this.controller = controller;
        this.model = pageModel;

        pageModel.getChartingModels().addListenerAndNotifyCurrent(new ObservableListListener<LineChartModel>() {

            @Override public void onAdded(LineChartModel t) {
                final LineChartView chart = buildChart(t);
                lineChartCounterparts.put(t, chart);

                add(chart, t.getLayout().get());

                t.getLayout().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        try {
                            layout.setComponentConstraints(chart, newValue);
                            revalidate();
                            doLayout();
                        } catch (IllegalArgumentException e) {
                            // Might be malformed as its being typed
                        }

                    }
                });

                // TODO : invoke later?
                revalidate();
                doLayout();
            }

            @Override public void onRemoved(LineChartModel t, int index) {
                LineChartView chart = lineChartCounterparts.remove(t);
                // TODO : invoke later?
                remove(chart);
                revalidate();
                doLayout();
            }

            @Override public void onCleared() {}

        });

        pageModel.getPieChartModels().addListenerAndNotifyCurrent(new ObservableListListener<PieChartModel>() {

            @Override public void onAdded(PieChartModel t) {
                final PieChartView chart = buildChart(t);
                pieChartCounterparts.put(t, chart);

                add(chart, t.getLayout().get());

                t.getLayout().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        try {
                            layout.setComponentConstraints(chart, newValue);
                            revalidate();
                            doLayout();
                        } catch (IllegalArgumentException e) {
                            // Might be malformed as its being typed
                        }

                    }
                });

                // TODO : invoke later?
                revalidate();
                doLayout();
            }

            @Override public void onRemoved(PieChartModel t, int index) {
                PieChartView chart = pieChartCounterparts.remove(t);
                // TODO : invoke later?
                remove(chart);
                revalidate();
                doLayout();
            }

            @Override public void onCleared() {}

        });

        pageModel.getTableChartModels().addListenerAndNotifyCurrent(new ObservableListListener<TableChartModel>() {

            @Override public void onAdded(TableChartModel t) {
                final TableChartView chart = buildChart(t);
                tableChartCounterparts.put(t, chart);

                add(chart, t.getLayout().get());

                t.getLayout().addListenerAndNotifyCurrent(new ObservablePropertyListener<String>() {
                    @Override public void onPropertyChanged(String oldValue, String newValue) {
                        try {
                            layout.setComponentConstraints(chart, newValue);
                            revalidate();
                            doLayout();
                        } catch (IllegalArgumentException e) {
                            // Might be malformed as its being typed
                        }

                    }
                });

                // TODO : invoke later?
                revalidate();
                doLayout();
            }

            @Override public void onRemoved(TableChartModel t, int index) {
                TableChartView chart = tableChartCounterparts.remove(t);
                // TODO : invoke later?
                remove(chart);
                revalidate();
                doLayout();
            }

            @Override public void onCleared() {}

        });
    }

    protected LineChartView buildChart(LineChartModel chartModel) {
        LineChartView chartPanel = new LineChartView();
        chartPanel.bind(controller, chartModel);
        return chartPanel;
    }

    protected TableChartView buildChart(TableChartModel chartModel) {
        TableChartView chartPanel = new TableChartView();
        chartPanel.bind(controller, chartModel);
        return chartPanel;
    }

    protected PieChartView buildChart(PieChartModel chartModel) {
        PieChartView chartPanel = new PieChartView();
        chartPanel.bind(controller, chartModel);
        return chartPanel;
    }

    public Collection<LineChartView> getChartViews() {
        return lineChartCounterparts.values();
    }
    
    public Collection<PieChartView> getPieChartViews() {
        return pieChartCounterparts.values();
    }

}
