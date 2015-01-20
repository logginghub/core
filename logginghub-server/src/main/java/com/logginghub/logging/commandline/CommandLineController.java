package com.logginghub.logging.commandline;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import jline.console.ConsoleReader;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternManagementAPI;
import com.logginghub.logging.commandline.AnsiColourHelper.AnsiColour;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.listeners.LoggingMessageListener;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.HealthCheckRequest;
import com.logginghub.logging.messages.HealthCheckResponse;
import com.logginghub.logging.messages.HistoricalAggregatedDataRequest;
import com.logginghub.logging.messages.HistoricalAggregatedDataResponse;
import com.logginghub.logging.messages.HistoricalDataRequest;
import com.logginghub.logging.messages.HistoricalDataResponse;
import com.logginghub.logging.messages.HistoricalPatternisedDataRequest;
import com.logginghub.logging.messages.HistoricalPatternisedDataResponse;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.messaging.SocketClient;
import com.logginghub.logging.messaging.SocketClientManager;
import com.logginghub.logging.messaging.SocketClientManager.State;
import com.logginghub.logging.messaging.SocketClientManagerListener;
import com.logginghub.utils.Destination;
import com.logginghub.utils.MutableInt;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.Result;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

public abstract class CommandLineController {

    private Map<String, CommandHandler> handlers = new HashMap<String, CommandHandler>();
    private SocketClient client;

    private Map<String, Destination<ChannelMessage>> channelDestinations = new HashMap<String, Destination<ChannelMessage>>();

    private FilterValues filterValues = new FilterValues();

    private boolean isPlayingRealtime = true;

    public CommandLineController() {
        handlers.put("connect", new CommandHandler() {

            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {

                client = new SocketClient();

                InetSocketAddress address = NetUtils.toInetSocketAddress(arguments[0], VLPorts.getSocketHubDefaultPort());

                append("Connecting to hub at '{}'...", address);

                client.addConnectionPoint(address);

                boolean autosubscribe = true;
                if (arguments.length > 1) {
                    autosubscribe = Boolean.parseBoolean(arguments[1]);
                    client.setAutoSubscribe(autosubscribe);
                }

                client.addLogEventListener(new LogEventListener() {
                    @Override public void onNewLogEvent(LogEvent event) {

                        if (isPlayingRealtime) {
                            boolean passes = true;

                            passes &= (filterValues.getMessage().isEmpty() || event.getMessage().contains(filterValues.getMessage()));
                            passes &= (filterValues.getSourceHost().isEmpty() || event.getSourceHost().contains(filterValues.getSourceHost()));

                            if (passes) {
                                append(format(event));
                            }
                        }
                    }

                });

                SocketClientManager manager = new SocketClientManager(client);
                manager.start();

                manager.addSocketClientManagerListener(new SocketClientManagerListener() {
                    @Override public void onStateChanged(State fromState, State toState) {
                        append("Connection state changed : {}", toState);
                    }
                });

                // try {
                // client.connect();
                //
                // append("Connected");
                // // promptStack.push(arguments[0]);
                // }
                // catch (ConnectorException e) {
                // append("Failed to connect : '{}'", e.getMessage());
                // }

            }
        });

        handlers.put("lspatterns", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {

                append("Making pattern request....");
                Result<List<Pattern>> result = client.getPatternManagementAPI().getPatterns();

                if (result.isSuccessful()) {
                    List<Pattern> value = result.getValue();
                    append("Patterns ({}) : ", value.size());

                    for (Pattern pattern : value) {
                        append(pattern.toString());
                    }
                }
                else {
                    append("Pattern request failed : {} : {}", result.getInternalReason(), result.getExternalReason());
                }

            }
        });

