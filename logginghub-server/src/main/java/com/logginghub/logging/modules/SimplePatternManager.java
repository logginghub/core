package com.logginghub.logging.modules;

import java.util.concurrent.atomic.AtomicInteger;

import com.logginghub.logging.api.patterns.Aggregation;
import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.utils.ObservableList;
import com.logginghub.utils.Result;
import com.logginghub.utils.logging.Logger;

public class SimplePatternManager implements PatternManagerService {

    private static final Logger logger = Logger.getLoggerFor(SimplePatternManager.class);

    private AtomicInteger nextPatternID = new AtomicInteger(0);
    private AtomicInteger nextAggregationID = new AtomicInteger(0);

    public ObservableList<Aggregation> aggregationModels = new ObservableList<Aggregation>();
    public ObservableList<Pattern> patternModels = new ObservableList<Pattern>();

    public void addPattern(Pattern pattern) {
        patternModels.add(pattern);
    }

    public Result<ObservableList<Pattern>> getPatterns() {
        Result<ObservableList<Pattern>> result = new Result<ObservableList<Pattern>>(patternModels);
        return result;
    }

    @Override public Result<ObservableList<Aggregation>> getAggregations() {
        Result<ObservableList<Aggregation>> result = new Result<ObservableList<Aggregation>>(aggregationModels);
        return result;
    }

    public Result<Pattern> createPattern(String name, String pattern) {

        Result<Pattern> result = new Result<Pattern>();

        for (Pattern patternModel : patternModels) {
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
            model.setPatternId(nextPatternID.getAndIncrement());

            logger.fine("Creating pattern : id={} name='{}' pattern='{}'", model.getPatternId(), name, pattern);

            patternModels.add(model);

            result.setValue(model);
        }

        return result;
    }

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

            aggregationModels.add(model);

            result.setValue(model);
        }

        return result;
    }

    @Override public Result<Pattern> getPatternByID(int patternID) {

        Pattern found = null;
        for (Pattern pattern : patternModels) {
            if (pattern.getPatternId() == patternID) {
                found = pattern;
                break;
            }
        }

        return new Result<Pattern>(found);

    }

}
