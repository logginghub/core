package com.logginghub.logging.frontend.charting;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.analysis.ChunkedResult;
import com.logginghub.logging.frontend.analysis.ChunkedResultHandler;
import com.logginghub.logging.frontend.charting.ChartingController;
import com.logginghub.logging.frontend.charting.model.ChartingModel;
import com.logginghub.logging.frontend.charting.model.ParserModel;
import com.logginghub.logging.frontend.charting.model.TimeChunkerModel;
import com.logginghub.logging.messages.AggregationType;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.logging.Logger;

public class TestChartingController {

    private static final Logger logger = Logger.getLoggerFor(TestChartingController.class);

    @Test public void testChartingController() throws Exception {
//        Logger.setLevel(Logger.trace, ValueStripper2.class, ResultKeyBuilder.class, TimeChunkingGenerator.class, TimeChunker.class, TestChartingController.class);

        PatternModel patternModel = new PatternModel();
        patternModel.getDebug().set(true);
        patternModel.getPattern().set("value1 {value1} value2 {value2} text3 [text3]");

        ParserModel parserModel = new ParserModel();
        parserModel.getFormat().set("{host}/{source}/{label}");
        parserModel.getPatterns().add(patternModel);

        TimeChunkerModel timeChunkerModel = new TimeChunkerModel();
        timeChunkerModel.getInterval().set(100);
        timeChunkerModel.getParserModels().add(parserModel);

        ChartingModel model = new ChartingModel();
        model.getTimeChunkers().add(timeChunkerModel);

        final Bucket<ChunkedResult> bucket = new Bucket<ChunkedResult>();

        ChartingController controller = new ChartingController(model);
        
        controller.getChunkedResultMultiplexer().addChunkedResultHandler(new ChunkedResultHandler() {
            @Override public void onNewChunkedResult(ChunkedResult result) {
                logger.debug("Received result : {}", result);
                bucket.add(result);
            }
            @Override public void complete() {}
        });
        

        DefaultLogEvent event = LogEventBuilder.start()
                                               .setMessage("value1 10 value2 20 text3 foo")
                                               .setSourceApplication("sourceApplication")
                                               .setSourceHost("sourceHost")
                                               .setLocalCreationTimeMillis(0L)
                                               .toLogEvent();

        controller.onNewLogEvent(event);

        controller.flush();

        // This is annoying - the chunkers are currently spewing out all possible values - we need
        // some sort of subscription system where they only pump out the results people are actually
        // interested in...
        int expected = AggregationType.values().length * 2;
        bucket.waitForMessages(expected);

        assertThat(bucket.size(), is(expected));

        ChunkedResult chunkedResult1 = bucket.get(11);
        ChunkedResult chunkedResult2 = bucket.get(1);

        assertThat(chunkedResult1.getChunkDuration(), is(100L));
        assertThat(chunkedResult1.getSource(), is("sourceHost/sourceApplication/value1/Sum"));
        assertThat(chunkedResult1.getStartOfCurrentChunk(), is(0L));
        assertThat(chunkedResult1.getValue(), is(10d));

        assertThat(chunkedResult2.getChunkDuration(), is(100L));
        assertThat(chunkedResult2.getSource(), is("sourceHost/sourceApplication/value2/Sum"));
        assertThat(chunkedResult2.getStartOfCurrentChunk(), is(0L));
        assertThat(chunkedResult2.getValue(), is(20d));
    }
}
