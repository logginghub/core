package com.logginghub.logging.frontend.charting;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventMultiplexer;
import com.logginghub.logging.frontend.analysis.ChunkedResultMultiplexer;
import com.logginghub.logging.frontend.analysis.ResultKeyBuilder;
import com.logginghub.logging.frontend.analysis.SimpleMatcher;
import com.logginghub.logging.frontend.analysis.TimeChunkingGenerator;
import com.logginghub.logging.frontend.charting.model.ChartingModel;
import com.logginghub.logging.frontend.charting.model.ParserModel;
import com.logginghub.logging.frontend.charting.model.TimeChunkerModel;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableListListener;

/**
 * Manages the relationships between the {@link ChartingModel} and the various helper classes that
 * do all the data processing {@link ValueStripper2}, {@link ResultKeyBuilder} and
 * {@link TimeChunkingGenerator} etc. It also binds the visual charting elements to the
 * {@link ChartingModel} pages and charts.
 * 
 * @author James
 * 
 */
public class ChartingController implements LogEventListener {

    private ChartingModel model;

    /**
     * List of generators - this need to receive clear messages
     */
    List<TimeChunkingGenerator> generators = new CopyOnWriteArrayList<TimeChunkingGenerator>();

    /**
     * The entities that listen to the raw logging stream
     */
    private LogEventMultiplexer logEventMultiplexer = new LogEventMultiplexer();

    /**
     * Connection point into ChunkedResultHandlers - ie the charts themselves will plug in here
     */
    private ChunkedResultMultiplexer chunkedResultMultiplexer = new ChunkedResultMultiplexer();

    public ChartingController(ChartingModel model) {
        this.model = model;
        setupInternalStructures(model);
    }
    
    public ChunkedResultMultiplexer getChunkedResultMultiplexer() {
        return chunkedResultMultiplexer;
    }

    private void setupInternalStructures(ChartingModel model) {

        // Build the time chunkers
        ObservableList<TimeChunkerModel> timeChunkers = model.getTimeChunkers();
        timeChunkers.addListenerAndNotifyExisting(new ObservableListListener<TimeChunkerModel>() {

            @Override public void onAdded(TimeChunkerModel timeChunkerModel) {
                TimeChunkingGenerator timeChunkingGenerator = createTimeChunkingGenerator(timeChunkerModel);
                timeChunkingGenerator.addChunkedResultHandler(chunkedResultMultiplexer);
                timeChunkerModel.setCounterpart(timeChunkingGenerator);
                generators.add(timeChunkingGenerator);
            }

            @Override public void onRemoved(TimeChunkerModel timeChunkerModel) {
                TimeChunkingGenerator timeChunkingGenerator = timeChunkerModel.getCounterpart();
                timeChunkingGenerator.removeChunkedResultHandler(chunkedResultMultiplexer);
                generators.remove(timeChunkingGenerator);
            }

            @Override public void onCleared() {}

        });
    }

    protected TimeChunkingGenerator createTimeChunkingGenerator(final TimeChunkerModel timeChunkerModel) {

        final TimeChunkingGenerator generator = new TimeChunkingGenerator(timeChunkerModel.getInterval().get());

        timeChunkerModel.getParserModels().addListenerAndNotifyExisting(new ObservableListListener<ParserModel>() {

            @Override public void onRemoved(ParserModel parserModel) {
                ResultKeyBuilder resultKeyBuilder = parserModel.getCounterpart();
                resultKeyBuilder.removeResultListener(generator);
            }

            @Override public void onCleared() {}

            @Override public void onAdded(ParserModel parserModel) {
                ResultKeyBuilder resultKeyBuilder = createResultKeyBuilder(parserModel);
                resultKeyBuilder.addResultListener(generator);
            }
        });

        return generator;
    }

    protected ResultKeyBuilder createResultKeyBuilder(ParserModel parserModel) {

        String format = parserModel.getFormat().get();
        final ResultKeyBuilder resultKeyBuilder = new ResultKeyBuilder(format);
        parserModel.setCounterpart(resultKeyBuilder);

        parserModel.getPatterns().addListenerAndNotifyExisting(new ObservableListListener<PatternModel>() {

            @Override public void onRemoved(PatternModel t) {
                ValueStripper2 valueStripper = t.getCounterpart();
                valueStripper.removeResultListener(resultKeyBuilder);
            }

            @Override public void onCleared() {}

            @Override public void onAdded(PatternModel t) {
                ValueStripper2 valueStripper = createValueStripper(t, resultKeyBuilder);
                t.setCounterpart(valueStripper);
            }
        });

        return resultKeyBuilder;
    }

    protected ValueStripper2 createValueStripper(PatternModel patternModel, ValueStripper2.ValueStripper2ResultListener resultListener) {

        String pattern = patternModel.getPattern().get();
        boolean debug = patternModel.getDebug().get();
        boolean cleanUp = patternModel.getCleanUp().get();

        ValueStripper2 stripper = new ValueStripper2();
        stripper.setPattern(pattern);
        stripper.setDebug(debug);
        if (stripper.getLabels().isEmpty()) {
            // Hmm no labels set, this could be a simple pattern
            String name = patternModel.getName().get();
            if (name != null) {
                SimpleMatcher matcher = new SimpleMatcher(pattern, name);
                matcher.addResultListener(resultListener);
                addEventListener(matcher);

            }
            else {
                // TODO : this is an xml error, might need to reformat it?
                throw new RuntimeException(String.format("Your parsers config looks broken : the pattern was '%s'; it didn't contain any {labels}, and it didn't have a name='' attribute either - you have to have one or more {labels} or a name element to build a matcher",
                                                         pattern));
            }
        }
        else {
            stripper.addResultListener(resultListener);
            addEventListener(stripper);
        }

        return stripper;
    }

    private void addEventListener(LogEventListener matcher) {
        logEventMultiplexer.addLogEventListener(matcher);
    }

    public ChartingModel getModel() {
        return model;
    }

    @Override public void onNewLogEvent(LogEvent event) {
        logEventMultiplexer.onNewLogEvent(event);
    }

    public void flush() {
        for (TimeChunkingGenerator timeChunkingGenerator : generators) {
            timeChunkingGenerator.flush();
        }
    }

}
