package com.logginghub.logging.modules.web;

import com.google.gson.*;
import com.logginghub.analytics.model.LongFrequencyCount;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.NonArrayChannelMessage;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SubscriptionController;
import com.logginghub.logging.modules.PatternManagerService;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.*;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Counterparts;
import com.logginghub.web.*;
import org.eclipse.jetty.websocket.WebSocket.Connection;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

@WebController(staticFiles = "/logginghubweb/")
public class WebFrontendController implements WebSocketSupport {

    private static final Logger logger = Logger.getLoggerFor(WebFrontendController.class);
    private Map<String, AuthenticationResult> sessions = new HashMap<String, AuthenticationResult>();

    // private Map<String, List<Connection>> subscriptions = new HashMap<String,
    // List<Connection>>();

    private SubscriptionController<Destination<String>, String> subscriptions = new SubscriptionController<Destination<String>, String>() {
        @Override
        protected Future<Boolean> handleLastSubscription(String channel) {
            return removeHubSubscription(channel);
        }

        @Override
        protected Future<Boolean> handleFirstSubscription(String channel) {
            return createHubSubscription(channel);
        }
    };

    private Counterparts<String, Destination<ChannelMessage>> hubSubscriptionCounterparts = new Counterparts<String, Destination<ChannelMessage>>();

    private Counterparts<Connection, Destination<String>> subscriptionCounterparts = new Counterparts<Connection, Destination<String>>();

    private WebSocketHelper webSocketHelper;
    private PatternManagerService patternManager;
    private ChannelMessagingService channelMessaging;

    private DataController dataController = new DataController();

    public WebFrontendController() {
        WorkerThread.execute("Test data reader", new Runnable() {
            @Override
            public void run() {
                dataController.loadData();
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
                    frequencyObject.add("value", new JsonPrimitive(sortedValue.key));
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

    protected Future<Boolean> removeHubSubscription(String channel) {
        logger.info("Removing subscription to hub channel '{}'", channel);
        Destination<ChannelMessage> destination = hubSubscriptionCounterparts.remove(channel);
        channelMessaging.unsubscribe(channel, destination);
        return null;
    }

    public String getPatterns() {
        logger.info("Getting patterns");
        Result<ObservableList<Pattern>> patterns = patternManager.getPatterns();
        return toJSON(patterns);
    }

    public String getAggregations() {
        logger.info("Getting aggregations");
        Result<ObservableList<Aggregation>> aggregations = patternManager.getAggregations();
        return toJSON(aggregations);
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

    public String createPattern(@Param(name = "name") String name, @Param(name = "pattern") String pattern) {
        logger.info("Create pattern name '{}' pattern '{}'", name, pattern);
        Result<Pattern> patternModel = patternManager.createPattern(name, pattern);
        return toJSON(patternModel);
    }

    private String toJSON(Object object) {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        System.out.println(json);
        return json;
    }

    private Result<AuthenticationResult> authenticate(String userName, String password) {
        Result<AuthenticationResult> result = new Result<AuthenticationResult>(new AuthenticationResult(userName));
        return result;
    }

    @Override
    public void setWebSocketHelper(WebSocketHelper helper) {
        this.webSocketHelper = helper;

        webSocketHelper.addListener(new WebSocketListener() {
            @Override
            public void onClosed(Connection connection, int closeCode, String message) {

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

            @Override
            public void onOpen(Connection connection) {

            }
        });
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

    public void setPatternManager(PatternManagerService patternManager) {
        this.patternManager = patternManager;
    }

    public void setChannelMessaging(ChannelMessagingService channelMessaging) {
        this.channelMessaging = channelMessaging;
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
}
