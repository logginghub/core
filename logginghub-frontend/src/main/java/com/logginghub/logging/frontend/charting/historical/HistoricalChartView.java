package com.logginghub.logging.frontend.charting.historical;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.logginghub.logging.frontend.analysis.XYScatterChart;
import com.logginghub.utils.Destination;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;
import com.logginghub.utils.observable.ObservablePropertyListener;

public class HistoricalChartView extends JPanel {

    private XYScatterChart chart = new XYScatterChart();
    private Binder2 binder;

    private static final Logger logger = Logger.getLoggerFor(HistoricalChartView.class);
    private HistoricalChartController controller;

    public HistoricalChartView() {
        setLayout(new BorderLayout());
        add(chart.getChartpanel(), BorderLayout.CENTER);
        
        chart.setYAxisLock(100);
    }

    public void bind(HistoricalChartController controller, final HistoricalChartModel model) {

        this.controller = controller;
        binder = new Binder2();
        binder.addListenerAndNotifyCurrent(model.getStartTime(), new ObservablePropertyListener<Long>() {
            @Override public void onPropertyChanged(Long oldValue, Long newValue) {
                updateData(model);
            }
        });
    }

    public void unbind() {
        if (binder != null) {
            binder.unbind();
        }
    }

    protected void updateData(HistoricalChartModel model) {
        logger.info("Updating chart to show new time '{}' duration '{}'",
                    Logger.toLocalDateString(model.getStartTime().longValue()),
                    TimeUtils.formatIntervalMillisecondsCompact(model.getDuration().longValue()));

        chart.clearChartData();
        controller.getChartData(model.getStartTime().longValue(), model.getDuration().longValue(), new Destination<TimeSeriesDataPoint>() {
            @Override public void send(TimeSeriesDataPoint t) {
                chart.addValue("series", t.getTime(), t.getValue());
            }
        });

    }

}
