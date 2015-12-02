package com.logginghub.logging.datafiles;

import com.logginghub.logging.datafiles.aggregation.Aggregation;
import com.logginghub.logging.datafiles.aggregation.PatternAggregation;
import com.logginghub.logging.modules.PatternCollection;
import com.logginghub.logging.utils.BinaryFileStream;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.LongStat;
import com.logginghub.utils.Out;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.StatBundle.StatRenderer;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;
import com.logginghub.utils.sof.SofException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by james on 17/09/15.
 */
public class EventContextsReplayToAggregator {

    public static void main(String[] args) {
        StatBundle bundle = new StatBundle();
        final IntegerStat events = bundle.createIncremental("events");
        final LongStat time = bundle.createSnapshotLongStat("time");
        bundle.setRenderer(time, new StatRenderer<LongStat>() {
            @Override
            public String render(LongStat stat) {
                return Logger.toDateString(stat.getValue()).toString();
            }
        });
        bundle.startPerSecond(Logger.root());

        final PatternCollection patternCollection = new BatsPatternCollection();

        final Aggregation aggregation = new Aggregation(patternCollection, 1000);

        BinaryFileStream.replayEventContexts(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                                      "bats.binary.log"), new Destination<EventContext>() {
            @Override
            public void send(EventContext eventContext) {
                events.increment(1);
                aggregation.send(eventContext);
                time.set(eventContext.getTime());
            }
        });

        bundle.stop();
        Out.out("Events = {N}", events.getTotal());

        // Write out the individual pattern files
        Collection<PatternAggregation> patternData = aggregation.getPatternData();
        for (PatternAggregation patternAggregation : patternData) {
            BinaryPatternAggregatedFileStream writer = new BinaryPatternAggregatedFileStream(new File(
                    "/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp"), "bats.aggregatedpattern." +
                                                                                                        patternAggregation.getPatternId() +
                                                                                                        ".binary.log");
            writer.send(patternAggregation);
            writer.close();
        }

        // Write out the consolated overview
        SummaryBuilder summaryBuilder = new SummaryBuilder();
        summaryBuilder.build(aggregation);
        try {
            summaryBuilder.save(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                         "bats.aggregatedsummary.binary.log"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SofException e) {
            e.printStackTrace();
        }

    }


}
