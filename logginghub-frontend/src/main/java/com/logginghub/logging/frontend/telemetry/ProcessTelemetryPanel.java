package com.logginghub.logging.frontend.telemetry;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.plot.PlotOrientation;

import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.charting.ChartPanelInterface;
import com.logginghub.analytics.charting.SortedCategoryDataset;
import com.logginghub.analytics.charting.XYTimeChartPanel;
import com.logginghub.utils.ExponentialMovingAverageFactoryMap;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.MovingAverageFactoryMap;
import com.logginghub.utils.TimerUtils;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Keys;
import com.logginghub.utils.data.DataStructure.Values;
import com.logginghub.utils.logging.Logger;

public class ProcessTelemetryPanel extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(ProcessTelemetryPanel.class);
    private long chartDuration = TimeUnit.MINUTES.toMillis(2);
    private Map<String, DataStructure> updates2 = new HashMap<String, DataStructure>();
    private Timer timer = null;

    private static final long serialVersionUID = 1L;

    private ChartPanelInterface stackedBarCPU;
    private ChartPanelInterface stackedBarMemory;

    private static final int movingAveragePoints = 10;

    private MovingAverageFactoryMap systemMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);
    private MovingAverageFactoryMap userMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);
    private ExponentialMovingAverageFactoryMap processCPUAverages = new ExponentialMovingAverageFactoryMap(movingAveragePoints);

    private XYTimeChartPanel xyCPU;
    private XYTimeChartPanel xyMemory;

    /**
     * Create the panel.
     */
    public ProcessTelemetryPanel() {
        setBorder(new TitledBorder(null, "Process Telemetry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("ins 0", "[grow]", "[grow]"));

        stackedBarCPU = ChartBuilder.startStackedBar()
                                    .setTitle("CPU")
                                    .setYAxisLabel("Percentage cpu")
                                    .setXAxisLabel("")
                                    .setYAxisLabel("")
                                    .setOrientation(PlotOrientation.HORIZONTAL)
                                    .setYMaximum(100)
                                    .toChart();
        stackedBarMemory = ChartBuilder.startStackedBar()
                                       .setTitle("Memory")
                                       .setYAxisLabel("MBytes")
                                       .setXAxisLabel("")
                                       .setOrientation(PlotOrientation.HORIZONTAL)
                                       .toChart();

        add(stackedBarCPU.getComponent(), "cell 0 0,grow");
        add(stackedBarMemory.getComponent(), "cell 1 0,grow");

        xyCPU = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("CPU").setYAxisLabel("Percentage cpu used").toChart();
        xyMemory = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Memory").setYAxisLabel("MBytes free").toChart();

        add(xyCPU.getComponent(), "cell 0 1,grow");
        add(xyMemory.getComponent(), "cell 1 1,grow");

        timer = TimerUtils.every("ProcessTelemetryPanel-Updater", 500, TimeUnit.MILLISECONDS, new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        updateInternal2();
                    }
                });

            }
        });
    }

    protected void updateCharts(TreePath[] checkedPaths) {

    }

    public void update(DataStructure data) {

        synchronized (updates2) {
            if (data.containsKey(Keys.processName)) {
                String processName = data.getKey(Keys.processName).asString();
                if (processName != null) {
                    String key = data.getKey(Keys.ip) + processName;
                    updates2.put(key, data);

                }
            }
        }
    }

    protected void updateInternal2() {

        Map<String, DataStructure> currentUpdates;

        synchronized (updates2) {
            currentUpdates = updates2;
            updates2 = new HashMap<String, DataStructure>();
        }

        for (DataStructure data : currentUpdates.values()) {

            if (data.containsKey(Keys.processName)) {
                String key = data.getKey(Keys.processName) + "@" + data.getKey(Keys.host);

                if (data.containsValue(Values.SIGAR_OS_Process_Cpu_System_Time)) {
                    SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarCPU.getDataset();

                    double processSystemTime = data.getDoubleValue(Values.SIGAR_OS_Process_Cpu_System_Time);
                    double processUserTime = data.getDoubleValue(Values.SIGAR_OS_Process_Cpu_User_Time);

                    MovingAverage userMovingAverage = userMovingAverages.get(key);
                    MovingAverage systemMovingAverage = systemMovingAverages.get(key);

                    userMovingAverage.addValue(processUserTime);
                    systemMovingAverages.get(key).addValue(processSystemTime);

                    categorydataset.setValue(systemMovingAverage.calculateMovingAverage(), "System", key);
                    categorydataset.setValue(userMovingAverage.calculateMovingAverage(), "User", key);

                    MovingAverage movingAverage = processCPUAverages.get(key);
                    movingAverage.addValue(data.getDoubleValue(Values.SIGAR_OS_Process_Cpu_Percentage) * 100f);

                    xyCPU.addValue(key, System.currentTimeMillis(), movingAverage.calculateMovingAverage());
                }

                if (data.containsValue(Values.JVM_Process_Memory_Maximum)) {
                    SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarMemory.getDataset();

                    double max = data.getDoubleValue(Values.JVM_Process_Memory_Maximum) / 1024f / 1024f;
                    double total = data.getDoubleValue(Values.JVM_Process_Memory_Total) / 1024f / 1024f;
                    double used = data.getDoubleValue(Values.JVM_Process_Memory_Used) / 1024f / 1024f;

                    // The whole bar adds up to max
                    // The first colour is used, which eats in to total
                    // The second colour is the free bit within total
                    // The last bit is the difference between total and max

                    categorydataset.setValue(used, "Used", key);
                    categorydataset.setValue(total - used, "Free", key);
                    categorydataset.setValue(max - total, "Spare", key);

                    double free = (total - used) + (max - total);

                    xyMemory.addValue(key, System.currentTimeMillis(), free);
                }
            }
        }

        xyCPU.removeOldDataPoints(chartDuration);
        xyMemory.removeOldDataPoints(chartDuration);

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
