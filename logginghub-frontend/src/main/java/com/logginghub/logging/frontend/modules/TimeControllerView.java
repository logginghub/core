package com.logginghub.logging.frontend.modules;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import com.logginghub.utils.Asynchronous;
import com.logginghub.utils.DateFormatFactory;
import com.logginghub.utils.OffsetableSystemTimeProvider;
import com.logginghub.utils.TimeProvider;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;

public class TimeControllerView extends JPanel implements Asynchronous {

    private JTextField currentTimeLocal;
    private JTextField localTimezone;
    private JTextField currentTimeUTC;
    private JPanel panel;
    private JButton back1Hour;
    private JButton back1Minute;
    private JButton back1Second;
    private JButton forward1Hour;
    private JButton forward1Minute;
    private JButton forward1Second;
    private WorkerThread timer;

    private TimeProvider timeProvider;

    public TimeControllerView() {

        setForeground(Color.WHITE);
        setLayout(new MigLayout("", "[grow][grow,fill]", "[][][][][grow]"));

        add(new JLabel("Current time (local)"), "cell 0 0");

        currentTimeLocal = new JTextField();
        currentTimeLocal.setEditable(false);
        add(currentTimeLocal, "cell 1 0");

        add(new JLabel("Local timezone"), "cell 0 1");

        localTimezone = new JTextField();
        localTimezone.setEditable(false);
        add(localTimezone, "cell 1 1");

        add(new JLabel("Current time (UTC)"), "cell 0 2");

        currentTimeUTC = new JTextField();
        currentTimeUTC.setEditable(false);
        add(currentTimeUTC, "cell 1 2");

        panel = new JPanel();
        add(panel, "cell 0 4 2 1,grow");
        panel.setLayout(new MigLayout("", "[grow,fill][grow,fill]", "[][][][]"));

        back1Hour = new JButton("Back 1 hour");
        panel.add(back1Hour, "cell 0 0");

        forward1Hour = new JButton("Forward 1 hour");
        panel.add(forward1Hour, "cell 1 0");

        back1Minute = new JButton("Back 1 minute");
        panel.add(back1Minute, "cell 0 1");

        forward1Minute = new JButton("Forward 1 minute");
        panel.add(forward1Minute, "cell 1 1");

        back1Second = new JButton("Back 1 second");
        panel.add(back1Second, "cell 0 2");

        forward1Second = new JButton("Forward 1 second");
        panel.add(forward1Second, "cell 1 2");

        back1Hour.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                back("1 hour");
            }
        });

        back1Minute.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                back("1 minute");
            }
        });

        back1Second.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                back("1 second");
            }
        });

        forward1Hour.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                forward("1 hour");
            }
        });

        forward1Minute.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                forward("1 minute");
            }
        });

        forward1Second.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                forward("1 second");
            }
        });

    }

    protected void back(String duration) {
        if (timeProvider instanceof OffsetableSystemTimeProvider) {
            OffsetableSystemTimeProvider offsetableSystemTimeProvider = (OffsetableSystemTimeProvider) timeProvider;
            offsetableSystemTimeProvider.incrementOffset(-TimeUtils.parseInterval(duration));
        }
        
        update();
    }

    protected void forward(String duration) {
        if (timeProvider instanceof OffsetableSystemTimeProvider) {
            OffsetableSystemTimeProvider offsetableSystemTimeProvider = (OffsetableSystemTimeProvider) timeProvider;
            offsetableSystemTimeProvider.incrementOffset(TimeUtils.parseInterval(duration));

        }
        
        update();
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                update();
            }
        });
    }

    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    @Override public void start() {
        stop();
        this.timer = WorkerThread.everyNowDaemon("LoggingHub-TimeControllerViewUpdate", 100, TimeUnit.MILLISECONDS, new Runnable() {
            @Override public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        update();
                    }
                });
            }
        });
    }

    private void update() {
        long time = timeProvider.getTime();

        TimeZone local = DateFormatFactory.local;
        localTimezone.setText(local.getDisplayName());

        currentTimeLocal.setText(DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.local).format(new Date(time)));
        currentTimeUTC.setText(DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.utc).format(new Date(time)));
    }

    @Override public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

}
