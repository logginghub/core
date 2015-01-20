package com.logginghub.logging.repository.processors;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.TimeSeriesAggregator;
import com.logginghub.analytics.TimeSeriesCounter;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.LogDataProcessor;
import com.logginghub.logging.repository.processors.AnnoyingTokeniser.Mode;
import com.logginghub.utils.IntegerFrequencyCount;

/**
 * Extracts values from events based on a regular expression. Does not do any
 * aggregation.
 * 
 * @author James
 * 
 */
public class RegexExtractingProcessor implements LogDataProcessor {

    private File resultsFolder;
    private Boolean[] nameOrValue;
    private String regex;
    private Pattern pattern;

    /**
     * The name to prepend to output files to distinguish between different
     * regex extractor results
     */
    private final String name;

    /**
     * The number of name elements we are expecting from the matcher
     */
    private int names;

    /**
     * The number of value elements we are expecting from the matcher
     */
    private int values;

    /**
     * The dataseries we'll be building over the processing period
     */
    private TimeSeriesData data = new TimeSeriesData();
    private final long aggregationDuration;
    private String[] valueNames;
    private String[] nameNames;
    private boolean allowNumericParseFailures;
    private boolean countNameElements = true;

    public RegexExtractingProcessor(String name) {
        this(name, 1000);
    }

    public RegexExtractingProcessor(String name, long bucketDuration) {
        this.name = name;
        this.aggregationDuration = bucketDuration;
    }

    public boolean isCountNameElements() {
        return countNameElements;
    }

    public boolean isAllowNumericParseFailures() {
        return allowNumericParseFailures;
    }

    public void setCountNameElements(boolean countNameElements) {
        this.countNameElements = countNameElements;
    }

