package com.logginghub.logging.frontend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.frontend.analysis.ChartProvider;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.analysis.ChunkedResultHandler;
import com.logginghub.logging.frontend.analysis.ComponentProvider;
import com.logginghub.logging.frontend.analysis.Page;
import com.logginghub.logging.frontend.analysis.ResultGenerator;
import com.logginghub.logging.frontend.analysis.XMLConfigurationLoader;
import com.logginghub.logging.frontend.configuration.ChartingConfiguration;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.listeners.LogEventListener;

public class OldChartingPanel extends JPanel implements LogEventListener {
    private static final long serialVersionUID = 1L;

    private List<LogEventListener> listeners;
    private List<ChartProvider> charts = new ArrayList<ChartProvider>();
    private List<ResultGenerator> generators;

    public OldChartingPanel(ChartingConfiguration configuration, String parsersFilename) {
        JTabbedPane tabbedPane = new JTabbedPane();
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        XMLConfigurationLoader loader = new XMLConfigurationLoader();

        final List<ChunkedResultHandler> handlers = new ArrayList<ChunkedResultHandler>();
        Collection<Page> values = loader.createPages(configuration, parsersFilename, handlers);
        for (Page page : values) {
            JPanel pagePanel = buildPagePanel(page);
            tabbedPane.addTab(page.getTitle(), pagePanel);
        }

        generators = new ArrayList<ResultGenerator>();

        listeners = loader.createEventListeners(configuration, parsersFilename, new ChunkedResultHandler() {
            public void onNewChunkedResult(ChunkedResult result) {
                for (ChunkedResultHandler handler : handlers) {
                    handler.onNewChunkedResult(result);
                }
            }

            public void complete() {

            }
        }, generators);
    }

    private JPanel buildPagePanel(Page page) {
        GridLayout gridLayout = new GridLayout(page.getRows(), page.getColumns());
        JPanel panel = new JPanel(gridLayout);

        List<ComponentProvider> charts = page.getCharts();
        for (ComponentProvider chartProvider : charts) {
            JComponent chart = chartProvider.getComponent();
            panel.add(chart);

            if (chartProvider instanceof ChartProvider) {
                this.charts.add((ChartProvider) chartProvider);
            }
        }

        return panel;
    }

    public void onNewLogEvent(LogEvent event) {
        if (listeners != null) {
            for (LogEventListener logEventListener : listeners) {
                try {
                    logEventListener.onNewLogEvent(event);
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

    }

    public void clearChartData() {
        for (ResultGenerator timeChunkingGenerator : generators) {
            timeChunkingGenerator.clear();
        }

        for (ChartProvider chartProvider : charts) {
            chartProvider.clearChartData();
        }
    }

    public void saveChartData() {
        File folder = new File("images/" + System.currentTimeMillis());
        folder.mkdirs();
        for (ChartProvider chartProvider : charts) {
            chartProvider.saveChartData(folder);
        }
    }

    public void saveChartImages() {
        File folder = new File("images/" + System.currentTimeMillis());
        folder.mkdirs();
        for (ChartProvider chartProvider : charts) {
            chartProvider.saveChartImage(folder);
        }

    }
}
