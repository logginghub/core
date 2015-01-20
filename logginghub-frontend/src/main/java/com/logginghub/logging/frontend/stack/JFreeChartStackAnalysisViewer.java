package com.logginghub.logging.frontend.stack;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jfree.data.category.DefaultCategoryDataset;

import com.logginghub.analytics.charting.BarChartPanel;
import com.logginghub.swingutils.MigPanel;
import com.logginghub.swingutils.stack.StackAnalyser;
import com.logginghub.swingutils.stack.ThreadData;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.MutableIntegerValue;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.swing.TestFrame;

public class JFreeChartStackAnalysisViewer extends StackAnalyser {

    int row = 1;
    int column = 0;
    // int columns = 5;

    private FactoryMap<String, BarChartPanel> individual = new FactoryMap<String, BarChartPanel>() {
        @Override protected BarChartPanel createEmptyValue(String key) {
            BarChartPanel individualPanel = new BarChartPanel();
            individualPanel.setTitle(key);

            String layout = StringUtils.format("cell {} {}", column, row);
            // column++;
            // if (column == columns) {
            row++;
            // column = 0;
            // }

            individualPanel.enableLongCategoryNames();

            internalPanel.add(individualPanel, layout);
            return individualPanel;
        }
    };
    private BarChartPanel barChartPanel = new BarChartPanel();
    private MigPanel internalPanel;

    // private IntegerFrequencyCount frequencyCount = new IntegerFrequencyCount();

    public JFreeChartStackAnalysisViewer() {
        super("fill", "[fill]", "[fill]");

        internalPanel = new MigPanel("fill", "[fill]", "[fill]");
        JScrollPane scroller = new JScrollPane(internalPanel);

        internalPanel.add(barChartPanel, "cell 0 0, span 5");
        barChartPanel.enableLongCategoryNames();
        barChartPanel.setTitle("Running threads");
        

        add(scroller, "cell 0 0");

        // barChartPanel.addSeries("Foo", frequencyCount);
    }

    @Override protected void process() {
        // super.process();
        final FactoryMap<String, ThreadData> data = getData();
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                synchronized (data) {
                    Set<Entry<String, ThreadData>> entrySet = data.entrySet();
                    for (Entry<String, ThreadData> entry : entrySet) {
                        ThreadData value = entry.getValue();
                        barChartPanel.getCategoryDataset().setValue(value.getRunning(), "Running", value.getKey());
                        barChartPanel.getCategoryDataset().setValue(value.getBlocked(), "Blocked", value.getKey());
                        barChartPanel.getCategoryDataset().setValue(value.getWaiting(), "Waiting", value.getKey());
                        barChartPanel.getCategoryDataset().setValue(value.getTimedWaiting(), "Timed Waiting", value.getKey());
                        updateIndividual(value);
                    }
                }
            }
        });
    }

    private void updateIndividual(ThreadData value) {
        final BarChartPanel individualPanel = individual.get(value.getKey());
        final List<MutableIntegerValue> sortedResults = value.getSortedResults();

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                DefaultCategoryDataset categoryDataset = individualPanel.getCategoryDataset();
                categoryDataset.clear();
                int count = 0;
                for (MutableIntegerValue mutableIntegerValue : sortedResults) {
                    categoryDataset.setValue(mutableIntegerValue.value, "In method", mutableIntegerValue.key == null ? "null"
                                                                                                                    : mutableIntegerValue.key);
                    count++;
                    if(count == 10){
                        break;
                    }
                }
            }
        });

    }
    
    public static void showAnalyser() {
        JFreeChartStackAnalysisViewer c = new JFreeChartStackAnalysisViewer();
        c.start(10, TimeUnit.MILLISECONDS);
        TestFrame.show(c);
    }
    

    public static void main(String[] args) {
        JFreeChartStackAnalysisViewer.showAnalyser();
    }
}