    public void onNewLogEvent(LogEvent event) {

        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {

            String[] names = new String[this.names];
            double[] values = new double[this.values];

            int namesIndex = 0;
            int valuesIndex = 0;

            try {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    if (this.nameOrValue[i]) {
                        names[namesIndex++] = matcher.group(i + 1);
                    }
                    else {
                        values[valuesIndex++] = parse(matcher.group(i + 1));
                    }
                }
            }
            catch (RuntimeException e) {
                throw new RuntimeException("Failed to process message '" + event.getMessage() + "'", e);
            }

            data.add(event.getOriginTime(), names, values);
        }
    }

    private double parse(String group) {
        double value;

        try {
            value = Double.parseDouble(group);
        }
        catch (NumberFormatException nfe) {
            try {
                value = NumberFormat.getInstance().parse(group).doubleValue();
            }
            catch (ParseException e) {
                if (allowNumericParseFailures) {
                    value = 0;
                }
                else {
                    throw new RuntimeException("Faield to parse '" +
                                               group +
                                               "' as a double or a Number. Please check your matching expression, as values (in curly braces) have to be numeric");
                }
            }
        }

        return value;

    }

    public void processingStarted(File resultsFolder) {
        this.resultsFolder = resultsFolder;
    }

    public void processingEnded() {

        if (!resultsFolder.exists()) {
            resultsFolder.mkdirs();
        }

        processTimeSeriesData("", data);
    }

    protected void processTimeSeriesData(String subAnalysis, TimeSeriesData data) {

        String fileStart = name;
        if (!subAnalysis.isEmpty()) {
            fileStart += "." + subAnalysis;
        }
        
        data.dumpToCSV(new File(resultsFolder, fileStart + ".raw.csv"));

        for (int i = 0; i < valueNames.length; i++) {

            String valueName = valueNames[i];

            TimeSeriesAggregator aggregator = new TimeSeriesAggregator();
            AggregatedData aggregated = aggregator.aggregate(valueName, data, aggregationDuration, i);

            ChartBuilder.startXY()
                        .setTitle(valueName + " (time aggregated " + aggregationDuration + ")")
                        .addSeries(aggregated, AggregatedDataKey.Mean)
                        .toPng(new File(resultsFolder, fileStart + "." + valueName + ".mean.png"))
                        .toCSV(new File(resultsFolder, fileStart + "." + valueName + ".mean.csv"));

            ChartBuilder.startXY()
                        .setTitle(valueName + " (time aggregated " + aggregationDuration + ")")
                        .addSeries(aggregated, AggregatedDataKey.Count)
                        .toPng(new File(resultsFolder, fileStart + "." + valueName + ".count.png"))
                        .toCSV(new File(resultsFolder, fileStart + "." + valueName + ".count.csv"));

            ChartBuilder.startXY()
                        .setTitle(valueName + " (time aggregated " + aggregationDuration + ")")
                        .addSeries(aggregated, AggregatedDataKey.Sum)
                        .toPng(new File(resultsFolder, fileStart + "." + valueName + ".sum.png"))
                        .toCSV(new File(resultsFolder, fileStart + "." + valueName + ".sum.csv"));
        }

        if (countNameElements) {
            for (int i = 0; i < nameNames.length; i++) {

                String name = nameNames[i];

                TimeSeriesCounter counter = new TimeSeriesCounter();
                IntegerFrequencyCount count = counter.count(name, data, i);
                IntegerFrequencyCount top = count.top(10);

                ChartBuilder.startBar()
                            .setTitle(name + " frequency")
                            .addFrequencyCountSeries(name, top)
                            .enableLongCategoryNames()
                            .toPng(new File(resultsFolder, fileStart + "." + name + ".frequency.png"))
                            .toCSV(new File(resultsFolder, fileStart + "." + name + ".frequency.csv"));
            }
        }
    }

    public TimeSeriesData getData() {
        return data;
    }

    public void setSimpleExpression(String expression) {

        AnnoyingTokeniser at = new AnnoyingTokeniser(expression);

        at.getDefaultMode().setTokenSeparators();
        at.getDefaultMode().setIncludeTokens(true);
        at.getDefaultMode().setAllowEmptyTokens(true);

        Mode squareBracketsMode = at.createMode();
        squareBracketsMode.setTokenSeparators();
        squareBracketsMode.setModeStartCharacters('[');
        squareBracketsMode.setModeEndCharacters(']');
        squareBracketsMode.setIncludeTokens(true);

        Mode curleyBracketsMode = at.createMode();
        curleyBracketsMode.setTokenSeparators();
        curleyBracketsMode.setModeStartCharacters('{');
        curleyBracketsMode.setModeEndCharacters('}');
        curleyBracketsMode.setIncludeTokens(true);

        at.getDefaultMode().addSubMode(curleyBracketsMode);
        at.getDefaultMode().addSubMode(squareBracketsMode);

        final int value = 0;
        final int variable = 1;

        StringBuilder regex = new StringBuilder();
        List<String> tokens = new ArrayList<String>();
        List<Boolean> nameOrValue = new ArrayList<Boolean>();

        List<String> valueNames = new ArrayList<String>();
        List<String> nameNames = new ArrayList<String>();

        int tokenIndex = 0;
        int valueCount = 0;
        int nameCount = 0;
        while (at.hasMoreTokens()) {
            String token = at.nextToken();
            if (at.getCurrentTokenMode() == squareBracketsMode) {
                regex.append("(.*?)");
                nameOrValue.add(true);
                nameNames.add(token.substring(1, token.length() - 1));
                nameCount++;
            }
            else if (at.getCurrentTokenMode() == curleyBracketsMode) {
                regex.append("(.*?)");
                nameOrValue.add(false);
                valueNames.add(token.substring(1, token.length() - 1));
                valueCount++;

            }
            else {
                regex.append(token);
            }
        }

        this.nameOrValue = nameOrValue.toArray(new Boolean[] {});

        this.values = valueCount;
        this.valueNames = valueNames.toArray(new String[] {});
        this.nameNames = nameNames.toArray(new String[] {});
        this.names = nameCount;

        this.regex = regex.toString();
        this.pattern = Pattern.compile(this.regex);
    }

    public void setAllowNumericParseFailures(boolean allowNumericParseFailures) {
        this.allowNumericParseFailures = allowNumericParseFailures;
    }

}
