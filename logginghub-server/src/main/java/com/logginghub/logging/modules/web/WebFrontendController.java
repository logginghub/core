package com.logginghub.logging.modules.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.logginghub.analytics.model.LongFrequencyCount;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.datafiles.SummaryBuilder;
import com.logginghub.logging.datafiles.SummaryTimeElement;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.NonArrayChannelMessage;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SubscriptionController;
import com.logginghub.logging.modules.PatternManagerService;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.CompareUtils;
import com.logginghub.utils.Destination;
import com.logginghub.utils.JSONWriter;
import com.logginghub.utils.KeyedFactory;
import com.logginghub.utils.MutableLongValue;
import com.logginghub.utils.Result;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.TimeKey;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.observable.Counterparts;
import com.logginghub.web.Param;
import com.logginghub.web.RequestContext;
import com.logginghub.web.WebController;
import com.logginghub.web.WebSocketHelper;
import com.logginghub.web.WebSocketListener;
import com.logginghub.web.WebSocketSupport;
import org.eclipse.jetty.websocket.WebSocket.Connection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Future;

@WebController(staticFiles = "/logginghubweb/")
public class WebFrontendController implements WebSocketSupport {

    private static final Logger logger = Logger.getLoggerFor(WebFrontendController.class);
    private Map<String, AuthenticationResult> sessions = new HashMap<String, AuthenticationResult>();

    // private Map<String, List<Connection>> subscriptions = new HashMap<String,
    // List<Connection>>();
    private Counterparts<String, Destination<ChannelMessage>> hubSubscriptionCounterparts = new Counterparts<String, Destination<ChannelMessage>>();
    private Counterparts<Connection, Destination<String>> subscriptionCounterparts = new Counterparts<Connection, Destination<String>>();
    private WebSocketHelper webSocketHelper;
    private PatternManagerService patternManager;
    private ChannelMessagingService channelMessaging;
    private SubscriptionController<Destination<String>, String> subscriptions = new SubscriptionController<Destination<String>, String>() {
        @Override
        protected Future<Boolean> handleFirstSubscription(String channel) {
            return createHubSubscription(channel);
        }

        @Override
        protected Future<Boolean> handleLastSubscription(String channel) {
            return removeHubSubscription(channel);
        }
    };
    private DataController dataController = new DataController();
    private ServiceDiscovery serviceDiscovery;
    private LoggingHubDatabase database = new FirstCutLoggingHubDatabase(new File("data/database"));
    private DatabaseHelper databaseHelper = new DatabaseHelper(database);
    private Gson gson = new Gson();

    public WebFrontendController() {
        WorkerThread.execute("Test data reader", new Runnable() {
            @Override
            public void run() {
                dataController.loadData();
            }
        });
    }

    private void broadcastAndRemoveFailures(List<Connection> list, String message) {
        List<Connection> failures = new ArrayList<Connection>();
        for (Connection connection : list) {
            try {
                logger.fine("Sending to connection '{}' : {}", connection, message);
                connection.sendMessage(message);
            } catch (IOException e) {
                logger.fine("Connection send failed, removing subscription");
                failures.add(connection);
            }
        }

        list.removeAll(failures);
    }

    public void broadcastEvent(LogEvent event) {
        String channel = "events";

        if (subscriptions.hasSubscriptions(channel)) {
            // TODO :use gson?
            JsonObject eventJSON = toJSON(event);

            JsonObject broadcastJSON = new JsonObject();
            broadcastJSON.addProperty("channel", channel);
            broadcastJSON.addProperty("value", eventJSON.toString());

            String json = broadcastJSON.toString();

            subscriptions.dispatch(Channels.toArray(channel), json);
        }
    }

    private JsonObject toJSON(LogEvent event) {
        JsonObject eventJSON = new JsonObject();
        eventJSON.addProperty("channel", event.getChannel());
        eventJSON.addProperty("formattedException", event.getFormattedException());
        eventJSON.addProperty("level", event.getLevel());
        eventJSON.addProperty("levelDescription", event.getLevelDescription());
        eventJSON.addProperty("time", event.getOriginTime());
        eventJSON.addProperty("loggerName", event.getLoggerName());
        eventJSON.addProperty("message", event.getMessage());
        eventJSON.addProperty("pid", event.getPid());
        eventJSON.addProperty("sequenceNumber", event.getSequenceNumber());
        eventJSON.addProperty("sourceAddress", event.getSourceAddress());
        eventJSON.addProperty("sourceApplication", event.getSourceApplication());
        eventJSON.addProperty("sourceClassName", event.getSourceClassName());
        eventJSON.addProperty("sourceHost", event.getSourceHost());
        eventJSON.addProperty("sourceMethodName", event.getSourceMethodName());
        eventJSON.addProperty("threadName", event.getThreadName());
        return eventJSON;
    }

    public String createAggregation(@Param(name = "pattern") int pattern,
                                    @Param(name = "label") int label,
                                    @Param(name = "interval") String intervalString,
                                    @Param(name = "type") String type,
                                    @Param(name = "groupBy") String groupBy) {
        logger.info("Create aggregation : pattern='{}' interval='{}' type='{}'", pattern, intervalString, type);
        Result<Aggregation> aggregationModel = patternManager.createAggregation(pattern,
                                                                                label,
                                                                                TimeUtils.parseInterval(intervalString),
                                                                                AggregationType.valueOf(type),
                                                                                groupBy);
        return toJSON(aggregationModel);
    }

    private String toJSON(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        System.out.println(json);
        return json;
    }

    protected Future<Boolean> createHubSubscription(String channel) {

        logger.info("Creating subscription to hub channel '{}'", channel);

        Destination<ChannelMessage> destination = new Destination<ChannelMessage>() {
            @Override
            public void send(ChannelMessage t) {
                dispatchHubChannelMessage(t);
            }
        };

        hubSubscriptionCounterparts.put(channel, destination);
        channelMessaging.subscribe(channel, destination);

        return null;

    }

