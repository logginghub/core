package com.logginghub.logging.frontend.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import com.logginghub.logging.frontend.configuration.ChartConfiguration;
import com.logginghub.logging.frontend.configuration.ChartingConfiguration;
import com.logginghub.logging.frontend.configuration.ChunkerConfiguration;
import com.logginghub.logging.frontend.configuration.MatcherConfiguration;
import com.logginghub.logging.frontend.configuration.PageConfiguration;
import com.logginghub.logging.frontend.configuration.ParserConfiguration;
import com.logginghub.logging.frontend.configuration.PatternConfiguration;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Tracer;
import com.logginghub.utils.logging.Logger;

public class XMLConfigurationLoader {

    private static final Logger logger = Logger.getLoggerFor(XMLConfigurationLoader.class);

    public List<LogEventListener> createEventListeners(ChartingConfiguration configuration, String parsersFilename, final ChunkedResultHandler handler, List<ResultGenerator> generators) {
        List<LogEventListener> listeners = new ArrayList<LogEventListener>();

        List<ChunkerConfiguration> chunkingParsers = configuration.getParserConfiguration().getChunkingParsers();
        processChunkerNodes(handler, generators, listeners, chunkingParsers);

        List<ParserConfiguration> rawParsers = configuration.getParserConfiguration().getRawParsers();
        processRawNodes(handler, generators, listeners, rawParsers);

        return listeners;
    }

    public Collection<Page> createPages(ChartingConfiguration configuration, String parsersFilename, List<ChunkedResultHandler> handlers) {
        final Collection<UpdatesEachSecond> updatesEachSecond = new ArrayList<UpdatesEachSecond>();
        Collection<Page> pages = new ArrayList<Page>();

        List<PageConfiguration> pagesNodes = configuration.getPages();
        for (PageConfiguration pageNode : pagesNodes) {
            String title = pageNode.getTitle();

            Page page = new Page();
            page.setColumns(pageNode.getColumns());
            page.setRows(pageNode.getRows());
            page.setTitle(title);
            pages.add(page);

            List<ChartConfiguration> charts = pageNode.getCharts();
            // List<SimpleXMLNode> charts = pageNode.getChildren("chart");
            for (ChartConfiguration chartConfiguration : charts) {
                ChartInterface chart = buildChart(updatesEachSecond, chartConfiguration);
                page.getCharts().add(chart);
                handlers.add(chart);
            }
        }

        startChartUpdateTimer(updatesEachSecond);

        return pages;
    }

    private ChartInterface buildChart(final Collection<UpdatesEachSecond> updatesEachSecond, ChartConfiguration chartNode) {
        String chartTitle = chartNode.getTitle();

        ChartInterface chart;
        String type = chartNode.getType();
        if (type != null && type.equals("histogram")) {
            XYHistogramChart xyhchart = new XYHistogramChart();
            updatesEachSecond.add(xyhchart);
            chart = xyhchart;
        }
        else {
            chart = new XYScatterChart();
        }
        chart.setTitle(chartTitle);
        String yAxisLabel = chartNode.getyLabel();
        String xAxisLabel = chartNode.getxLabel();
        String onlyShowValuesAbove = chartNode.getOnlyShowValuesAbove();
        boolean forceYZero = chartNode.isForceYZero();
        boolean showLegend = chartNode.getShowLegend();
        boolean sideLegend = chartNode.isSideLegend();

        if (chart instanceof XYHistogramChart) {
            float minimumBucket = chartNode.getMinimumBucket();
            float maximumBucket = chartNode.getMaximumBucket();
            int granularity = chartNode.getGranularity();
            long timeLimit = chartNode.getTimeLimit();
            boolean realtimeUpdate = chartNode.isRealtimeUpdate();

            XYHistogramChart histogramChart = (XYHistogramChart) chart;
            histogramChart.setMaximumBucket(maximumBucket);
            histogramChart.setMinimumBucket(minimumBucket);
            histogramChart.setGranularity(granularity);
            histogramChart.setTimeLimit(timeLimit);
            histogramChart.setRealtimeUpdate(realtimeUpdate);
        }

        JFreeChart jFreeChart = chart.getChart();
        if (jFreeChart.getPlot() instanceof XYPlot) {
            ValueAxis rangeAxis = jFreeChart.getXYPlot().getRangeAxis();

            if (onlyShowValuesAbove != null) {
                float yMinimumFilter = Float.parseFloat(onlyShowValuesAbove);
                chart.setYMinimumFilter(yMinimumFilter);
            }

            if (chartNode.getWarningThreshold() != Double.NaN) {
                chart.setWarningThreshold(chartNode.getWarningThreshold());
            }

            if (chartNode.getSevereThreshold() != Double.NaN) {
                chart.setSevereThreshold(chartNode.getSevereThreshold());
            }

            if (chartNode.getyAxisLock() != Double.NaN) {
                chart.setYAxisLock(chartNode.getyAxisLock());
            }

            if (forceYZero) {
                NumberAxis axis = (NumberAxis) rangeAxis;
                axis.setAutoRangeIncludesZero(true);
            }

            if (yAxisLabel != null) {
                rangeAxis.setLabel(yAxisLabel);
            }
            else {
                rangeAxis.setLabel("");
            }

            if (xAxisLabel != null) {
                jFreeChart.getXYPlot().getDomainAxis().setLabel(xAxisLabel);
            }
            else {
                jFreeChart.getXYPlot().getDomainAxis().setLabel("");
            }
        }

        if (jFreeChart.getLegend() != null) {
            jFreeChart.getLegend().setVisible(showLegend);

            if (sideLegend) {
                jFreeChart.getLegend().setPosition(RectangleEdge.RIGHT);
            }
        }

        chart.setDatapoints(chartNode.getDataPoints());

        List<MatcherConfiguration> matcherNodes = chartNode.getMatchers();
        for (MatcherConfiguration matcherNode : matcherNodes) {
            String matcherValue = matcherNode.getValue();
            SourceWildcardChunkedResultFilter filter = new SourceWildcardChunkedResultFilter();
            filter.setPattern(matcherValue);
            chart.addFilter(filter);

            String legend = matcherNode.getLegend();
            if (legend != null) {
                filter.setLegend(legend);
            }
        }

        return chart;
    }

