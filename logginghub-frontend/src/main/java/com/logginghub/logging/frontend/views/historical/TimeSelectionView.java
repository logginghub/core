package com.logginghub.logging.frontend.views.historical;

import com.logginghub.logging.frontend.charting.model.TimeSelectionModel;
import com.logginghub.utils.SystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.observable.ObservablePropertyListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by james on 29/01/15.
 */
public class TimeSelectionView extends JPanel {

    private TimeField startTime;
    private TimeField endTime;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY hh:mm:ss.SSSS");
    private TimeSelectionModel timeSelectionModel;
    private WorkerThread workerThread;
    private TimeProvider timeProvider = new SystemTimeProvider();

    public TimeSelectionView() {
        setLayout(new MigLayout("fill", "[fill][fill,grow][fill][fill][fill,grow][fill]", "[fill, grow]"));

        startTime = new TimeField();
        endTime = new TimeField();

        JButton resetStartButton = new JButton("Reset");
        resetStartButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(timeSelectionModel != null) {
                    timeSelectionModel.getStartTime().getTime().set(0);
                    timeSelectionModel.getStartTime().getEdited().set(false);
                }
            }
        });

        JButton resetEndButton = new JButton("Reset");
        resetEndButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if(timeSelectionModel != null) {
                    timeSelectionModel.getEndTime().getTime().set(timeProvider.getTime());
                    timeSelectionModel.getEndTime().getEdited().set(false);
                }
            }
        });

        add(new JLabel("Start"));
        add(startTime);
        add(resetStartButton);
        add(new JLabel("End"));
        add(endTime);
        add(resetEndButton);
    }

    public void bind(TimeSelectionModel timeSelectionModel) {

        this.timeSelectionModel = timeSelectionModel;

        startTime.bind(timeSelectionModel.getStartTime());
        endTime.bind(timeSelectionModel.getEndTime());

        timeSelectionModel.getEndTime().getEdited().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
            @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    stopUpdateTimer();
                } else {
                    startUpdateTimer();
                }
            }
        });

    }

    private void stopUpdateTimer() {
        if (workerThread != null) {
            workerThread.stop();
        }
    }

    private void startUpdateTimer() {
        stopUpdateTimer();
        workerThread = WorkerThread.everySecond("TimeSelectionView-UpdateThread", new Runnable() {
            @Override public void run() {
                timeSelectionModel.getEndTime().getTime().set(TimeUtils.chunk(timeProvider.getTime(), 1000));
            }
        });

    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }
}
