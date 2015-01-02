package com.logginghub.utils;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.logginghub.utils.TimeUtils.TimeDetails;

public class VisualStopwatchController {

    private int maximumValues = 3 * 60;
    private static VisualStopwatchController instance = new VisualStopwatchController();

    public class SeriesWrapper {
        String name;

        long currentPeriodCount;
        long currentPeriodTotal;
        long currentPeriodMax;
        long currentPeriodMin;

        // ArrayList<Long> counts;
        // ArrayList<Double> averages;

        Histogram countHistogram;

        Histogram averageHistogram;

        JPanel panel;
        JLabel averageLabel;
        JLabel countLabel;

        MovingAverage averageMovingAverage;
        MovingAverage countMovingAverage;

        public void update() {

            if (countHistogram != null) {
                countHistogram.add(0, (int) currentPeriodCount);
                double average = currentPeriodTotal / (double) currentPeriodCount;
                averageHistogram.add(1, (int) average);

                averageMovingAverage.addValue(average);
                countMovingAverage.addValue(currentPeriodCount);
                
                averageHistogram.add(2, (int) currentPeriodMin);
                averageHistogram.add(0, (int) currentPeriodMax);
            }

            currentPeriodCount = 0;
            currentPeriodTotal = 0;
            currentPeriodMax = Long.MIN_VALUE;
            currentPeriodMin = Long.MAX_VALUE;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (countHistogram == null) {
                        createSwingElements();
                    }

                    countHistogram.repaint();
                                        
                    averageHistogram.repaint();

                    TimeDetails averageNice = TimeUtils.makeNice(averageMovingAverage.calculateMovingAverage());
                    String countNice = NumberFormat.getInstance().format(countMovingAverage.calculateMovingAverage());

                    averageLabel.setText(averageNice.toString());
                    countLabel.setText(countNice.toString() + " /sec");
                }
            });
        }

        protected void createSwingElements() {

            SeriesWrapper wrapper = this;
            wrapper.averageHistogram = new Histogram(3);
            wrapper.countHistogram = new Histogram(1);
            wrapper.averageLabel = new JLabel();
            wrapper.countLabel = new JLabel();
            wrapper.panel = new JPanel(new FlowLayout());
            
            wrapper.averageLabel.setPreferredSize(new Dimension(100, 20));
            wrapper.countLabel.setPreferredSize(new Dimension(100, 20));

            JPanel averagePanel = new JPanel(new FlowLayout());
            JPanel countPanel = new JPanel(new FlowLayout());

            averagePanel.add(wrapper.averageLabel, "wrap");
            averagePanel.add(wrapper.averageHistogram);
            countPanel.add(wrapper.countLabel, "wrap");
            countPanel.add(wrapper.countHistogram);

            averagePanel.setBorder(BorderFactory.createTitledBorder("Average"));
            countPanel.setBorder(BorderFactory.createTitledBorder("Count"));

            wrapper.panel.add(new JLabel(wrapper.name), "span 2, wrap");
            wrapper.panel.add(countPanel);
            wrapper.panel.add(averagePanel);

            mainPanel.add(wrapper.panel, "wrap");
        }

        public void addResult(long durationNanos) {
            currentPeriodCount++;
            currentPeriodTotal += durationNanos;
            currentPeriodMax = Math.max(currentPeriodMax, durationNanos);
            currentPeriodMin = Math.min(currentPeriodMin, durationNanos);
        }
    }

    private Map<String, SeriesWrapper> wrappers = new HashMap<String, VisualStopwatchController.SeriesWrapper>();

    private JPanel mainPanel;

    public void add(Stopwatch stopwatch) {
        String description = stopwatch.getDescription();

        SeriesWrapper seriesWrapper = wrappers.get(description);
        if (seriesWrapper == null) {
            seriesWrapper = createSeriesWrapper(description);
            wrappers.put(description, seriesWrapper);
        }

        seriesWrapper.addResult(stopwatch.getDurationNanos());        
    }

    private SeriesWrapper createSeriesWrapper(String description) {
        SeriesWrapper wrapper = new SeriesWrapper();

        wrapper.name = description;
        wrapper.currentPeriodCount = 0;
        wrapper.currentPeriodTotal = 0;
        wrapper.averageMovingAverage = new MovingAverage(2);
        wrapper.countMovingAverage = new MovingAverage(2);

        return wrapper;
    }

    public void show() {
        JFrame frame = new JFrame("Stopwatch Performance");

        mainPanel = new JPanel(new GridLayout(5, 5));
        frame.getContentPane().add(new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        TimerUtils.everySecond("VisualStopwatchController-secondTimerThreads", new Runnable() {
            public void run() {
                update();
            }
        });
    }

    protected void update() {

        Collection<SeriesWrapper> wrapperObjects = wrappers.values();
        for (SeriesWrapper seriesWrapper : wrapperObjects) {
            seriesWrapper.update();
        }
    }

    public static VisualStopwatchController getInstance() {
        return instance;
    }

}