    protected void dispatchHubChannelMessage(ChannelMessage t) {

        NonArrayChannelMessage message = NonArrayChannelMessage.fromChannelMessage(t);
        String channel = message.getChannel();
        if (subscriptions.hasSubscriptions(channel)) {
            Gson gson = new Gson();
            String json = gson.toJson(message);
            logger.fine("Dispatching json to websocket connections for channel '{}' : data '{}'", channel, json);
            subscriptions.dispatch(Channels.toArray(channel), json);
        }

    }

    public String createPattern(@Param(name = "name") String name, @Param(name = "pattern") String pattern) {
        logger.info("Create pattern name '{}' pattern '{}'", name, pattern);
        Result<Pattern> patternModel = patternManager.createPattern(name, pattern);
        return toJSON(patternModel);
    }

    public String daystats(@Param(name = "year") int year, @Param(name = "month") int month, @Param(name = "day") int day) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();
        JsonArray allStatsArray = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);

        DataController.MonthlyData monthlyData = yearlyData.getSubPeriodData().get(DataController.PeriodKey.month(year, month));
        DataController.DailyData dailyData = monthlyData.getSubPeriodData().get(DataController.PeriodKey.dayOfMonth(year, month, day));

        Collection<DataController.HourlyData> values = dailyData.getSubPeriodData().values();
        List<DataController.HourlyData> sorted = new ArrayList<DataController.HourlyData>(values);
        Collections.sort(sorted, new Comparator<DataController.HourlyData>() {
            @Override
            public int compare(DataController.HourlyData o1, DataController.HourlyData o2) {
                return CompareUtils.compare(o1.getKey(), o2.getKey());
            }
        });

        for (DataController.HourlyData hour : sorted) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("hour", new JsonPrimitive(hour.getKey().hour));
            itemObject.add("day", new JsonPrimitive(hour.getKey().dayOfMonth));
            itemObject.add("month", new JsonPrimitive(hour.getKey().month));
            itemObject.add("year", new JsonPrimitive(hour.getKey().year));
            itemObject.add("events", new JsonPrimitive(hour.getCount()));

            SinglePassStatisticsDoublePrecision[] statsArray = hour.getNumericStats();
            for (SinglePassStatisticsDoublePrecision stats : statsArray) {
                if (stats != null) {

                    JsonObject statsObject = new JsonObject();
                    JsonArray percentilesArray = new JsonArray();

                    stats.doCalculations();

                    statsObject.add("count", new JsonPrimitive(stats.getCount()));
                    statsObject.add("mean", new JsonPrimitive(stats.getMean()));
                    statsObject.add("min", new JsonPrimitive(stats.getMinimum()));
                    statsObject.add("max", new JsonPrimitive(stats.getMaximum()));

                    double[] percentiles = stats.getPercentiles();
                    percentilesArray.add(new JsonPrimitive(percentiles[0]));
                    percentilesArray.add(new JsonPrimitive(percentiles[10]));
                    percentilesArray.add(new JsonPrimitive(percentiles[20]));
                    percentilesArray.add(new JsonPrimitive(percentiles[30]));
                    percentilesArray.add(new JsonPrimitive(percentiles[40]));
                    percentilesArray.add(new JsonPrimitive(percentiles[50]));
                    percentilesArray.add(new JsonPrimitive(percentiles[60]));
                    percentilesArray.add(new JsonPrimitive(percentiles[70]));
                    percentilesArray.add(new JsonPrimitive(percentiles[80]));
                    percentilesArray.add(new JsonPrimitive(percentiles[90]));
                    percentilesArray.add(new JsonPrimitive(percentiles[100]));
                    percentilesArray.add(new JsonPrimitive(percentiles[95]));
                    percentilesArray.add(new JsonPrimitive(percentiles[98]));
                    percentilesArray.add(new JsonPrimitive(percentiles[99]));

                    statsObject.add("percentiles", percentilesArray);

                    // TODO: should be an array for multiple numeric values in the pattern
                    itemObject.add("patternStats", statsObject);

                }
            }

            array.add(itemObject);

        }

        object.add("hours", array);

        JsonArray allFrequenciesArray = new JsonArray();

        LongFrequencyCount[] frequencyCounts = dailyData.getFrequencyCounts();
        for (LongFrequencyCount frequencyCount : frequencyCounts) {
            if (frequencyCount != null) {

                JsonArray frequencyArray = new JsonArray();

                int max = 20;
                int index = 0;
                List<MutableLongValue> sortedValues = frequencyCount.getSortedValues();
                for (MutableLongValue sortedValue : sortedValues) {

                    JsonObject frequencyObject = new JsonObject();
                    frequencyObject.add("value", new JsonPrimitive(sortedValue.key.toString()));
                    frequencyObject.add("count", new JsonPrimitive(sortedValue.value));

                    frequencyArray.add(frequencyObject);

                    index++;
                    if (index == max) {
                        break;
                    }
                }

                allFrequenciesArray.add(frequencyArray);
            }

        }

        object.add("variableFrequencies", allFrequenciesArray);
        object.add("variableStats", allStatsArray);

        return object.toString();
    }

    public String getAggregations() {
        logger.info("Getting aggregations");
        Result<ObservableList<Aggregation>> aggregations = patternManager.getAggregations();
        return toJSON(aggregations);
    }

    public String getAllPatterns() {

        final List<SummaryTimeElement> elements = new ArrayList<SummaryTimeElement>();
        SummaryBuilder.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                       "bats.aggregatedsummary.binary.log"), new Destination<SummaryTimeElement>() {
            @Override
            public void send(SummaryTimeElement summaryTimeElement) {
                elements.add(summaryTimeElement);
            }
        });

        // Sort the patterns by their Id
        ObservableList<Pattern> patternsSortedById = patternManager.getPatterns().getValue();
        Collections.sort(patternsSortedById, new Comparator<Pattern>() {
            @Override
            public int compare(Pattern o1, Pattern o2) {
                return Integer.compare(o1.getPatternId(), o2.getPatternId());
            }
        });

        // Put the names into an array for table headings
        JsonArray patternNames = new JsonArray();
        patternNames.add(new JsonPrimitive("Total"));
        for (Pattern pattern : patternsSortedById) {
            patternNames.add(new JsonPrimitive(pattern.getName()));
        }

        // Build the time elements
        JsonArray timeArray = new JsonArray();
        for (SummaryTimeElement element : elements) {

            JsonArray patternValuesTemp = new JsonArray();

            long total = 0;
            for (Pattern pattern : patternsSortedById) {
                int patternId = pattern.getPatternId();
                long countForPattern = element.getCountForPattern(patternId);
                total += countForPattern;
                patternValuesTemp.add(new JsonPrimitive(countForPattern));
            }

            JsonArray patternValues = new JsonArray();
            patternValues.add(new JsonPrimitive(total));
            patternValues.addAll(patternValuesTemp);

            JsonObject timeElement = new JsonObject();
            timeElement.add("time", new JsonPrimitive(Logger.toTimeString(element.getTime()).toString()));
            timeElement.add("values", patternValues);

            timeArray.add(timeElement);
        }

        // Put this all together
        JsonObject object = new JsonObject();
        object.add("names", patternNames);
        object.add("times", timeArray);

        return object.toString();
    }

    public JsonArray getCalendar() {

        //LoggingHubDatabase database = serviceDiscovery.findService(LoggingHubDatabase.class);

        Calendar calendar = new GregorianCalendar();

        int weeksBefore = 3;
        int weeksAfter = 1;

        int todayYear = calendar.get(Calendar.YEAR);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.add(Calendar.WEEK_OF_YEAR, -weeksBefore);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        JsonArray weeks = new JsonArray();

        for (int i = 0; i < weeksBefore + weeksAfter; i++) {

            JsonObject week = new JsonObject();
            week.add("year", new JsonPrimitive(calendar.get(Calendar.YEAR)));
            week.add("month", new JsonPrimitive(calendar.get(Calendar.MONTH)));
            week.add("monthFormatted", new JsonPrimitive(new SimpleDateFormat("MMM").format(calendar.getTime())));
            week.add("startDayOfMonth", new JsonPrimitive(calendar.get(Calendar.DAY_OF_MONTH)));

            JsonArray days = new JsonArray();
            for (int j = 0; j < 7; j++) {

                JsonObject day = new JsonObject();
                day.add("date", new JsonPrimitive(calendar.get(Calendar.DAY_OF_MONTH)));
                day.add("month", new JsonPrimitive(calendar.get(Calendar.MONTH)));
                day.add("year", new JsonPrimitive(calendar.get(Calendar.YEAR)));
                day.add("dayFormatted", new JsonPrimitive(new SimpleDateFormat("DDD").format(calendar.getTime())));

                int dayYear = calendar.get(Calendar.YEAR);
                int dayMonth = calendar.get(Calendar.MONTH);
                int dayDay = calendar.get(Calendar.DAY_OF_MONTH);

                if (dayYear == todayYear && dayMonth == todayMonth && dayDay == todayDay) {
                    day.add("isToday", new JsonPrimitive(true));
                }

                SummaryStatistics dailyStats = database.getDailyStats(dayYear, dayMonth, dayDay);
                if (dailyStats != null) {
                    day.add("total", new JsonPrimitive(dailyStats.getCount()));
                }

                days.add(day);

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            week.add("days", days);
            weeks.add(week);
        }

        return weeks;
    }

    public JsonObject getDaily(@Param(name = "year") int year, @Param(name = "month") int month, @Param(name = "day") int day) {
        logger.info("Creating daily view for year='{}' month='{}' day='{}'", year, month, day);

        //LoggingHubDatabase database = serviceDiscovery.findService(LoggingHubDatabase.class);

        JsonObject dailyView = new JsonObject();

        dailyView.add("year", new JsonPrimitive(year));
        dailyView.add("month", new JsonPrimitive(month));
        dailyView.add("day", new JsonPrimitive(day));

        SummaryStatistics dailyStats = database.getDailyStats(year, month, day);
        if (dailyStats != null) {
            dailyView.add("total", new JsonPrimitive(dailyStats.getCount()));
        }

        JsonArray segments = new JsonArray();
        for (int i = 0; i < 8; i++) {

            JsonObject segment = new JsonObject();
            JsonArray hours = new JsonArray();

            for (int j = 0; j < 3; j++) {
                JsonObject hour = new JsonObject();

                int hourIndex = (i * 3) + j;
                hour.add("hour", new JsonPrimitive(hourIndex));

                SummaryStatistics hourlyStats = database.getHourlyStats(year, month, day, hourIndex);
                if (hourlyStats != null) {
                    hour.add("total", new JsonPrimitive(hourlyStats.getCount()));
                }


                hours.add(hour);
            }

            segment.add("hours", hours);
            segments.add(segment);
        }

        dailyView.add("segments", segments);

        return dailyView;
    }

    public JsonObject getHourly(@Param(name = "year") int year,
                                @Param(name = "month") int month,
                                @Param(name = "day") int day,
                                @Param(name = "hour") int hour,
                                @Param(name = "independentPatternHeat") boolean independentPatternHeat,
                                @Param(name = "useTotalScale") boolean useTotalScale) {

        int columns = 6;
        int rows = 10;
        long interval = TimeUtils.hours;
        long subInterval = TimeUtils.minutes;
        TimeKey timeKey = TimeKey.from(year, month, day, hour);
        String viewName = "hourly";

        return createView(independentPatternHeat, useTotalScale, columns, rows, interval, subInterval, timeKey, viewName);

//
//
//
//        logger.info("Creating hourly view for year='{}' month='{}' day='{}' hour='{}'", year, month, day, hour);
//
//        JsonObject hourlyView = new JsonObject();
//
//        hourlyView.add("year", new JsonPrimitive(year));
//        hourlyView.add("month", new JsonPrimitive(month));
//        hourlyView.add("day", new JsonPrimitive(day));
//        hourlyView.add("hour", new JsonPrimitive(hour));
//
//        SummaryStatistics dailyStats = database.getHourlyStats(year, month, day, hour);
//        if (dailyStats != null) {
//            hourlyView.add("total", new JsonPrimitive(dailyStats.getCount()));
//        }
//
//        JsonObject patternLookup = getPatternLookup();
//        hourlyView.add("patternLookup", patternLookup);
//
//        long max = 0;
//
//        JsonArray segments = new JsonArray();
//        int seg = 6;
//        int row = 10;
//        for (int i = 0; i < seg; i++) {
//
//            JsonObject segment = new JsonObject();
//            JsonArray minutes = new JsonArray();
//
//            for (int j = 0; j < row; j++) {
//                JsonObject minute = new JsonObject();
//
//                int minuteIndex = (i * row) + j;
//                minute.add("minute", new JsonPrimitive(minuteIndex));
//
//                SummaryStatistics minutelyStats = database.getMinutelyStats(year, month, day, hour, minuteIndex);
//                if (minutelyStats != null) {
//                    long count = minutelyStats.getCount();
//                    minute.add("total", new JsonPrimitive(count));
//
//                    JsonObject patterns = getPatternCounts(minutelyStats);
//                    minute.add("patterns", patterns);
//
//                    max = Math.max(max, count);
//                }
//
//                minutes.add(minute);
//            }
//
//            segment.add("minutes", minutes);
//            segments.add(segment);
//        }
//
//        addHeatmapColours(max, segments, seg, row, "minutes");
//
//        hourlyView.add("segments", segments);
//
//        return hourlyView;
    }


    private JsonObject getPatternLookup() {
        JsonObject patternLookup = new JsonObject();

        ObservableList<Pattern> patternsSortedById = patternManager.getPatterns().getValue();
        for (Pattern pattern : patternsSortedById) {
            patternLookup.add("" + pattern.getPatternId(), new JsonPrimitive(pattern.getName()));
        }
        return patternLookup;
    }

    private JsonObject getPatternCounts(SummaryStatistics millisecondlyStats) {

        JsonObject patterns = new JsonObject();

        //        JsonArray patterns = new JsonArray();
        Map<Integer, Long> patternCounts = millisecondlyStats.getPatternCounts();
        for (Entry<Integer, Long> entry : patternCounts.entrySet()) {
            int patternId = entry.getKey();
            long patternCount = entry.getValue();

            patterns.add("" + patternId, new JsonPrimitive(patternCount));

            //            JsonObject pattern = new JsonObject();
            //            pattern.add("patternId", new JsonPrimitive(patternId));
            //            pattern.add("count", new JsonPrimitive(patternCount));

            //            patterns.add(pattern);
        }
        return patterns;
    }

    private void addHeatmapColours(double max, JsonArray segments, int seg, int row, String name) {
        ColourInterpolation interp = new ColourInterpolation(Color.white, Color.blue);

        for (int i = 0; i < seg; i++) {
            for (int j = 0; j < row; j++) {
                JsonObject second = segments.get(i).getAsJsonObject().get(name).getAsJsonArray().get(j).getAsJsonObject();
                long total;
                if (second.has("total")) {
                    total = second.get("total").getAsLong();
                } else {
                    total = 0;
                }

                if (total > 0) {
                    double factor = total / max;
                    Color color = interp.interpolate(factor);

                    int mag = (color.getBlue() + color.getGreen() + color.getRed()) / 3;
                    if (mag < (128)) {
                        second.add("textcolour", new JsonPrimitive("white"));
                    } else {
                        second.add("textcolour", new JsonPrimitive("black"));
                    }

                    String hexString = ColourUtils.toHex(color);
                    second.add("colour", new JsonPrimitive("#" + hexString));
                } else {
                    second.add("colour", new JsonPrimitive("#dddddd"));
                }
            }
        }
    }

    public JsonObject getMinutely(@Param(name = "year") int year,
                                  @Param(name = "month") int month,
                                  @Param(name = "day") int day,
                                  @Param(name = "hour") int hour,
                                  @Param(name = "minute") int minute,
                                  @Param(name = "independentPatternHeat") boolean independentPatternHeat,
                                  @Param(name = "useTotalScale") boolean useTotalScale) {

        int columns = 6;
        int rows = 10;
        long interval = TimeUtils.minutes;
        long subInterval = TimeUtils.seconds;
        TimeKey timeKey = TimeKey.from(year, month, day, hour, minute);
        String viewName = "minutely";

        return createView(independentPatternHeat, useTotalScale, columns, rows, interval, subInterval, timeKey, viewName);
    }

    private JsonObject createView(@Param(name = "independentPatternHeat") boolean independentPatternHeat,
                                  @Param(name = "useTotalScale") boolean useTotalScale,
                                  int columns,
                                  int rows,
                                  long interval,
                                  long subInterval,
                                  TimeKey timeKey,
                                  String viewName) {
        logger.info("Creating {} view for key='{}' independentPatternHeat='{}' useTotalScale='{}'",
                    viewName,
                    timeKey,
                    independentPatternHeat,
                    useTotalScale);

        JsonObject view = new JsonObject();

        view.add("timeKey", gson.toJsonTree(timeKey));

        SummaryStatistics summaryStats = database.getIntervalStats(interval, timeKey);
        if (summaryStats != null) {
            view.add("total", new JsonPrimitive(summaryStats.getCount()));
        }

        List<SummaryStatistics> secondStats = databaseHelper.query(timeKey, subInterval, columns * rows);

        long commonMax = 0;
        List<SingleSeriesSummaryStatistics> overall = databaseHelper.extractOverallStats(secondStats);

        JsonArray overallHeatmap = buildHeatmap(overall, columns, rows);
        JsonObject patternHeatmaps = new JsonObject();

        if (!independentPatternHeat) {
            if (useTotalScale) {
                for (SingleSeriesSummaryStatistics singleSeriesSummaryStatistics : overall) {
                    commonMax = Math.max(commonMax, singleSeriesSummaryStatistics.getCount());
                }
            } else {
                for (SummaryStatistics stat : secondStats) {
                    Map<Integer, Long> patternCounts = stat.getPatternCounts();
                    for (Long aLong : patternCounts.values()) {
                        commonMax = Math.max(commonMax, aLong);
                    }
                }
            }
        }

        for (Pattern pattern : getPatternList()) {
            List<SingleSeriesSummaryStatistics> patternStats = databaseHelper.extractPatternStats(pattern.getPatternId(), secondStats);
            JsonArray patternHeatmap;

            if (independentPatternHeat) {
                patternHeatmap = buildHeatmap(patternStats, columns, rows);
            } else {
                patternHeatmap = buildHeatmap(patternStats, columns, rows, commonMax);
            }
            patternHeatmaps.add(Integer.toString(pattern.getPatternId()), patternHeatmap);
        }

        JsonObject patternLookup = getPatternLookup();

        view.add("overall", overallHeatmap);
        view.add("patterns", patternHeatmaps);
        view.add("patternLookup", patternLookup);

        return view;
    }

    private JsonArray buildHeatmap(List<SingleSeriesSummaryStatistics> stats, int columns, int row) {

        long max = 0;
        for (SingleSeriesSummaryStatistics stat : stats) {
            max = Math.max(max, stat.getCount());
        }

        return buildHeatmap(stats, columns, row, max);
    }

    private List<Pattern> getPatternList() {
        return patternManager.getPatterns().getValue();
    }

    private JsonArray buildHeatmap(List<SingleSeriesSummaryStatistics> stats, int columns, int row, long max) {

        ColourInterpolation interp = new ColourInterpolation(Color.white, Color.blue);

        JsonArray rowsArray = new JsonArray();
        for (int i = 0; i < columns; i++) {

            JsonObject columnObject = new JsonObject();
            JsonArray cells = new JsonArray();

            for (int j = 0; j < row; j++) {
                JsonObject cell = new JsonObject();

                int index = (i * row) + j;
                cell.add("index", new JsonPrimitive(index));

                SingleSeriesSummaryStatistics item = stats.get(index);

                long count = item.getCount();
                cell.add("total", new JsonPrimitive(count));

                if (count > 0) {
                    double factor = count / (double) max;
                    Color color = interp.interpolate(factor);

                    int mag = (color.getBlue() + color.getGreen() + color.getRed()) / 3;
                    if (mag < (128)) {
                        cell.add("textcolour", new JsonPrimitive("white"));
                    } else {
                        cell.add("textcolour", new JsonPrimitive("black"));
                    }

                    String hexString = ColourUtils.toHex(color);
                    cell.add("colour", new JsonPrimitive("#" + hexString));
                } else {
                    cell.add("colour", new JsonPrimitive("#dddddd"));
                }


                cells.add(cell);
            }

            columnObject.add("cells", cells);
            rowsArray.add(columnObject);
        }

        return rowsArray;
    }

    public String getPatterns() {
        logger.info("Getting patterns");
        Result<ObservableList<Pattern>> patterns = patternManager.getPatterns();
        return toJSON(patterns);
    }

    public JsonObject getSecondly(@Param(name = "year") int year,
                                  @Param(name = "month") int month,
                                  @Param(name = "day") int day,
                                  @Param(name = "hour") int hour,
                                  @Param(name = "minute") int minute,
                                  @Param(name = "second") int second) {

        logger.info("Creating secondly view for year='{}' month='{}' day='{}' hour='{}' minute='{}' second='{}'",
                    year,
                    month,
                    day,
                    hour,
                    minute,
                    second);

        JsonObject secondlyView = new JsonObject();

        secondlyView.add("year", new JsonPrimitive(year));
        secondlyView.add("month", new JsonPrimitive(month));
        secondlyView.add("day", new JsonPrimitive(day));
        secondlyView.add("hour", new JsonPrimitive(hour));
        secondlyView.add("minute", new JsonPrimitive(minute));
        secondlyView.add("second", new JsonPrimitive(second));

        SummaryStatistics secondlyStats = database.getSecondlyStats(year, month, day, hour, minute, second);
        if (secondlyStats != null) {
            secondlyView.add("total", new JsonPrimitive(secondlyStats.getCount()));
        }

        JsonObject patternLookup = getPatternLookup();
        secondlyView.add("patternLookup", patternLookup);

        long max = 0;

        JsonArray segments = new JsonArray();
        int seg = 50;
        int row = 20;
        for (int i = 0; i < seg; i++) {

            JsonObject segment = new JsonObject();
            JsonArray milliseconds = new JsonArray();

            for (int j = 0; j < row; j++) {
                JsonObject millisecond = new JsonObject();

                int index = (i * row) + j;
                millisecond.add("millisecond", new JsonPrimitive(index));

                SummaryStatistics millisecondlyStats = database.getMillisecondlyStats(year, month, day, hour, minute, second, index);
                if (millisecondlyStats != null) {
                    long count = millisecondlyStats.getCount();
                    millisecond.add("total", new JsonPrimitive(count));

                    JsonObject patterns = getPatternCounts(millisecondlyStats);
                    millisecond.add("patterns", patterns);
                    max = Math.max(max, count);
                }

                milliseconds.add(millisecond);
            }

            segment.add("milliseconds", milliseconds);
            segments.add(segment);
        }

        addHeatmapColours(max, segments, seg, row, "milliseconds");

        //        JsonArray patternHeatmap = buildPatternHeatmap(0, seg, row);

        secondlyView.add("segments", segments);

        return secondlyView;
    }

    //    public Object handle(String url) {
    //        logger.info("Handling url '{}'", url);
    //
    //        String[] split = url.split("/");
    //
    //        String service = split[0];
    //
    //        String classname = "com.logging.modules." + StringUtils.capitalise(service);
    //
    //        try {
    //            Object instance = serviceDiscovery.findService(Class.forName(classname));
    //
    //
    //
    //        } catch (ClassNotFoundException e) {
    //            return e.getMessage();
    //        }
    //
    //        return "Hello";
    //    }

    public String hourstats(@Param(name = "year") int year,
                            @Param(name = "month") int month,
                            @Param(name = "day") int day,
                            @Param(name = "hour") int hour) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);

        DataController.MonthlyData monthlyData = yearlyData.getSubPeriodData().get(DataController.PeriodKey.month(year, month));
        DataController.DailyData dailyData = monthlyData.getSubPeriodData().get(DataController.PeriodKey.dayOfMonth(year, month, day));
        DataController.HourlyData hourData = dailyData.getSubPeriodData().get(DataController.PeriodKey.hour(year, month, day, hour));

        Collection<DataController.MinuteData> values = hourData.getSubPeriodData().values();
        List<DataController.MinuteData> sorted = new ArrayList<DataController.MinuteData>(values);
        Collections.sort(sorted, new Comparator<DataController.MinuteData>() {
            @Override
            public int compare(DataController.MinuteData o1, DataController.MinuteData o2) {
                return CompareUtils.compare(o1.getKey(), o2.getKey());
            }
        });


        for (DataController.MinuteData minute : sorted) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("minute", new JsonPrimitive(minute.getKey().minute));
            itemObject.add("hour", new JsonPrimitive(minute.getKey().hour));
            itemObject.add("day", new JsonPrimitive(minute.getKey().dayOfMonth));
            itemObject.add("month", new JsonPrimitive(minute.getKey().month));
            itemObject.add("year", new JsonPrimitive(minute.getKey().year));
            itemObject.add("events", new JsonPrimitive(minute.getCount()));

            SinglePassStatisticsDoublePrecision[] statsArray = minute.getNumericStats();
            for (SinglePassStatisticsDoublePrecision stats : statsArray) {
                if (stats != null) {

                    JsonObject statsObject = new JsonObject();
                    JsonArray percentilesArray = new JsonArray();

                    stats.doCalculations();

                    statsObject.add("count", new JsonPrimitive(stats.getCount()));
                    statsObject.add("mean", new JsonPrimitive(stats.getMean()));
                    statsObject.add("min", new JsonPrimitive(stats.getMinimum()));
                    statsObject.add("max", new JsonPrimitive(stats.getMaximum()));

                    double[] percentiles = stats.getPercentiles();
                    percentilesArray.add(new JsonPrimitive(percentiles[0]));
                    percentilesArray.add(new JsonPrimitive(percentiles[10]));
                    percentilesArray.add(new JsonPrimitive(percentiles[20]));
                    percentilesArray.add(new JsonPrimitive(percentiles[30]));
                    percentilesArray.add(new JsonPrimitive(percentiles[40]));
                    percentilesArray.add(new JsonPrimitive(percentiles[50]));
                    percentilesArray.add(new JsonPrimitive(percentiles[60]));
                    percentilesArray.add(new JsonPrimitive(percentiles[70]));
                    percentilesArray.add(new JsonPrimitive(percentiles[80]));
                    percentilesArray.add(new JsonPrimitive(percentiles[90]));
                    percentilesArray.add(new JsonPrimitive(percentiles[100]));
                    percentilesArray.add(new JsonPrimitive(percentiles[95]));
                    percentilesArray.add(new JsonPrimitive(percentiles[98]));
                    percentilesArray.add(new JsonPrimitive(percentiles[99]));

                    statsObject.add("percentiles", percentilesArray);

                    // TODO: should be an array for multiple numeric values in the pattern
                    itemObject.add("patternStats", statsObject);

                }
            }

            array.add(itemObject);
        }

        object.add("minutes", array);

        return object.toString();
    }

    public String logon(Map<String, String[]> parameters) {
        String userName = parameters.get("userName")[0];
        String password = parameters.get("password")[0];

        logger.info("Logon attempted for userName '{}'", userName);

        JSONWriter writer = new JSONWriter();
        writer.startElement();

        Result<AuthenticationResult> result = authenticate(userName, password);
        if (result.getState() == Result.State.Successful) {
            String sessionID = UUID.randomUUID().toString();
            sessions.put(sessionID, result.getValue());
            writer.writeProperty("success", true);
            writer.writeProperty("sessionID", sessionID);
            writer.writeProperty("userName", userName);

            RequestContext.getRequestContext().addCookie("sessionID", sessionID);
        } else {
            writer.writeProperty("success", false);
            writer.writeProperty("reason", result.getExternalReason());
        }

        writer.endElement();
        return writer.toString();
    }

    private Result<AuthenticationResult> authenticate(String userName, String password) {
        Result<AuthenticationResult> result = new Result<AuthenticationResult>(new AuthenticationResult(userName));
        return result;
    }

    public String minutestats(@Param(name = "year") int year,
                              @Param(name = "month") int month,
                              @Param(name = "day") int day,
                              @Param(name = "hour") int hour,
                              @Param(name = "minute") int minute) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);

        DataController.MonthlyData monthlyData = yearlyData.getSubPeriodData().get(DataController.PeriodKey.month(year, month));
        DataController.DailyData dailyData = monthlyData.getSubPeriodData().get(DataController.PeriodKey.dayOfMonth(year, month, day));
        DataController.HourlyData hourData = dailyData.getSubPeriodData().get(DataController.PeriodKey.hour(year, month, day, hour));
        DataController.MinuteData minuteData = hourData.getSubPeriodData().get(DataController.PeriodKey.minute(year, month, day, hour, minute));

        Collection<DataController.SecondData> values = minuteData.getSubPeriodData().values();
        List<DataController.SecondData> sorted = new ArrayList<DataController.SecondData>(values);
        Collections.sort(sorted, new Comparator<DataController.SecondData>() {
            @Override
            public int compare(DataController.SecondData o1, DataController.SecondData o2) {
                return CompareUtils.compare(o1.getKey(), o2.getKey());
            }
        });

        for (DataController.SecondData second : sorted) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("second", new JsonPrimitive(second.getKey().second));
            itemObject.add("minute", new JsonPrimitive(second.getKey().minute));
            itemObject.add("hour", new JsonPrimitive(second.getKey().hour));
            itemObject.add("day", new JsonPrimitive(second.getKey().dayOfMonth));
            itemObject.add("month", new JsonPrimitive(second.getKey().month));
            itemObject.add("year", new JsonPrimitive(second.getKey().year));
            itemObject.add("events", new JsonPrimitive(second.getCount()));

            SinglePassStatisticsDoublePrecision[] statsArray = second.getNumericStats();
            for (SinglePassStatisticsDoublePrecision stats : statsArray) {
                if (stats != null) {

                    JsonObject statsObject = new JsonObject();
                    JsonArray percentilesArray = new JsonArray();

                    stats.doCalculations();

                    statsObject.add("count", new JsonPrimitive(stats.getCount()));
                    statsObject.add("mean", new JsonPrimitive(stats.getMean()));
                    statsObject.add("min", new JsonPrimitive(stats.getMinimum()));
                    statsObject.add("max", new JsonPrimitive(stats.getMaximum()));

                    double[] percentiles = stats.getPercentiles();
                    percentilesArray.add(new JsonPrimitive(percentiles[0]));
                    percentilesArray.add(new JsonPrimitive(percentiles[10]));
                    percentilesArray.add(new JsonPrimitive(percentiles[20]));
                    percentilesArray.add(new JsonPrimitive(percentiles[30]));
                    percentilesArray.add(new JsonPrimitive(percentiles[40]));
                    percentilesArray.add(new JsonPrimitive(percentiles[50]));
                    percentilesArray.add(new JsonPrimitive(percentiles[60]));
                    percentilesArray.add(new JsonPrimitive(percentiles[70]));
                    percentilesArray.add(new JsonPrimitive(percentiles[80]));
                    percentilesArray.add(new JsonPrimitive(percentiles[90]));
                    percentilesArray.add(new JsonPrimitive(percentiles[100]));
                    percentilesArray.add(new JsonPrimitive(percentiles[95]));
                    percentilesArray.add(new JsonPrimitive(percentiles[98]));
                    percentilesArray.add(new JsonPrimitive(percentiles[99]));

                    statsObject.add("percentiles", percentilesArray);

                    // TODO: should be an array for multiple numeric values in the pattern
                    itemObject.add("patternStats", statsObject);

                }
            }

            array.add(itemObject);
        }

        object.add("seconds", array);

        return object.toString();
    }

    public String monthstats(@Param(name = "year") int year, @Param(name = "month") int month) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);
        DataController.PeriodKey key = DataController.PeriodKey.month(year, month);

        DataController.MonthlyData subPeriodData = yearlyData.getSubPeriodData().get(key);

        Collection<DataController.DailyData> values = subPeriodData.getSubPeriodData().values();
        List<DataController.DailyData> sorted = new ArrayList<DataController.DailyData>(values);
        Collections.sort(sorted, new Comparator<DataController.DailyData>() {
            @Override
            public int compare(DataController.DailyData o1, DataController.DailyData o2) {
                return CompareUtils.compare(o1.getKey(), o2.getKey());
            }
        });

        for (DataController.DailyData day : sorted) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("day", new JsonPrimitive(day.getKey().dayOfMonth));
            itemObject.add("month", new JsonPrimitive(day.getKey().month));
            itemObject.add("year", new JsonPrimitive(day.getKey().year));
            itemObject.add("events", new JsonPrimitive(day.getCount()));

            SinglePassStatisticsDoublePrecision[] statsArray = day.getNumericStats();
            for (SinglePassStatisticsDoublePrecision stats : statsArray) {
                if (stats != null) {

                    JsonObject statsObject = new JsonObject();
                    JsonArray percentilesArray = new JsonArray();

                    stats.doCalculations();

                    statsObject.add("count", new JsonPrimitive(stats.getCount()));
                    statsObject.add("mean", new JsonPrimitive(stats.getMean()));
                    statsObject.add("min", new JsonPrimitive(stats.getMinimum()));
                    statsObject.add("max", new JsonPrimitive(stats.getMaximum()));

                    double[] percentiles = stats.getPercentiles();
                    percentilesArray.add(new JsonPrimitive(percentiles[0]));
                    percentilesArray.add(new JsonPrimitive(percentiles[10]));
                    percentilesArray.add(new JsonPrimitive(percentiles[20]));
                    percentilesArray.add(new JsonPrimitive(percentiles[30]));
                    percentilesArray.add(new JsonPrimitive(percentiles[40]));
                    percentilesArray.add(new JsonPrimitive(percentiles[50]));
                    percentilesArray.add(new JsonPrimitive(percentiles[60]));
                    percentilesArray.add(new JsonPrimitive(percentiles[70]));
                    percentilesArray.add(new JsonPrimitive(percentiles[80]));
                    percentilesArray.add(new JsonPrimitive(percentiles[90]));
                    percentilesArray.add(new JsonPrimitive(percentiles[100]));
                    percentilesArray.add(new JsonPrimitive(percentiles[95]));
                    percentilesArray.add(new JsonPrimitive(percentiles[98]));
                    percentilesArray.add(new JsonPrimitive(percentiles[99]));

                    statsObject.add("percentiles", percentilesArray);

                    // TODO: should be an array for multiple numeric values in the pattern
                    itemObject.add("patternStats", statsObject);

                }
            }

            array.add(itemObject);
        }

        object.add("days", array);

        return object.toString();
    }

    protected Future<Boolean> removeHubSubscription(String channel) {
        logger.info("Removing subscription to hub channel '{}'", channel);
        Destination<ChannelMessage> destination = hubSubscriptionCounterparts.remove(channel);
        channelMessaging.unsubscribe(channel, destination);
        return null;
    }

    public String secondstats(@Param(name = "year") int year,
                              @Param(name = "month") int month,
                              @Param(name = "day") int day,
                              @Param(name = "hour") int hour,
                              @Param(name = "minute") int minute,
                              @Param(name = "second") int second) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);

        DataController.MonthlyData monthlyData = yearlyData.getSubPeriodData().get(DataController.PeriodKey.month(year, month));
        DataController.DailyData dailyData = monthlyData.getSubPeriodData().get(DataController.PeriodKey.dayOfMonth(year, month, day));
        DataController.HourlyData hourData = dailyData.getSubPeriodData().get(DataController.PeriodKey.hour(year, month, day, hour));
        DataController.MinuteData minuteData = hourData.getSubPeriodData().get(DataController.PeriodKey.minute(year, month, day, hour, minute));
        DataController.SecondData secondData = minuteData.getSubPeriodData()
                                                         .get(DataController.PeriodKey.second(year, month, day, hour, minute, second));

        if (secondData != null) {
            List<PatternisedLogEvent> data = secondData.getData();
            Collections.sort(data, new Comparator<PatternisedLogEvent>() {
                @Override
                public int compare(PatternisedLogEvent o1, PatternisedLogEvent o2) {
                    return CompareUtils.compare(o1.getTime(), o2.getTime());
                }
            });

            Gson gson = new Gson();
            for (PatternisedLogEvent event : data) {

                JsonElement element = gson.toJsonTree(event);

                array.add(element);
            }
        }

        object.add("events", array);

        return object.toString();
    }

    public void setChannelMessaging(ChannelMessagingService channelMessaging) {
        this.channelMessaging = channelMessaging;
    }

    public void setPatternManager(PatternManagerService patternManager) {
        this.patternManager = patternManager;
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    // private List<Connection> getSubscriptions(String channel) {
    // List<Connection> list;
    // synchronized (subscriptions) {
    // list = subscriptions.get(channel);
    // if (list == null) {
    // list = new CopyOnWriteArrayList<Connection>();
    // subscriptions.put(channel, list);
    // }
    // }
    // return list;
    //
    // }

    @Override
    public void setWebSocketHelper(WebSocketHelper helper) {
        this.webSocketHelper = helper;

        webSocketHelper.addListener(new WebSocketListener() {
            @Override
            public void onClosed(Connection connection, int closeCode, String message) {

            }

            @Override
            public void onOpen(Connection connection) {

            }

            @Override
            public void onMessage(final Connection connection, String data) {

                logger.info("Handling websockets message : {}", data);

                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(data).getAsJsonObject();

                String action = jsonObject.get("action").toString();

                if (action.equals("subscribe")) {

                    String channel = jsonObject.get("channel").toString();

                    KeyedFactory<Connection, Destination<String>> factory = new KeyedFactory<Connection, Destination<String>>() {

                        @Override
                        public Destination<String> create(Connection key) {
                            return new Destination<String>() {
                                @Override
                                public void send(String data) {
                                    try {
                                        connection.sendMessage(data);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                ;
                            };

                        }
                    };

                    Destination<String> destination = subscriptionCounterparts.create(connection, factory);
                    subscriptions.addSubscription(channel, destination);

                    JsonObject response = new JsonObject();
                    response.addProperty("requestID", jsonObject.get("requestID").toString());
                    response.addProperty("reason", "");
                    response.addProperty("state", Result.State.Successful.toString());
                    response.addProperty("value", true);

                    try {
                        connection.sendMessage(response.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (action.equals("unsubscribe")) {

                    String channel = jsonObject.get("channel").toString();
                    subscriptions.removeSubscription(channel, subscriptionCounterparts.remove(connection));

                    JsonObject response = new JsonObject();
                    response.addProperty("requestID", jsonObject.get("requestID").toString());
                    response.addProperty("reason", "");
                    response.addProperty("state", Result.State.Successful.toString());
                    response.addProperty("value", true);

                    try {
                        connection.sendMessage(response.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String stats() {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        List<DataController.YearlyData> years = dataController.getYears();
        for (DataController.YearlyData year : years) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("year", new JsonPrimitive(year.getKey().year));
            itemObject.add("events", new JsonPrimitive(year.getCount()));

            array.add(itemObject);
        }

        object.add("years", array);

        return object.toString();
    }

    public String yearstats(@Param(name = "year") int year) {
        JsonObject object = new JsonObject();

        JsonArray array = new JsonArray();

        DataController.YearlyData yearlyData = dataController.getYear(year);
        Collection<DataController.MonthlyData> values = yearlyData.getSubPeriodData().values();
        List<DataController.MonthlyData> sorted = new ArrayList<DataController.MonthlyData>(values);
        Collections.sort(sorted, new Comparator<DataController.MonthlyData>() {
            @Override
            public int compare(DataController.MonthlyData o1, DataController.MonthlyData o2) {
                return CompareUtils.compare(o1.getKey(), o2.getKey());
            }
        });

        for (DataController.MonthlyData month : sorted) {

            JsonObject itemObject = new JsonObject();

            itemObject.add("month", new JsonPrimitive(month.getKey().month));
            itemObject.add("year", new JsonPrimitive(month.getKey().year));
            itemObject.add("events", new JsonPrimitive(month.getCount()));

            array.add(itemObject);
        }

        object.add("months", array);

        return object.toString();
    }
}
