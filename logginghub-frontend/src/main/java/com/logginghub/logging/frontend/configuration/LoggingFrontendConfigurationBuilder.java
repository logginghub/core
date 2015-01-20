package com.logginghub.logging.frontend.configuration;

import com.logginghub.logging.hub.configuration.TimestampVariableRollingFileLoggerConfiguration;

public class LoggingFrontendConfigurationBuilder {

    private LoggingFrontendConfiguration configuration = new LoggingFrontendConfiguration();

    public static LoggingFrontendConfigurationBuilder newConfiguration() {
        return new LoggingFrontendConfigurationBuilder();
    }

    public LoggingFrontendConfiguration toConfiguration() {
        return configuration;
    }

    public LoggingFrontendConfigurationBuilder selectedRowFormat(RowFormatConfigurationBuilder rowFormat) {
        configuration.setSelectedRowFormat(rowFormat.getConfiguration());
        return this;
    }

    public EnvironmentConfigurationBuilder environment(String name) {

        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration();
        environmentConfiguration.setName(name);

        configuration.getEnvironments().add(environmentConfiguration);

        EnvironmentConfigurationBuilder environment = new EnvironmentConfigurationBuilder(environmentConfiguration);
        return environment;
    }

    public LoggingFrontendConfigurationBuilder setShowDashboard(boolean showDashboard) {
        configuration.setShowDashboard(showDashboard);
        return this;
    }

    public LoggingFrontendConfigurationBuilder setPopoutCharting(boolean popoutCharting) {
        configuration.setPopoutCharting(popoutCharting);
        return this;
    }

    public static class EnvironmentConfigurationBuilder {

        private EnvironmentConfiguration environmentConfiguration;

        public EnvironmentConfigurationBuilder(EnvironmentConfiguration environmentConfiguration) {
            this.environmentConfiguration = environmentConfiguration;
        }

        public EnvironmentConfigurationBuilder hub(String host, int port) {
            environmentConfiguration.getHubs().add(new HubConfiguration(host, host, port));
            return this;
        }

        public EnvironmentConfigurationBuilder highlighter(String message, String colourHex, boolean regex) {
            HighlighterConfiguration highlighterConfiguration = new HighlighterConfiguration();
            highlighterConfiguration.setColourHex(colourHex);
            highlighterConfiguration.setPhrase(message);
            environmentConfiguration.getHighlighters().add(highlighterConfiguration);
            return this;
        }

        public EnvironmentConfiguration toEnvironment() {
            return environmentConfiguration;

        }

        public EnvironmentConfigurationBuilder setChannel(String channel) {
            environmentConfiguration.setChannel(channel);
            return this;
        }

        public EnvironmentConfigurationBuilder setAutoLocking(boolean autoLocking) {
            environmentConfiguration.setAutoLocking(autoLocking);
            return this;
        }

        public EnvironmentConfigurationBuilder setRepoEnabled(boolean b) {
            environmentConfiguration.setRepoEnabled(b);
            return this;
        }

        public EnvironmentConfigurationBuilder setWriteOutputLog(boolean b) {
            environmentConfiguration.setWriteOutputLog(b);
            return this;
        }

        public EnvironmentConfigurationBuilder setOutputLogConfiguration(TimestampVariableRollingFileLoggerConfiguration logConfiguration) {
            environmentConfiguration.setOutputLogConfiguration(logConfiguration);
            return this;
        }

        public EnvironmentConfigurationBuilder addQuickFilter(String filter) {
            environmentConfiguration.getQuickFilters().add(filter);
            return this;
        }

        // public ChartingConfigurationBuilder newChart() {
        // ChartingConfiguration chartingConfiguration =
        // environmentConfiguration.getChartingConfiguration();
        // ChartingConfigurationBuilder builder = new
        // ChartingConfigurationBuilder(chartingConfiguration);
        // return builder;
        // }
    }

    public static EnvironmentConfigurationBuilder newEnvironment(String name) {
        EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration();
        environmentConfiguration.setName(name);
        EnvironmentConfigurationBuilder environment = new EnvironmentConfigurationBuilder(environmentConfiguration);
        return environment;

    }

    public LoggingFrontendConfigurationBuilder environment(EnvironmentConfigurationBuilder highlighter) {
        configuration.getEnvironments().add(highlighter.toEnvironment());
        return this;
    }

    public static ChartBuilder chart(String title) {
        ChartConfiguration chartConfiguration = new ChartConfiguration();
        chartConfiguration.setTitle(title);
        return new ChartBuilder(chartConfiguration);
    }

    public static class ChartBuilder {

        private ChartConfiguration chartConfiguration;

        public ChartBuilder(ChartConfiguration chartConfiguration) {
            this.chartConfiguration = chartConfiguration;
        }

