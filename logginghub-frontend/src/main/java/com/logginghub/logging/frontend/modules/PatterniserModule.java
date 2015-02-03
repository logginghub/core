package com.logginghub.logging.frontend.modules;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.frontend.services.PatternManagementService;
import com.logginghub.logging.frontend.services.PatternisedEventService;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatternCollection;
import com.logginghub.utils.Destination;
import com.logginghub.utils.Source;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Inject;
import com.logginghub.utils.observable.ObservableList;

import java.util.List;

public class PatterniserModule implements PatternManagementService, PatternisedEventService {

    private static final Logger logger = Logger.getLoggerFor(PatterniserModule.class);
    private EnvironmentMessagingService environmentMessagingService;

    // private Multiplexer<PatternisedLogEvent> eventStream = new
    // Multiplexer<PatternisedLogEvent>();

    private PatternCollection patternCollection = new PatternCollection();

    public PatterniserModule() {

    }

    public PatterniserModule(Source<LogEvent> source) {
        source.addDestination(patternCollection);
    }

    // TODO : we've really got two models here - the configuration details provided in patternModel,
    // and the actual model inside the patternCollection?
    //    private List<PatternModel> patterns = new ArrayList<PatternModel>();

    @Inject public void setEnvironmentMessagingService(EnvironmentMessagingService environmentMessagingService) {
        this.environmentMessagingService = environmentMessagingService;
    }

    public void initialise() {
        environmentMessagingService.addLogEventListener(patternCollection);
        //        patternCollection.configureFromModels(patterns);
        logger.info("Patterniser module running");
    }

    @Override public ObservableList<PatternModel> listPatterns() {
        return patternCollection.getObservablePatternList();

    }

    @Override public List<String> getPatternNames() {
        return patternCollection.getPatternNames();

    }

    @Override public void addPatternisedEventListener(Destination<PatternisedLogEvent> destination) {
        patternCollection.addDestination(destination);
    }

    @Override public void removePatternisedEventListener(Destination<PatternisedLogEvent> destination) {
        patternCollection.removeDestination(destination);
    }

    public ObservableList<Pattern> getPatterns() {
        return patternCollection.getPatternList();
    }

    @Override public Pattern getPatternByID(int patternID) {
        Pattern foundPattern = null;
        for (Pattern pattern : patternCollection.getPatternList()) {
            if (pattern.getPatternID() == patternID) {
                foundPattern = pattern;
                break;
            }
        }
        return foundPattern;
    }

    @Override public String getLabelName(int patternID, int labelIndex) {
        return null;

    }

    @Override public String getPatternName(int patternID) {
        return null;

    }

    public void addPattern(Pattern pattern) {
        patternCollection.add(pattern);
    }
}
