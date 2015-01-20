package com.logginghub.logging.frontend.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.charting.BarChartPanel;
import com.logginghub.analytics.charting.ChartPanelInterface;
import com.logginghub.analytics.charting.SortedCategoryDataset;
import com.logginghub.analytics.charting.XYTimeChartPanel;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.MovingAverageFactoryMap;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;

public class MachineTelemetryPanel extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(MachineTelemetryPanel.class);

    private long chartDuration = TimeUnit.MINUTES.toMillis(2);
    private Map<String, DataStructure> updates2 = new HashMap<String, DataStructure>();
    private Timer timer = null;

    private static final long serialVersionUID = 1L;

    private ChartPanelInterface stackedBarCPU;
    private ChartPanelInterface stackedBarMemory;
    private ChartPanelInterface ioBarChart;

    private XYTimeChartPanel xyCPU;
    private XYTimeChartPanel xyMemory;

    private XYTimeChartPanel xyIORx;
    private XYTimeChartPanel xyIOTx;

    private int movingAveragePoints = 5;
    private MovingAverageFactoryMap txMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);
    private MovingAverageFactoryMap rxMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);

    // private JPanel chartPanel;

    /**
     * Create the panel.
     */
    public MachineTelemetryPanel() {
        setBorder(new TitledBorder(null, "Machine Telemetry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("ins 0", "[grow,fill]0[grow,fill]0[grow,fill]0[grow,fill]", "[grow]0[grow]"));

        stackedBarCPU = ChartBuilder.startStackedBar()
                                    .setTitle("CPU")
                                    .setOrientation(PlotOrientation.HORIZONTAL)
                                    .setYAxisLabel("Percentage cpu")
                                    .setYMaximum(100)
                                    .setXAxisLabel("")
                                    .setYAxisLabel("")
                                    .toChart();
        stackedBarMemory = ChartBuilder.startStackedBar()
                                       .setTitle("Memory")
                                       .setOrientation(PlotOrientation.HORIZONTAL)
                                       .setYAxisLabel("MBytes")
                                       .toChart();
        ioBarChart = ChartBuilder.startBar()
                                 .setTitle("IO")
                                 .setYAxisLabel("KBytes/sec")
                                 .setOrientation(PlotOrientation.HORIZONTAL)
                                 .yAxisLock(1024)
                                 .setVerticalXAxisLabels(true)
                                 .toChart();

        // add(stackedBarCPU.getComponent(), "cell 0 0,grow");
        JPanel a = new JPanel();
        a.add(stackedBarCPU.getComponent());
        add(a, "cell 0 0,grow");
        a.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        // add(stackedBarMemory.getComponent(), "cell 1 0,grow");
        JPanel b = new JPanel();
        b.add(stackedBarMemory.getComponent());
        add(b, "cell 1 0,grow");
        b.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        // add(stackedBarIO.getComponent(), "cell 2 0,grow, wrap");
        JPanel c = new JPanel();
        c.add(ioBarChart.getComponent());
        add(c, "cell 2 0 2 1,grow");
        c.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        xyCPU = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("CPU").setYAxisLabel("Percentage cpu used").toChart();
        xyMemory = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Memory").setYAxisLabel("MBytes free").toChart();
        xyIORx = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Network IO").setYAxisLabel("KBytes/sec").toChart();
        xyIOTx = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Network IO").setYAxisLabel("KBytes/sec").toChart();

        // add(xyCPU.getComponent(), "cell 0 1,grow");
        JPanel d = new JPanel();
        d.add(xyCPU.getComponent());
        add(d, "cell 0 1,grow");
        d.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        // add(xyMemory.getComponent(), "cell 1 1,grow");
        JPanel e = new JPanel();
        e.add(xyMemory.getComponent());
        add(e, "cell 1 1,grow");
        e.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel f = new JPanel();
        f.add(xyIORx.getComponent());
        add(f, "cell 2 1,grow");
        f.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel g = new JPanel();
        g.add(xyIOTx.getComponent());
        add(g, "cell 3 1,grow");
        g.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        timer = TimerUtils.every("MachineTelemetryPanel-Updater", 500, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        updateInternal2();
                    }
                });

            }
        });
    }

    public void update(DataStructure data) {
        synchronized (updates2) {
            if (data.containsValue(Values.SIGAR_OS_Network_Bytes_Received) ||
                data.containsValue(Values.SIGAR_OS_Cpu_System_Time) ||
                data.containsValue(Values.SIGAR_OS_Memory_Ram)) {
                updates2.put(data.getKey(Keys.host).asString(), data);
            }
        }
    }

    protected void updateInternal2() {
        Map<String, DataStructure> currentUpdates;

        synchronized (updates2) {
            currentUpdates = updates2;
            updates2 = new HashMap<String, DataStructure>();
        }

        logger.debug("Processing machine telemetry {} updates : {}", currentUpdates.size(), currentUpdates.keySet());

        for (DataStructure data : currentUpdates.values()) {

            String key = data.getKey(Keys.host).asString();
            if (data.containsValue(Values.SIGAR_OS_Cpu_System_Time)) {
                SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarCPU.getDataset();
                categorydataset.setValue(data.getDoubleValue(Values.SIGAR_OS_Cpu_System_Time) * 100, "System", key);
                categorydataset.setValue(data.getDoubleValue(Values.SIGAR_OS_Cpu_User_Time) * 100, "User", key);
                categorydataset.setValue(data.getDoubleValue(Values.SIGAR_OS_Cpu_Wait_Time) * 100, "Wait", key);
                categorydataset.setValue(data.getDoubleValue(Values.SIGAR_OS_Cpu_Idle_Time) * 100, "Idle", key);

                xyCPU.addValue(key, System.currentTimeMillis(), 100 - data.getDoubleValue(Values.SIGAR_OS_Cpu_Idle_Time) * 100);
            }

            if (data.containsValue(Values.SIGAR_OS_Memory_Ram)) {
                SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarMemory.getDataset();

                double ramUsed = data.getDoubleValue(Values.SIGAR_OS_Memory_Used) / 1024f / 1024f;
                double ramFree = data.getDoubleValue(Values.SIGAR_OS_Memory_Free) / 1024f / 1024f;

                categorydataset.setValue(ramUsed, "Used", key);
                categorydataset.setValue(ramFree, "Free", key);

                xyMemory.addValue(key, System.currentTimeMillis(), ramFree);
            }

            if (data.containsValue(Values.SIGAR_OS_Network_Bytes_Received)) {
                DefaultCategoryDataset categorydataset = (DefaultCategoryDataset) ioBarChart.getDataset();

                double tx = data.getDoubleValue(Values.SIGAR_OS_Network_Bytes_Sent) / 1024f;
                double rx = data.getDoubleValue(Values.SIGAR_OS_Network_Bytes_Received) / 1024f;

                categorydataset.setValue(tx, "Tx", key);
                categorydataset.setValue(rx, "Rx", key);

                ((BarChartPanel) ioBarChart).updateRange(tx);
                ((BarChartPanel) ioBarChart).updateRange(rx);

                MovingAverage txMovingAverage = txMovingAverages.get(key);
                MovingAverage rxMovingAverage = rxMovingAverages.get(key);

                txMovingAverage.addValue(tx);
                rxMovingAverage.addValue(rx);

                xyIOTx.addValue(key + ".tx", System.currentTimeMillis(), txMovingAverage.calculateMovingAverage());
                xyIORx.addValue(key + ".rx", System.currentTimeMillis(), rxMovingAverage.calculateMovingAverage());
            }
        }

        xyCPU.removeOldDataPoints(chartDuration);
        xyMemory.removeOldDataPoints(chartDuration);
        xyIORx.removeOldDataPoints(chartDuration);
        xyIOTx.removeOldDataPoints(chartDuration);

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                repaint();
            }
        });
    }
    
    public void setChartDuration(long chartDuration) {
        this.chartDuration = chartDuration;
    }

}
