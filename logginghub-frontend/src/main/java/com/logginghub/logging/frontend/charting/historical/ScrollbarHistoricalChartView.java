package com.logginghub.logging.frontend.charting.historical;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Binder2;

public class ScrollbarHistoricalChartView extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(ScrollbarHistoricalChartView.class);

    private JScrollBar scrollBar;
    private Binder2 binder;

    public ScrollbarHistoricalChartView() {
        setLayout(new MigLayout("fill", "[grow,fill]", "[grow,fill]"));

        scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        add(scrollBar, "cell 0 0");
    }

    public void bind(HistoricalChartController controller, final HistoricalChartModel model) {

        long startTime = model.getStartTime().longValue();
        long yesterday = TimeUtils.before(startTime, "24 hours");

        logger.info("Scroll bar max '{}' scroll bar min '{}'", Logger.toLocalDateString(startTime), Logger.toLocalDateString(yesterday));

        int nowSeconds = (int) (startTime / 1000L);
        int yesterdaySeconds = (int) (yesterday / 1000L);

        scrollBar.setMaximum(nowSeconds);
        scrollBar.setMinimum(yesterdaySeconds);
        scrollBar.setValue(nowSeconds);
        scrollBar.setUnitIncrement((int) (TimeUtils.minutes(1)/1000L));

        binder = new Binder2();

        binder.addAdjustmentListener(scrollBar, new AdjustmentListener() {
            @Override public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int value = scrollBar.getValue();
                    long newStartTime = value * 1000L;
                    System.out.println(e);
                    logger.info("Setting time to '{}' [local]", Logger.toLocalDateString(newStartTime));

                    if (model.getStartTime().longValue() != newStartTime) {
                        model.getStartTime().set(newStartTime);
                    }
                }

            }
        });

    }

    public void unbind() {
        binder.unbind();
    }

}
