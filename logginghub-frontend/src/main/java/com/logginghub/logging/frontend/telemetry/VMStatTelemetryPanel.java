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
import org.jfree.data.general.Dataset;

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

public class VMStatTelemetryPanel extends JPanel {

    private static final Logger logger = Logger.getLoggerFor(VMStatTelemetryPanel.class);

    private long chartDuration = TimeUnit.MINUTES.toMillis(2);
    private Map<String, DataStructure> updates = new HashMap<String, DataStructure>();
    private Timer timer = null;

    private static final long serialVersionUID = 1L;

    private ChartPanelInterface stackedBarCPU;
    private ChartPanelInterface stackedBarMemory;
    private ChartPanelInterface ioBarChart;

    private XYTimeChartPanel xyContextSwitches;
    private XYTimeChartPanel xyInterupts;

    private XYTimeChartPanel xyCPU;
    private XYTimeChartPanel xyMemory;

    private XYTimeChartPanel xyIORx;
    private XYTimeChartPanel xyIOTx;

    private XYTimeChartPanel xySwapIn;
    private XYTimeChartPanel xySwapOut;

    private XYTimeChartPanel xyRunQueue;
    private XYTimeChartPanel xyWaiting;

    private int movingAveragePoints = 5;
    private MovingAverageFactoryMap txMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);
    private MovingAverageFactoryMap rxMovingAverages = new MovingAverageFactoryMap(movingAveragePoints);


    /**
     * Create the panel.
     */
    public VMStatTelemetryPanel() {
        setBorder(new TitledBorder(null, "Machine Telemetry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new MigLayout("ins 0", "[grow,fill]0[grow,fill]0[grow,fill]0[grow,fill]", "[grow]0[grow]"));

        stackedBarCPU = ChartBuilder.startStackedBar().setTitle("CPU").setOrientation(PlotOrientation.HORIZONTAL)
                .setYAxisLabel("Percentage cpu").setYMaximum(100).setXAxisLabel("").setYAxisLabel("").toChart();
        SortedCategoryDataset dataset = (SortedCategoryDataset) stackedBarCPU.getDataset();
        dataset.setSortRows(false);
        dataset.setSortColumns(true);

        stackedBarMemory = ChartBuilder.startStackedBar().setTitle("Memory").setOrientation(PlotOrientation.HORIZONTAL)
                .setYAxisLabel("MBytes").toChart();
        ioBarChart = ChartBuilder.startBar().setTitle("IO").setYAxisLabel("KBytes/sec").setOrientation(PlotOrientation.HORIZONTAL)
                .yAxisLock(1024).setVerticalXAxisLabels(true).toChart();

        JPanel a = new JPanel();
        a.add(stackedBarCPU.getComponent());
        add(a, "cell 0 0,grow");
        a.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel b = new JPanel();
        b.add(stackedBarMemory.getComponent());
        add(b, "cell 1 0,grow");
        b.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        xyCPU = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("CPU").setYAxisLabel("Percentage cpu used").toChart();
        xyMemory = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Memory").setYAxisLabel("MBytes free").toChart();

        xyIORx = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Block IO in").setYAxisLabel("KBytes/sec").toChart();
        xyIOTx = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Block IO out").setYAxisLabel("KBytes/sec").toChart();

        xySwapIn = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Swap in").setYAxisLabel("Blocks/sec").toChart();
        xySwapOut = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Swap out").setYAxisLabel("Blocks/sec").toChart();

        xyInterupts = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Interupts").setYAxisLabel("Number of interupts").toChart();
        xyContextSwitches = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Context switches")
                .setYAxisLabel("Number of context switches").toChart();

        xyRunQueue = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Run queue").setYAxisLabel("Processes").toChart();
        xyWaiting = (XYTimeChartPanel) ChartBuilder.startXY().setTitle("Waiting processes").setYAxisLabel("Processes").toChart();

        JPanel d = new JPanel();
        d.add(xyCPU.getComponent());
        add(d, "cell 0 1,grow");
        d.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel e = new JPanel();
        e.add(xyMemory.getComponent());
        add(e, "cell 1 1,grow");
        e.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel f = new JPanel();
        f.add(xyIORx.getComponent());
        add(f, "cell 2 2,grow");
        f.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel g = new JPanel();
        g.add(xyIOTx.getComponent());
        add(g, "cell 3 2,grow");
        g.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel h = new JPanel();
        h.add(xyContextSwitches.getComponent());
        add(h, "cell 3 1,grow");
        h.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel i = new JPanel();
        i.add(xyInterupts.getComponent());
        add(i, "cell 2 1,grow");
        i.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel j = new JPanel();
        j.add(xySwapOut.getComponent());
        add(j, "cell 0 2,grow");
        j.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel k = new JPanel();
        k.add(xySwapIn.getComponent());
        add(k, "cell 1 2,grow");
        k.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel l = new JPanel();
        l.add(xyRunQueue.getComponent());
        add(l, "cell 2 0,grow");
        l.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

        JPanel m = new JPanel();
        m.add(xyWaiting.getComponent());
        add(m, "cell 3 0,grow");
        m.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));

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
            if (dataStructure.containsValue(Values.VMSTAT_CPU_Idle)) {
                updates.put(dataStructure.getStringKey(Keys.host), dataStructure);
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

            String key = data.getStringKey(Keys.host);
            key = shortenHost(key);

            if (data.containsValue(Values.VMSTAT_CPU_Idle)) {
                SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarCPU.getDataset();

                Integer sys = data.getIntValue(Values.VMSTAT_CPU_System);
                Integer usr = data.getIntValue(Values.VMSTAT_CPU_User);
                Integer waiting = data.getIntValue(Values.VMSTAT_CPU_Waiting);
                Integer idle = data.getIntValue(Values.VMSTAT_CPU_Idle);

                categorydataset.setValue(sys, "System", key);
                categorydataset.setValue(usr, "User", key);
                categorydataset.setValue(waiting, "Wait", key);
                categorydataset.setValue(idle, "Idle", key);

                xyCPU.addValue(key, now, 100 - idle);
            }

            xyContextSwitches.addValue(key, now, data.getIntValue(Values.VMSTAT_System_Context_Switches));
            xyInterupts.addValue(key, now, data.getIntValue(Values.VMSTAT_System_Interupts));

            xyRunQueue.addValue(key, now, data.getIntValue(Values.VMSTAT_Processes_Run_Queue));
            xyWaiting.addValue(key, now, data.getIntValue(Values.VMSTAT_Processes_Blocking));
            xySwapIn.addValue(key, now, data.getIntValue(Values.VMSTAT_Swap_In));
            xySwapOut.addValue(key, now, data.getIntValue(Values.VMSTAT_Swap_Out));

            if (data.containsValue(Values.VMSTAT_Memory_Free)) {
                SortedCategoryDataset categorydataset = (SortedCategoryDataset) stackedBarMemory.getDataset();

                double ramFree = data.getIntValue(Values.VMSTAT_Memory_Free) / 1024f;
                double buffers = data.getIntValue(Values.VMSTAT_Memory_Buffers) / 1024d;
                double cache = data.getIntValue(Values.VMSTAT_Memory_Cache) / 1024d;
                double swap = data.getIntValue(Values.VMSTAT_Memory_Swap) / 1024d;

                categorydataset.setValue(ramFree, "Free", key);
                categorydataset.setValue(buffers, "Buffers", key);
                categorydataset.setValue(cache, "Cache", key);
                categorydataset.setValue(swap, "Swap", key);

                xyMemory.addValue(key, now, ramFree);
            }

            if (data.containsValue(Values.VMSTAT_IO_Blocks_In)) {
                DefaultCategoryDataset categorydataset = (DefaultCategoryDataset) ioBarChart.getDataset();

                double tx = data.getIntValue(Values.VMSTAT_IO_Blocks_Out) / 1024f;
                double rx = data.getIntValue(Values.VMSTAT_IO_Blocks_In) / 1024f;

                categorydataset.setValue(tx, "Tx", key);
                categorydataset.setValue(rx, "Rx", key);

                ((BarChartPanel) ioBarChart).updateRange(tx);
                ((BarChartPanel) ioBarChart).updateRange(rx);

                MovingAverage txMovingAverage = txMovingAverages.get(key);
                MovingAverage rxMovingAverage = rxMovingAverages.get(key);

                txMovingAverage.addValue(tx);
                rxMovingAverage.addValue(rx);

                xyIOTx.addValue(key + ".tx", now, txMovingAverage.calculateMovingAverage());
                xyIORx.addValue(key + ".rx", now, rxMovingAverage.calculateMovingAverage());
            }
        }

        xyCPU.removeOldDataPoints(chartDuration);
        xyMemory.removeOldDataPoints(chartDuration);
        xyIORx.removeOldDataPoints(chartDuration);
        xyIOTx.removeOldDataPoints(chartDuration);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        });
    }

    public static String shortenHost(String key) {

        String shorter;
        int indexOf = key.indexOf(".");
        if (indexOf != -1) {
            shorter = key.substring(0, indexOf);
        } else {
            shorter = key;
        }

        return shorter;
    }

    public void setChartDuration(long chartDuration) {
        this.chartDuration = chartDuration;
    }
}
