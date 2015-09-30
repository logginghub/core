package com.logginghub.logging.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.AggregationListRequest;
import com.logginghub.logging.api.patterns.AggregationListResponse;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.api.patterns.PatternListRequest;
import com.logginghub.logging.api.patterns.PatternListResponse;
import com.logginghub.logging.exceptions.LoggingMessageSenderException;
import com.logginghub.logging.interfaces.LoggingMessageSender;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messages.ChannelMessage;
import com.logginghub.logging.messages.Channels;
import com.logginghub.logging.messages.LoggingMessage;
import com.logginghub.logging.messages.MapMessage;
import com.logginghub.logging.messages.ResponseMessage;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.configuration.PatternManagerConfiguration;
import com.logginghub.logging.servers.ServerMessageHandler;
import com.logginghub.logging.servers.ServerSubscriptionsService;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Result;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.Provides;
import com.logginghub.utils.module.ServiceDiscovery;
import com.logginghub.utils.sof.SerialisableObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Provides(PatternisedLogEvent.class) public class PatternManagerModule implements Module<PatternManagerConfiguration>, PatternManagerService {

    private static final Logger logger = Logger.getLoggerFor(PatternManagerModule.class);

    private PatternManagerConfiguration configuration;

    private AtomicInteger nextPatternId = new AtomicInteger(0);
    private AtomicInteger nextAggregationID = new AtomicInteger(0);

    private File dataFile;

    public static class DataWrapper {
        public ObservableList<Aggregation> aggregationModels = new ObservableList<Aggregation>();
        public ObservableList<Pattern> patternModels = new ObservableList<Pattern>();
    }

    private DataWrapper dataWrapper = new DataWrapper();

    private boolean configured;

    private ServerSubscriptionsService serverSubscriptionsService;

    public void addPattern(Pattern pattern) {
        dataWrapper.patternModels.add(pattern);
    }

    @SuppressWarnings("unchecked") @Override public void configure(PatternManagerConfiguration configuration, ServiceDiscovery discovery) {
        this.configuration = configuration;
        dataFile = new File(configuration.getDataFile());

        if (dataFile.exists()) {

            String json = FileUtils.read(dataFile);
            Gson gson = new Gson();

            // Type listType = new TypeToken<ArrayList<PatternModel>>() {}.getType();
            // Debug.out(json);
            dataWrapper = gson.fromJson(json, DataWrapper.class);
            if (dataWrapper != null) {
                if (dataWrapper != null) {
                    
                    for (Pattern patternModel : dataWrapper.patternModels) {
                        nextPatternId.set(Math.max(nextPatternId.get(), patternModel.getPatternId()));
                    }
                    
                    nextPatternId.incrementAndGet();
                    
                    for (Aggregation aggregation : dataWrapper.aggregationModels) {
                        nextAggregationID.set(Math.max(nextAggregationID.get(), aggregation.getAggregationID()));
                    }
                    
                    nextAggregationID.incrementAndGet();
                }
            }
            else {
                dataWrapper = new DataWrapper();
            }
        }

        serverSubscriptionsService = discovery.findService(ServerSubscriptionsService.class);

        configured = true;
        logger.fine("Next pattern ID will be '{}'", nextPatternId);
    }

    public Result<ObservableList<Pattern>> getPatterns() {
        ensureConfigured();
        Result<ObservableList<Pattern>> result = new Result<ObservableList<Pattern>>(dataWrapper.patternModels);
        return result;
    }

    @Override public Result<ObservableList<Aggregation>> getAggregations() {
        ensureConfigured();
        Result<ObservableList<Aggregation>> result = new Result<ObservableList<Aggregation>>(dataWrapper.aggregationModels);
        return result;
    }

    private void ensureConfigured() {
        if (!configured) {
            throw new RuntimeException("Not configured");
        }
    }

    public Result<Pattern> createPattern(String name, String pattern) {

        Result<Pattern> result = new Result<Pattern>();

        for (Pattern patternModel : dataWrapper.patternModels) {
            String existingName = patternModel.getName();
            String existingPattern = patternModel.getPattern();

            if (existingName.equals(name)) {
                result.failFormat("A pattern with name '{}' already exists", name);
                break;
            }

            if (existingPattern.equals(pattern)) {
                result.failFormat("A pattern with regex '{}' already exists", pattern);
                break;
            }
        }

        if (result.isSuccessful()) {
            Pattern model = new Pattern(name, pattern);
            model.setPatternId(nextPatternId.getAndIncrement());

            logger.fine("Creating pattern : id={} name='{}' pattern='{}'", model.getPatternId(), name, pattern);

            dataWrapper.patternModels.add(model);

            saveData();

            result.setValue(model);
        }

        return result;
    }

    private void saveData() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json2 = gson.toJson(dataWrapper);
        FileUtils.write(json2, dataFile);
    }

    @Override public void start() {
        stop();

        ServerMessageHandler patternsHandler = new ServerMessageHandler() {
            @Override public void onMessage(LoggingMessage message, LoggingMessageSender source) {

                ChannelMessage channelMessage = (ChannelMessage) message;
                SerialisableObject payload = channelMessage.getPayload();

                if (payload instanceof PatternListRequest) {
                    PatternListRequest request = (PatternListRequest) channelMessage.getPayload();

                    ObservableList<Pattern> patterns = dataWrapper.patternModels;

                    PatternListResponse response = new PatternListResponse();
                    response.setPatterns(patterns);

                    ResponseMessage responseWrapper = new ResponseMessage(channelMessage.getCorrelationID(), response);

                    try {
                        source.send(responseWrapper);
                    }
                    catch (LoggingMessageSenderException e) {
                        logger.info("Failed to send response", e);
                    }
                }
                else if (payload instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) payload;

                    String action = mapMessage.get("action");
                    if (action.equals("createPattern")) {

                        String name = mapMessage.get("name");
                        String pattern = mapMessage.get("pattern");

                        Result<Pattern> createPattern = createPattern(name, pattern);

                        if (createPattern.isSuccessful()) {
                            mapMessage.put("patternID", Integer.toString(createPattern.getValue().getPatternId()));
                        }

                        ResponseMessage responseWrapper = new ResponseMessage(channelMessage.getCorrelationID(), mapMessage);
                        responseWrapper.setupResult(createPattern);

                        try {
                            source.send(responseWrapper);
                        }
                        catch (LoggingMessageSenderException e) {
                            logger.info("Failed to send response", e);
                        }
                    }

                }
            }
        };

        serverSubscriptionsService.subscribe(Channels.patternListRequests, patternsHandler);

        ServerMessageHandler aggregationsHandler = new ServerMessageHandler() {
            @Override public void onMessage(LoggingMessage message, LoggingMessageSender source) {

                ChannelMessage channelMessage = (ChannelMessage) message;
                SerialisableObject payload = channelMessage.getPayload();

                if (payload instanceof AggregationListRequest) {
                    AggregationListRequest request = (AggregationListRequest) channelMessage.getPayload();

                    ObservableList<Aggregation> aggregations = dataWrapper.aggregationModels;

                    AggregationListResponse response = new AggregationListResponse();
                    response.setAggregations(aggregations);

                    ResponseMessage responseWrapper = new ResponseMessage(channelMessage.getCorrelationID(), response);

                    try {
                        source.send(responseWrapper);
                    }
                    catch (LoggingMessageSenderException e) {
                        logger.info("Failed to send response", e);
                    }
                }
                else if (payload instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) payload;

                    String action = mapMessage.get("action");
                    if (action.equals("createAggregation")) {

                        int patternID = mapMessage.getInt("patternID");
                        int captureLabelIndex = mapMessage.getInt("captureLabelIndex");
                        String groupBy = mapMessage.get("groupBy");
                        long interval = mapMessage.getLong("interval");
                        String type = mapMessage.get("type");

                        Result<Aggregation> createAggregation = createAggregation(patternID,
                                                                                  captureLabelIndex,
                                                                                  interval,
                                                                                  AggregationType.valueOf(type),
                                                                                  groupBy);

                        if (createAggregation.isSuccessful()) {
                            mapMessage.put("aggregationID", Integer.toString(createAggregation.getValue().getAggregationID()));
                        }

                        ResponseMessage responseWrapper = new ResponseMessage(channelMessage.getCorrelationID(), mapMessage);
                        responseWrapper.setupResult(createAggregation);

                        try {
                            source.send(responseWrapper);
                        }
                        catch (LoggingMessageSenderException e) {
                            logger.info("Failed to send response", e);
                        }
                    }

                }
            }
        };

        serverSubscriptionsService.subscribe(Channels.aggregationListRequests, aggregationsHandler);

    }

    @Override public void stop() {}

    @Override public Result<Aggregation> createAggregation(int pattern, int label, long interval, AggregationType type, String groupBy) {
        Result<Aggregation> result = new Result<Aggregation>();

        if (result.isSuccessful()) {
            Aggregation model = new Aggregation();
            model.setAggregationID(nextAggregationID.getAndIncrement());
            model.setPatternID(pattern);
            model.setCaptureLabelIndex(label);
            model.setInterval(interval);
            model.setGroupBy(groupBy);
            model.setType(type);

            logger.fine("Creating aggregation : id={} pattern='{}' interval='{}' type='{}'", model.getAggregationID(), pattern, interval, type);

            dataWrapper.aggregationModels.add(model);

            saveData();

            result.setValue(model);
        }

        return result;
    }

    @Override public Result<Pattern> getPatternByID(int patternID) {

        Pattern found = null;
        List<Pattern> patternModels = dataWrapper.patternModels;
        for (Pattern pattern : patternModels) {
            if (pattern.getPatternId() == patternID) {
                found = pattern;
                break;
            }
        }

        return new Result<Pattern>(found);

    }

}
