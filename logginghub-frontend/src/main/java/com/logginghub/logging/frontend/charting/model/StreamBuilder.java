package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.AggregatedPatternParser;
import com.logginghub.logging.utils.ValueStripper2.ValueStripper2ResultListener2;
import com.logginghub.utils.EnvironmentProperties;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class StreamBuilder implements ValueStripper2ResultListener2 {

    private static Logger logger = Logger.getLoggerFor(StreamBuilder.class);
    private int labelIndex = -1;
    private String[] eventParts;
    private int patternID = -1;
    private Stream<StreamResultItem> output;
    private String groupBy = null;

    private PatternManagementService patternService;

    private List<ChartSeriesFilterModel> filters = new CopyOnWriteArrayList<ChartSeriesFilterModel>();

    // private Map<String, Set<String>> whitelistByVariable = new HashMap<String, Set<String>>();
    // private Map<String, Set<String>> blacklistByVariable = new HashMap<String, Set<String>>();

    // private Set<String> whitelist = new HashSet<String>();
    // private Set<String> blacklist = new HashSet<String>();

    public StreamBuilder(Stream<StreamResultItem> output, PatternManagementService patternService) {
        this.output = output;
        this.patternService = patternService;
    }

    public Stream<StreamResultItem> getOutput() {
        return output;
    }

    public String[] getEventParts() {
        return eventParts;
    }

    public void setEventParts(String[] eventParts) {
        this.eventParts = eventParts;
    }

    @Override public void onNewPatternisedResult(PatternisedLogEvent entry, boolean[] isNumeric) {

        if (labelIndex != -1 && eventParts != null) {
            String result = "?";
            boolean isResultNumeric = false;

            result = entry.getVariables()[labelIndex];
            isResultNumeric = isNumeric[labelIndex];
            String labelName = getLabelName(patternID, labelIndex);

            StringBuilder streamBuilder = new StringBuilder();

            String div = "";
            for (String string : eventParts) {
                streamBuilder.append(div);
                if (string.contains("Source Application")) {
                    streamBuilder.append(entry.getSourceApplication());
                }
                else if (string.contains("Source Host")) {
                    if (EnvironmentProperties.getBoolean("dontShrinkHostnames")) {
                        streamBuilder.append(entry.getSourceHost());
                    }
                    else {
                        streamBuilder.append(shortenHost(entry.getSourceHost()));

                    }
                }
                else if (string.contains("Source IP")) {
                    streamBuilder.append(entry.getSourceAddress());
                }
                else {
                    streamBuilder.append(string);
                }

                div = "/";
            }

            String groupByValue = null;
            if (StringUtils.isNotNullOrEmpty(groupBy)) {
                AggregatedPatternParser parser = new AggregatedPatternParser();
                
                Pattern patternByID = patternService.getPatternByID(patternID);                
                parser.parse(groupBy, patternByID.getPattern());
                groupByValue = parser.format(entry);
            }

            String path;
            if (StringUtils.isNotNullOrEmpty(groupByValue)) {
                path = groupByValue;
            }
            else {
                path = StringUtils.format("{}/{}/{}", getPatternName(patternID), labelName, streamBuilder.toString());
            }

            boolean publish = true;

            // Map<String, String> values = new HashMap<String, String>();
            // for (int i = 0; i < labels.length; i++) {
            // String label = labels[i];
            // String variableValue = patternVariables[i];
            // values.put(label, variableValue);
            // }

            for (ChartSeriesFilterModel filter : filters) {
                if (filter.getEnabled().asBoolean()) {

                    int variableIndex = filter.getVariableIndex().get();

                    String variableValue = entry.getVariables()[variableIndex];

                    Set<String> whitelist = filter.getWhitelistValues();
                    Set<String> blacklist = filter.getBlacklistValues();

                    boolean whitelisted = whitelist == null || whitelist.isEmpty() || whitelist.contains(variableValue);

                    if (whitelisted) {
                        boolean blacklisted = blacklist != null && blacklist.contains(variableValue);
                        if (blacklisted) {
                            logger.fine("Dropping event as value '{}' for variable '{}' was blacklisted", variableValue, labelIndex);
                            publish = false;
                            break;
                        }
                    }
                    else {
                        logger.fine("Dropping event as value '{}' for variable '{}' was not whitelisted", variableValue, labelIndex);
                        publish = false;
                        break;
                    }
                }
            }

            if (publish) {
                logger.fine("Output path is '{}'", path);
                output.send(new StreamResultItem(entry.getTime(), labelName, path, groupBy, result, isResultNumeric));
            }
        }

    }

    private String getLabelName(int patternID, int labelIndex) {
        return patternService.getLabelName(patternID, labelIndex);

    }

    private String getPatternName(int patternID) {
        return patternService.getPatternName(patternID);
    }

    public static String shortenHost(String key) {

        String shorter;
        int indexOf = key.indexOf(".");
        if (indexOf != -1) {
            shorter = key.substring(0, indexOf);
        }
        else {
            shorter = key;
        }

        return shorter;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setStream(String string) {
        if (string != null && !string.equals("null") && string.length() > 0) {
            String[] split = string.split("/");
//            patternID = Integer.parseInt(split[0]);
//            labelIndex = Integer.parseInt(split[1]);

//            eventParts = new String[split.length - 2];
//            System.arraycopy(split, 2, eventParts, 0, eventParts.length);
            eventParts = split;
        }
        else {
            eventParts = new String[] {};
        }
        logger.debug("Setting stream to '{}' : event parts are {}", string, Arrays.toString(eventParts));
    }

    public void setEventElements(ObservableList<String> eventElements) {
        eventParts = StringUtils.toArray(eventElements);
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public void addFilter(ChartSeriesFilterModel t) {
        filters.add(t);
    }

    public void removeAllFilters() {
        filters.clear();
    }

    public void removeFilter(ChartSeriesFilterModel t) {
        filters.remove(t);
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }
}
