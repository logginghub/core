package com.logginghub.logging.modules.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.eclipse.jetty.websocket.WebSocket.Connection;

import com.google.gson.Gson;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.interfaces.ChannelMessagingService;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.NonArrayChannelMessage;
import com.logginghub.logging.messaging.SubscriptionController;
import com.logginghub.logging.modules.PatternManagerService;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.Destination;
import com.logginghub.utils.JSONWriter;
import com.logginghub.utils.KeyedFactory;
import com.logginghub.utils.Result;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Counterparts;
import com.logginghub.web.Param;
import com.logginghub.web.RequestContext;
import com.logginghub.web.WebController;
import com.logginghub.web.WebSocketHelper;
import com.logginghub.web.WebSocketListener;
import com.logginghub.web.WebSocketSupport;

@WebController(staticFiles = "/logginghubweb/") public class WebFrontendController implements WebSocketSupport {

    private static final Logger logger = Logger.getLoggerFor(WebFrontendController.class);
    private Map<String, AuthenticationResult> sessions = new HashMap<String, AuthenticationResult>();

    // private Map<String, List<Connection>> subscriptions = new HashMap<String,
    // List<Connection>>();

    private SubscriptionController<Destination<String>, String> subscriptions = new SubscriptionController<Destination<String>, String>() {
        @Override protected Future<Boolean> handleLastSubscription(String channel) {
            return removeHubSubscription(channel);
        }

        @Override protected Future<Boolean> handleFirstSubscription(String channel) {
            return createHubSubscription(channel);
        }
    };

    private Counterparts<String, Destination<ChannelMessage>> hubSubscriptionCounterparts = new Counterparts<String, Destination<ChannelMessage>>();

    private Counterparts<Connection, Destination<String>> subscriptionCounterparts = new Counterparts<Connection, Destination<String>>();

    private WebSocketHelper webSocketHelper;
    private PatternManagerService patternManager;
    private ChannelMessagingService channelMessaging;

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
        }
        else {
            writer.writeProperty("success", false);
            writer.writeProperty("reason", result.getExternalReason());
        }

        writer.endElement();
        return writer.toString();
    }

    protected Future<Boolean> createHubSubscription(String channel) {

        logger.info("Creating subscription to hub channel '{}'", channel);

        Destination<ChannelMessage> destination = new Destination<ChannelMessage>() {
            @Override public void send(ChannelMessage t) {
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

    @Override public void setWebSocketHelper(WebSocketHelper helper) {
        this.webSocketHelper = helper;

        webSocketHelper.addListener(new WebSocketListener() {
            @Override public void onClosed(Connection connection, int closeCode, String message) {

            }

            @Override public void onMessage(final Connection connection, String data) {

                logger.info("Handling websockets message : {}", data);
                Object parse = JSONValue.parse(data);
                if (parse instanceof JSONObject) {

                    JSONObject jsonObject = (JSONObject) parse;

                    String action = jsonObject.get("action").toString();

                    if (action.equals("subscribe")) {

                        String channel = jsonObject.get("channel").toString();

                        KeyedFactory<Connection, Destination<String>> factory = new KeyedFactory<Connection, Destination<String>>() {

                            @Override public Destination<String> create(Connection key) {
                                return new Destination<String>() {
                                    @Override public void send(String data) {
                                        try {
                                            connection.sendMessage(data);
                                        }
                                        catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    };
                                };

                            }
                        };

                        Destination<String> destination = subscriptionCounterparts.create(connection, factory);
                        subscriptions.addSubscription(channel, destination);

                        JSONObject response = new JSONObject();
                        response.put("requestID", jsonObject.get("requestID"));
                        response.put("reason", "");
                        response.put("state", Result.State.Successful);
                        response.put("value", true);

                        try {
                            connection.sendMessage(response.toJSONString());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (action.equals("unsubscribe")) {

                        String channel = jsonObject.get("channel").toString();
                        subscriptions.removeSubscription(channel, subscriptionCounterparts.remove(connection));

                        JSONObject response = new JSONObject();
                        response.put("requestID", jsonObject.get("requestID"));
                        response.put("reason", "");
                        response.put("state", Result.State.Successful);
                        response.put("value", true);

                        try {
                            connection.sendMessage(response.toJSONString());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override public void onOpen(Connection connection) {

            }
        });
    }

    public void broadcastEvent(LogEvent event) {
        String channel = "events";

        if (subscriptions.hasSubscriptions(channel)) {
            // TODO :use gson?
            JSONObject eventJSON = toJSON(event);

            JSONObject broadcastJSON = new JSONObject();
            broadcastJSON.put("channel", channel);
            broadcastJSON.put("value", eventJSON);

            String json = broadcastJSON.toJSONString();

            subscriptions.dispatch(Channels.toArray(channel), json);
        }
    }

    private JSONObject toJSON(LogEvent event) {
        JSONObject eventJSON = new JSONObject();
        eventJSON.put("channel", event.getChannel());
        eventJSON.put("formattedException", event.getFormattedException());
        eventJSON.put("level", event.getLevel());
        eventJSON.put("levelDescription", event.getLevelDescription());
        eventJSON.put("time", event.getOriginTime());
        eventJSON.put("loggerName", event.getLoggerName());
        eventJSON.put("message", event.getMessage());
        eventJSON.put("pid", event.getPid());
        eventJSON.put("sequenceNumber", event.getSequenceNumber());
        eventJSON.put("sourceAddress", event.getSourceAddress());
        eventJSON.put("sourceApplication", event.getSourceApplication());
        eventJSON.put("sourceClassName", event.getSourceClassName());
        eventJSON.put("sourceHost", event.getSourceHost());
        eventJSON.put("sourceMethodName", event.getSourceMethodName());
        eventJSON.put("threadName", event.getThreadName());
        return eventJSON;
    }

    private void broadcastAndRemoveFailures(List<Connection> list, String message) {
        List<Connection> failures = new ArrayList<Connection>();
        for (Connection connection : list) {
            try {
                logger.fine("Sending to connection '{}' : {}", connection, message);
                connection.sendMessage(message);
            }
            catch (IOException e) {
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
