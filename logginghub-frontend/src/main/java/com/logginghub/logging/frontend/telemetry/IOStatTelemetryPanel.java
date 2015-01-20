package com.logginghub.logging.frontend.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.charting.XYTimeChartPanel;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;

public class IOStatTelemetryPanel extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(IOStatTelemetryPanel.class);

    private long chartDuration = TimeUnit.MINUTES.toMillis(2);
    private Map<String, DataStructure> updates = new HashMap<String, DataStructure>();
    private Timer timer = null;

    private static final long serialVersionUID = 1L;

    private XYTimeChartPanel xyReadsRequested;
    private XYTimeChartPanel xyReadsCompleted;

    private XYTimeChartPanel xyWritesRequested;
    private XYTimeChartPanel xyWritesCompleted;

    private XYTimeChartPanel xyReadAmount;
    private XYTimeChartPanel xyWriteAmount;

    private XYTimeChartPanel xyRequestSize;
    private XYTimeChartPanel xyRequestQueueLength;

    private XYTimeChartPanel xyReadRequstServedTime;
    private XYTimeChartPanel xyWriteRequestServedTime;

    private XYTimeChartPanel xyRequestServedTime;

    private XYTimeChartPanel xyUtilisation;

      /**
     * Create the panel.
     */
    public IOStatTelemetryPanel() {
        setBorder(new TitledBorder(null, "Machine Telemetry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("ins 0", "[grow,fill]0[grow,fill]0[grow,fill]0[grow,fill]", "[grow]0[grow]"));

        xyUtilisation = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Device utilisation").setYAxisLabel("%").toChart();
        xyRequestServedTime = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Request served time").setYAxisLabel("ms").toChart();

        xyReadAmount = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Blocks Read").setYAxisLabel("KBytes/sec").toChart();
        xyWriteAmount = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Blocks Written").setYAxisLabel("KBytes/sec").toChart();

        xyRequestSize = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Average request size").setYAxisLabel("Sectors").toChart();
        xyRequestQueueLength = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Request queue length").setYAxisLabel("Requests")
                .toChart();

        xyReadRequstServedTime = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Read request served time").setYAxisLabel("ms")
                .toChart();
        xyWriteRequestServedTime = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Write request served time").setYAxisLabel("ms")
                .toChart();

        xyWritesRequested = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Writes requested")
                .setYAxisLabel("Number of context switches").toChart();
        xyWritesCompleted = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Writes completed ").setYAxisLabel("Number of interupts")
                .toChart();

        xyReadsRequested = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Reads requested").setYAxisLabel("Read requests").toChart();
        xyReadsCompleted = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Reads completed").setYAxisLabel("Read requests").toChart();

        // First row

        JPanel a = new JPanel();
        a.add(xyUtilisation.getComponent());
        add(a, "cell 0 0,grow");
        a.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel b = new JPanel();
        b.add(xyRequestServedTime.getComponent());
        add(b, "cell 1 0,grow");
        b.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel f = new JPanel();
        f.add(xyRequestSize.getComponent());
        add(f, "cell 2 0,grow");
        f.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel g = new JPanel();
        g.add(xyRequestQueueLength.getComponent());
        add(g, "cell 3 0,grow");
        g.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        // Second row
        JPanel e = new JPanel();
        e.add(xyWriteAmount.getComponent());
        add(e, "cell 0 1,grow");
        e.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel h = new JPanel();
        h.add(xyWritesRequested.getComponent());
        add(h, "cell 1 1,grow");
        h.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel i = new JPanel();
        i.add(xyWritesCompleted.getComponent());
        add(i, "cell 2 1,grow");
        i.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel j = new JPanel();
        j.add(xyWriteRequestServedTime.getComponent());
        add(j, "cell 3 1,grow");
        j.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        // Third row
        JPanel d = new JPanel();
        d.add(xyReadAmount.getComponent());
        add(d, "cell 0 2,grow");
        d.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel l = new JPanel();
        l.add(xyReadsRequested.getComponent());
        add(l, "cell 1 2,grow");
        l.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel m = new JPanel();
        m.add(xyReadsCompleted.getComponent());
        add(m, "cell 2 2,grow");
        m.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel k = new JPanel();
        k.add(xyReadRequstServedTime.getComponent());
        add(k, "cell 3 2,grow");
        k.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        timer = TimerUtils.every("MachineTelemetryPanel-Updater", 500, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateInternal();
                    }
                });

            }
        });
    }

    public void update(DataStructure dataStructure) {
        synchronized (updates) {
            if (dataStructure.containsValue(Values.IOSTAT_Average_Request_Queue_Length)) {
                String key = dataStructure.getStringKey(Keys.host) + " - " + dataStructure.getStringKey(Keys.device);
                updates.put(key, dataStructure);
            }
        }
    }

    protected void updateInternal() {
        Map<String, DataStructure> currentUpdates;

        synchronized (updates) {
            currentUpdates = updates;
            updates = new HashMap<String, DataStructure>();
        }

        logger.debug("Processing machine telemetry {} updates : {}", currentUpdates.size(), currentUpdates.keySet());
        long now = System.currentTimeMillis();

        for (DataStructure data : currentUpdates.values()) {

            String key =
                    VMStatTelemetryPanel.shortenHost(data.getStringKey(Keys.host)) + " - " + data.getStringKey(Keys.device);

            xyReadsRequested.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Reads_Requested));
            xyReadsCompleted.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Reads_Completed));

            xyWritesRequested.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Writes_Requested));
            xyWritesCompleted.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Writes_Completed));

            xyReadAmount.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Read_Amount));
            xyWriteAmount.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Write_Amount));

            if (data.containsValue(Values.IOSTAT_Read_Requests_Served_Time)) {
                xyReadRequstServedTime.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Read_Requests_Served_Time));
            }

            if (data.containsValue(Values.IOSTAT_Write_Requests_Served_Time)) {
                xyWriteRequestServedTime.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Write_Requests_Served_Time));
            }

            xyRequestSize.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Average_Request_Size));
            xyRequestQueueLength.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Average_Request_Queue_Length));

            xyUtilisation.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Device_Utilisation));
            xyRequestServedTime.addValue(key, now, data.getDoubleValue(Values.IOSTAT_Request_Served_Time));

            // Deliberately missing IOSTAT_Service_Time
        }

        xyReadAmount.removeOldDataPoints(chartDuration);
        xyWriteAmount.removeOldDataPoints(chartDuration);
        xyRequestSize.removeOldDataPoints(chartDuration);
        xyRequestQueueLength.removeOldDataPoints(chartDuration);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                repaint();
            }
        });
    }

    public void setChartDuration(long chartDuration) {
        this.chartDuration = chartDuration;
    }
}