        handlers.put("lsaggregations", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {

                append("Making aggregation request....");
                Result<List<Aggregation>> result = client.getPatternManagementAPI().getAggregations();

                if (result.isSuccessful()) {
                    List<Aggregation> value = result.getValue();
                    append("Aggregations ({}) : ", value.size());

                    for (Aggregation aggregation : value) {
                        append(aggregation.toString());
                    }
                }
                else {
                    append("Aggregation request failed : {} : {}", result.getInternalReason(), result.getExternalReason());
                }

            }
        });

        handlers.put("subscribe", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                try {
                    client.subscribe();
                }
                catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }
        });

        handlers.put("subscribepattern", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                Destination<ChannelMessage> destination = new Destination<ChannelMessage>() {
                    @Override public void send(ChannelMessage t) {
                        PatternisedLogEvent event = (PatternisedLogEvent) t.getPayload();
                        append(StringUtils.format("{} | {} | {} | {}",
                                                  Logger.toDateString(event.getTime()),
                                                  StringUtils.padLeft(event.getSourceHost(), 20),
                                                  StringUtils.padLeft(event.getSourceApplication(), 10),
                                                  Arrays.toString(event.getVariables())));
                    }
                };

                String channel = Channels.getPatternisedStream(Integer.parseInt(arguments[0]));
                client.addSubscription(channel, destination);

                channelDestinations.put(channel, destination);

            }
        });

        handlers.put("subscribeaggregation", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                Destination<ChannelMessage> destination = new Destination<ChannelMessage>() {
                    @Override public void send(ChannelMessage t) {
                        AggregatedLogEvent event = (AggregatedLogEvent) t.getPayload();
                        append(StringUtils.format("{} | {} | {}",
                                                  Logger.toDateString(event.getTime()),
                                                  StringUtils.padLeft(event.getSeriesKey(), 20),
                                                  "" + event.getValue()));
                    }
                };

                String channel = Channels.getAggregatedStream(Integer.parseInt(arguments[0]));
                client.addSubscription(channel, destination);

                channelDestinations.put(channel, destination);

            }
        });

        handlers.put("unsubscribepattern", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                String channel = Channels.getPatternisedStream(Integer.parseInt(arguments[0]));
                Destination<ChannelMessage> destination = channelDestinations.remove(channel);
                client.removeSubscription(channel, destination);
            }
        });

        handlers.put("unsubscribeaggregation", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                String channel = Channels.getAggregatedStream(Integer.parseInt(arguments[0]));
                Destination<ChannelMessage> destination = channelDestinations.remove(channel);
                client.removeSubscription(channel, destination);
            }
        });

        handlers.put("unsubscribe", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                try {
                    client.unsubscribe();
                }
                catch (LoggingMessageSenderException e) {
                    e.printStackTrace();
                }
            }
        });

        handlers.put("exit", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                System.exit(0);
            }
        });

        handlers.put("quit", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                System.exit(0);
            }
        });

        handlers.put("clientdebug", new CommandHandler() {
            @Override public void handle(ConsoleReader reader, final PrintWriter out, String command, String[] arguments) {
                client.setDebug(!client.isDebug());
            }
        });
        
    }

    public void registerHandler(String command, CommandHandler handler) {
        handlers.put(command, handler);
    }

    public void processCommand(String line) {
        String[] split = line.split(" ");
        String command = split[0];

        String[] arguments = new String[split.length - 1];
        System.arraycopy(split, 1, arguments, 0, split.length - 1);

        append("Command is '{}' with arguments '{}'", command, Arrays.toString(arguments));
        CommandHandler commandHandler = handlers.get(command);
        if (commandHandler != null) {
            commandHandler.handle(null, null, command, arguments);
        }
    }

    public abstract void append(String line, Object... args);

    private String format(LogEvent event) {

        String message = event.getMessage();
        int index = message.indexOf('\n');
        if (index == -1) {
            // Fine, no CR
        }
        else {
            message = message.substring(0, index);
        }

        String level = StringUtils.padCenter(event.getLevelDescription(), 7, ' ');

        String host = StringUtils.padCenter(event.getSourceHost(), 20, ' ');

        return StringUtils.format("{} | {} | {} | {}", Logger.toDateString(event.getOriginTime()), level, host, message);

    }

    private String format(PatternisedLogEvent event) {

        String message = Arrays.toString(event.getVariables());
        int index = message.indexOf('\n');
        if (index == -1) {
            // Fine, no CR
        }
        else {
            message = message.substring(0, index);
        }

        String level = StringUtils.padCenter("" + event.getLevel(), 7, ' ');
        String host = StringUtils.padCenter(event.getSourceHost(), 20, ' ');
        return StringUtils.format("{} | {} | {} | {}", Logger.toDateString(event.getTime()), level, host, message);
    }

    private String format(AggregatedLogEvent event) {
        String message = NumberFormat.getInstance().format(event.getValue());

        String seriesKey = StringUtils.padCenter(event.getSeriesKey(), 7, ' ');
        String aggregationID = StringUtils.padCenter("" + event.getAggregationID(), 20, ' ');

        return StringUtils.format("{} | {} | {} | {}", Logger.toDateString(event.getTime()), aggregationID, seriesKey, message);
    }

    public Result<Pattern> createPattern(String name, String pattern) {

        PatternManagementAPI patternAPI = client.getPatternManagementAPI();

        Pattern template = new Pattern();

        template.setName(name);
        template.setPattern(pattern);

        Result<Pattern> createPatternResult = patternAPI.createPattern(template);

        return createPatternResult;
    }

    public Result<Aggregation> createAggregation(Aggregation aggregationTemplate) {
        PatternManagementAPI patternAPI = client.getPatternManagementAPI();
        Result<Aggregation> createPatternResult = patternAPI.createAggregation(aggregationTemplate);
        return createPatternResult;
    }

    public List<String> getPotentialCommands(String partial) {

        List<String> potentials = new ArrayList<String>();

        Set<String> keySet = handlers.keySet();
        List<String> ordered = new ArrayList<String>();
        ordered.addAll(keySet);
        Collections.sort(ordered);

        for (String command : ordered) {
            if (command.startsWith(partial)) {
                potentials.add(command);
            }
        }

        return potentials;
    }

    public FilterValues getFilterValues() {
        return filterValues;
    }

    public void makeHistoricalAggregatedEventRequest() {
        isPlayingRealtime = false;

        // Construct the history data request message to send to the hub
        final HistoricalAggregatedDataRequest request = new HistoricalAggregatedDataRequest();
        request.setCorrelationID(client.getNextCorrelationID());

        // We'll request the last 5 minutes of data
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "5 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        append(AnsiColourHelper.format(AnsiColour.Cyan, AnsiColour.None, true, "Making historical patternised data request..."));

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        LoggingMessageListener listener = new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof HistoricalAggregatedDataResponse) {
                    HistoricalAggregatedDataResponse response = (HistoricalAggregatedDataResponse) message;

                    if (response.getCorrelationID() == request.getCorrelationID()) {
                        AggregatedLogEvent[] events = response.getEvents();

                        for (AggregatedLogEvent defaultLogEvent : events) {
                            // This is where you add your code to consume the historical events
                            append("*" + format(defaultLogEvent));
                        }

                        count.value += events.length;
                        batches.value++;

                        // The isLastBatch field indidicates when all of the data has been received
                        if (!response.isLastBatch()) {
                            append("=== more to follow ===");
                        }
                        else {
                            append("======================");

                            // This is the final batch, so notify the main thread we are done
                            latch.countDown();
                        }
                    }
                }

            }

        };
        client.addLoggingMessageListener(listener);

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        try {
            client.send(request);
        }
        catch (LoggingMessageSenderException e) {
            e.printStackTrace();
        }

        // Block the main thread and wait for the response to arrive
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            client.removeLoggingMessageListener(listener);
        }

        append("Received {} events in {} batches", count, batches);

    }

    public void makeHistoricalPatternisedEventRequest() {

        isPlayingRealtime = false;

        // Construct the history data request message to send to the hub
        final HistoricalPatternisedDataRequest request = new HistoricalPatternisedDataRequest();
        request.setCorrelationID(client.getNextCorrelationID());

        // We'll request the last 5 minutes of data
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "5 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        append(AnsiColourHelper.format(AnsiColour.Cyan, AnsiColour.None, true, "Making historical patternised data request..."));

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        LoggingMessageListener listener = new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof HistoricalPatternisedDataResponse) {
                    HistoricalPatternisedDataResponse response = (HistoricalPatternisedDataResponse) message;
                    if (response.getCorrelationID() == request.getCorrelationID()) {
                        PatternisedLogEvent[] events = response.getEvents();

                        for (PatternisedLogEvent defaultLogEvent : events) {
                            // This is where you add your code to consume the historical events
                            append("*" + format(defaultLogEvent));
                        }

                        count.value += events.length;
                        batches.value++;

                        // The isLastBatch field indidicates when all of the data has been received
                        if (!response.isLastBatch()) {
                            append("=== more to follow ===");
                        }
                        else {
                            append("======================");

                            // This is the final batch, so notify the main thread we are done
                            latch.countDown();
                        }
                    }
                }

            }

        };
        client.addLoggingMessageListener(listener);

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        try {
            client.send(request);
        }
        catch (LoggingMessageSenderException e) {
            e.printStackTrace();
        }

        // Block the main thread and wait for the response to arrive
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            client.removeLoggingMessageListener(listener);
        }

        append("Received {} events in {} batches", count, batches);

    }

    public void makeHistoricalEventRequest() {

        isPlayingRealtime = false;

        // Construct the history data request message to send to the hub
        final HistoricalDataRequest request = new HistoricalDataRequest();
        request.setCorrelationID(client.getNextCorrelationID());

        // We'll request the last 5 minutes of data
        long now = System.currentTimeMillis();
        request.setStart(TimeUtils.before(now, "5 minutes"));
        request.setEnd(now);

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);
        final MutableInt count = new MutableInt(0);

        append(AnsiColourHelper.format(AnsiColour.Cyan, AnsiColour.None, true, "Making historical request..."));

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        LoggingMessageListener listener = new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof HistoricalDataResponse) {
                    HistoricalDataResponse response = (HistoricalDataResponse) message;

                    if (response.getCorrelationID() == request.getCorrelationID()) {

                        DefaultLogEvent[] events = response.getEvents();

                        for (DefaultLogEvent defaultLogEvent : events) {
                            // This is where you add your code to consume the historical events
                            append("*" + format(defaultLogEvent));
                        }

                        count.value += events.length;
                        batches.value++;

                        // The isLastBatch field indidicates when all of the data has been received
                        if (!response.isLastBatch()) {
                            append("=== more to follow ===");
                        }
                        else {
                            append("======================");

                            // This is the final batch, so notify the main thread we are done
                            latch.countDown();
                        }
                    }
                }

            }

        };
        client.addLoggingMessageListener(listener);

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        try {
            client.send(request);
        }
        catch (LoggingMessageSenderException e) {
            e.printStackTrace();
        }

        // Block the main thread and wait for the response to arrive
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            client.removeLoggingMessageListener(listener);
        }

        append("Received {} events in {} batches", count, batches);

    }

    public void makeHistoricalAggregatedHealthRequest() {
        WorkerThread.execute("CommandExecutor", new Runnable() {
            @Override public void run() {
                makeHealthRequest(Channels.aggregatedHistoryRequests);
            }
        });
    }

    public void makeHistoricalPatternisedHealthRequest() {
        WorkerThread.execute("CommandExecutor", new Runnable() {
            @Override public void run() {
                makeHealthRequest(Channels.patternisedHistoryRequests);
            }
        });
    }

    public void makeHistoricalEventHealthRequest() {
        WorkerThread.execute("CommandExecutor", new Runnable() {
            @Override public void run() {
                makeHealthRequest(Channels.eventHistoryRequests);
            }
        });
    }

    public void makeHealthRequest(String channel) {
        isPlayingRealtime = false;

        // Construct the history data request message to send to the hub
        final HealthCheckRequest request = new HealthCheckRequest();

        // The request will execute asynchronously in one or more batch updates, so if we want to
        // wait for the response to complete we'll need to coordinate between the threads
        final CountDownLatch latch = new CountDownLatch(1);

        // We can use some counters to track the process of the request
        final MutableInt batches = new MutableInt(0);

        append(AnsiColourHelper.format(AnsiColour.Cyan, AnsiColour.None, true, "Making historical request..."));

        // Build a channel message to wrap it
        final ChannelMessage requestMessage = new ChannelMessage();
        requestMessage.setCorrelationID(client.getNextCorrelationID());
        requestMessage.setReplyToChannel(Channels.getPrivateConnectionChannel(client.getConnectionID()));
        requestMessage.setChannel(channel);
        requestMessage.setPayload(request);

        // We need to bind to the message receiver to pick out the HistoricalDataResponse messages
        LoggingMessageListener listener = new LoggingMessageListener() {

            @Override public void onNewLoggingMessage(LoggingMessage message) {

                if (message instanceof ChannelMessage) {
                    ChannelMessage channelMessage = (ChannelMessage) message;

                    if (channelMessage.getCorrelationID() == requestMessage.getCorrelationID()) {

                        HealthCheckResponse response = (HealthCheckResponse) channelMessage.getPayload();

                        List<String> splitIntoLineList = StringUtils.splitIntoLineList(response.getContent());
                        for (String string : splitIntoLineList) {
                            append(string);
                        }
                        
                        batches.value++;

                        // The isLastBatch field indidicates when all of the data has been received
                        if (response.isMoreToFollow()) {
                            append("=== more to follow ===");
                        }
                        else {
                            append("======================");

                            // This is the final batch, so notify the main thread we are done
                            latch.countDown();
                        }
                    }
                }

            }

        };
        client.addLoggingMessageListener(listener);

        // Send the request - note that we always register the listener _before_ sending the request
        // to avoid race conditions.
        try {
            client.send(requestMessage);
        }
        catch (LoggingMessageSenderException e) {
            e.printStackTrace();
        }

        // Block the main thread and wait for the response to arrive
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            client.removeLoggingMessageListener(listener);
        }

        append("Received {} batches", batches);
    }
}