    private void processChunkerNodes(final ChunkedResultHandler handler, List<ResultGenerator> generators, List<LogEventListener> listeners, List<ChunkerConfiguration> chunkingParsers) {
        for (ChunkerConfiguration chunkerNode : chunkingParsers) {
            Tracer.trace("Processing chunking parser {}", chunkerNode);
            TimeChunkingGenerator generator = new TimeChunkingGenerator();

            generator.addChunkedResultHandler(handler);
            generators.add(generator);

            List<ParserConfiguration> parserConfigurations = chunkerNode.getParserConfigurations();
            for (ParserConfiguration parserConfiguration : parserConfigurations) {
                String format = parserConfiguration.getFormatConfiguration().getValue();

                ResultKeyBuilder resultKeyBuilder = new ResultKeyBuilder(format);

                List<PatternConfiguration> patterns = parserConfiguration.getPatterns();
                for (PatternConfiguration patternConfiguration : patterns) {
                    processPatternNode(listeners, resultKeyBuilder, patternConfiguration);
                }

                resultKeyBuilder.addResultListener(generator);
            }
        }
    }

    private void processPatternNode(List<LogEventListener> listeners, ResultKeyBuilder resultKeyBuilder, PatternConfiguration patternConfiguration) {
        String pattern = patternConfiguration.getValue();
        boolean debug = patternConfiguration.isDebug();
        boolean cleanUp = patternConfiguration.isCleanup();

        ValueStripper2 stripper = new ValueStripper2();
        stripper.setPattern(pattern);
        stripper.setDebug(debug);
        if (stripper.getLabels().isEmpty()) {
            // Hmm no labels set, this could be a simple pattern
            String name = patternConfiguration.getName();
            if (name != null) {
                SimpleMatcher matcher = new SimpleMatcher(pattern, name);
                matcher.addResultListener(resultKeyBuilder);
                listeners.add(matcher);
            }
            else {
                logger.warn(String.format("Your parsers config looks broken : the pattern was '%s'; it didn't contain any {labels}, and it didn't have a name='' attribute either - you have to have one or more {labels} or a name element to build a matcher",
                                          pattern));
            }
        }
        else {
            stripper.addResultListener(resultKeyBuilder);
            listeners.add(stripper);
        }
    }

    private void processRawNodes(final ChunkedResultHandler handler, List<ResultGenerator> generators, List<LogEventListener> listeners, List<ParserConfiguration> rawParsers) {
        RawGenerator generator = new RawGenerator();
        generator.addChunkedResultHandler(handler);
        generators.add(generator);

        for (ParserConfiguration parserNode : rawParsers) {
            String format = parserNode.getFormatConfiguration().getValue();

            ResultKeyBuilder resultKeyBuilder = new ResultKeyBuilder(format);

            List<PatternConfiguration> patterns = parserNode.getPatterns();
            for (PatternConfiguration patternConfiguration : patterns) {
                processPatternNode(listeners, resultKeyBuilder, patternConfiguration);
            }

            resultKeyBuilder.addResultListener(generator);
        }
    }

    private void startChartUpdateTimer(final Collection<UpdatesEachSecond> updatesEachSecond) {
        Timer timer = new Timer("Chart update timer");
        TimerTask task = new TimerTask() {
            @Override public void run() {
                for (UpdatesEachSecond chart : updatesEachSecond) {
                    chart.update();
                }
            }
        };
        timer.schedule(task, 1000, 1000);
    }
}