        public ChartBuilder addMatcher(String value) {
            MatcherConfiguration matcherConfiguration = new MatcherConfiguration();
            matcherConfiguration.setValue(value);
            chartConfiguration.getMatchers().add(matcherConfiguration);
            return this;
        }

        public ChartConfiguration getChartConfiguration() {
            return chartConfiguration;
        }

        public ChartBuilder warningThreshold(double threshold) {
            chartConfiguration.setWarningThreshold(threshold);
            return this;

        }

        public ChartBuilder severeThreshold(double threshold) {
            chartConfiguration.setSevereThreshold(threshold);
            return this;

        }

        public ChartBuilder yAxisLock(double threshold) {
            chartConfiguration.setyAxisLock(threshold);
            return this;
        }

        public ChartBuilder dataPoints(int i) {
            chartConfiguration.setDataPoints(i);
            return this;

        }
    }

    public static class PageBuilder {

        private PageConfiguration pageConfiguration;

        public PageBuilder(PageConfiguration pageConfiguration) {
            this.pageConfiguration = pageConfiguration;
        }

        public PageBuilder addChart(ChartBuilder chartBuilder) {
            pageConfiguration.getCharts().add(chartBuilder.chartConfiguration);
            return this;
        }

        public PageConfiguration getPageConfiguration() {
            return pageConfiguration;
        }

    }

    public static class ChunkerBuilder {

        private ChunkerConfiguration chunkerConfiguration;

        public ChunkerBuilder(ChunkerConfiguration chunkerConfiguration) {
            this.chunkerConfiguration = chunkerConfiguration;
        }

        public ChunkerConfiguration getChunkerConfiguration() {
            return chunkerConfiguration;
        }

        public ChunkerBuilder addParser(ParserBuilder parserBuilder) {
            chunkerConfiguration.getParserConfigurations().add(parserBuilder.getParserConfiguration());
            return this;
        }

    }

    public static class ParserBuilder {

        private ParserConfiguration parserConfiguration;

        public ParserBuilder(ParserConfiguration parserConfiguration) {
            this.parserConfiguration = parserConfiguration;
        }

        public ParserConfiguration getParserConfiguration() {
            return parserConfiguration;
        }

        public ParserBuilder addPattern(String pattern, boolean debug) {
            PatternConfiguration configuration = new PatternConfiguration();
            configuration.setValue(pattern);
            configuration.setDebug(debug);
            parserConfiguration.getPatterns().add(configuration);
            return this;
        }

    }

    public static ParserBuilder parser(String pattern) {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        ParserFormatConfiguration parserFormatConfiguration = new ParserFormatConfiguration();
        parserFormatConfiguration.setValue(pattern);
        parserConfiguration.setFormatConfiguration(parserFormatConfiguration);
        return new ParserBuilder(parserConfiguration);

    }

    public static ChunkerBuilder chunker(int interval) {
        ChunkerConfiguration chunkerConfiguration = new ChunkerConfiguration();
        chunkerConfiguration.setInterval(interval);
        return new ChunkerBuilder(chunkerConfiguration);

    }

    public static PageBuilder page(String title, int rows, int columns) {
        PageConfiguration pageConfiguration = new PageConfiguration();
        pageConfiguration.setTitle(title);
        pageConfiguration.setRows(rows);
        pageConfiguration.setColumns(columns);
        return new PageBuilder(pageConfiguration);
    }

    public static class RowFormatConfigurationBuilder {
        private RowFormatConfiguration configuration;

        public RowFormatConfigurationBuilder(RowFormatConfiguration configuration) {
            this.configuration = configuration;
        }

        public RowFormatConfiguration getConfiguration() {
            return configuration;
        }

        public RowFormatConfigurationBuilder font(String fontDefinition) {
            configuration.setFont(fontDefinition);
            return this;
        }

        public RowFormatConfigurationBuilder backgroundColour(String colourDefinition) {
            configuration.setBackgroundColour(colourDefinition);
            return this;
        }

        public RowFormatConfigurationBuilder foregroundColour(String colourDefinition) {
            configuration.setForegroundColour(colourDefinition);
            return this;
        }

        public RowFormatConfigurationBuilder borderColour(String colourDefinition) {
            configuration.setBorderColour(colourDefinition);
            return this;
        }

        public RowFormatConfigurationBuilder borderLineWidth(int width) {
            configuration.setBorderLineWidth(width);
            return this;
        }
    }

    public static RowFormatConfigurationBuilder rowFormat() {
        RowFormatConfiguration configuration = new RowFormatConfiguration();
        RowFormatConfigurationBuilder builder = new RowFormatConfigurationBuilder(configuration);
        return builder;
    }

    public LoggingFrontendConfigurationBuilder chartingConfiguration(String absolutePath) {
        configuration.setChartingConfigurationFile(absolutePath);
        return this;
    }

    public LoggingFrontendConfigurationBuilder setShowOldCharting(boolean b) {
        configuration.setShowOldCharting(b);
        return this;
    }

}
