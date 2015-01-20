package com.logginghub.logging.frontend.views.detail.time;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.logginghub.swingutils.MigPanel;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.TimerUtils;

public class TimeView extends MigPanel {

    private TimeViewComponent timeViewComponent = new TimeViewComponent();

    private JLabel intervalLabel = new JLabel("1 sec");

    private String[] labels = new String[] { "1 sec", "10 sec", "30 sec", "1 min", "10 min", "30 min", "1 hour", "6 hour", "12 hour", "1 day" };
    private long[] periods = new long[] { TimeUtils.seconds(1),
                                         TimeUtils.seconds(10),
                                         TimeUtils.seconds(30),
                                         TimeUtils.minutes(1),
                                         TimeUtils.minutes(10),
                                         TimeUtils.minutes(30),
                                         TimeUtils.hours(1),
                                         TimeUtils.hours(6),
                                         TimeUtils.hours(12),
                                         TimeUtils.days(1), };
    private int currentLevel = 0;

    private TimeController timeController;
    private TimeProvider timeProvider = new SystemTimeProvider();

    public TimeView() {
        super("fill", "[fill]", "[fill]");
        setName("TimeView");
        TimerUtils.every("TimeRepainter", 50, TimeUnit.MILLISECONDS, new Runnable() {

            @Override public void run() {

                if (timeController != null) {
                    timeController.updateCurrentTime(timeProvider.getTime());
                }
                timeViewComponent.repaint();

                // if (timeViewComponent.isDirty()) {
                // Debug.out("Dirrrty");
                // }else{
                // Debug.out("Not Dirrrty");
                // }
            }
        });
        setLayout(new MigLayout("", "[grow,fill][40px:40px:40px]", "[grow,fill]"));

        add(timeViewComponent, "cell 0 0,alignx left,aligny top");
        add(intervalLabel, "cell 1 0,alignx left,aligny center");

        intervalLabel.setName("TimeView.intervalLabel");

        timeViewComponent.addMouseWheelListener(new MouseWheelListener() {
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                int rotation = e.getWheelRotation();
                changeLevel(rotation);
            }

        });

        intervalLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getButton() == MouseEvent.BUTTON1) {
                    changeLevel(-1);
                }
                else if (e.getButton() == MouseEvent.BUTTON3) {
                    changeLevel(1);
                }

            }
        });

    }

    public void bind(TimeController timeController) {
        this.timeController = timeController;
        timeViewComponent.bind(timeController);
    }

    public void unbind() {
        timeViewComponent.unbind();
    }

    public TimeViewComponent getTimeViewComponent() {
        return timeViewComponent;
    }

    private void changeLevel(int rotation) {
        currentLevel += -rotation;
        currentLevel = Math.max(currentLevel, 0);
        currentLevel = Math.min(currentLevel, labels.length - 1);

        intervalLabel.setText(labels[currentLevel]);

        timeController.setPeriod(periods[currentLevel]);
    }

    public long getViewEndTime() {
        return timeViewComponent.getViewEndTime();
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }
}
