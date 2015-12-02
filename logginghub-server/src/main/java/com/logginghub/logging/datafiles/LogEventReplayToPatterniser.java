package com.logginghub.logging.datafiles;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.modules.PatternCollection;
import com.logginghub.logging.utils.BinaryFileStream;
import com.logginghub.logging.utils.BinaryPatternisedFileStream;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.Out;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.logging.Logger;

import java.io.File;

/**
 * Created by james on 17/09/15.
 */
public class LogEventReplayToPatterniser {

    public static void main(String[] args) {
        StatBundle bundle = new StatBundle();
        final IntegerStat events = bundle.createIntegerStat("events");
        bundle.startPerSecond(Logger.root());

        final PatternCollection patternCollection = new BatsPatternCollection();

        patternCollection.addNotMatchedDestination(new Destination<LogEvent>() {
            @Override
            public void send(LogEvent patternisedLogEvent) {
                Out.out("FAILED  : {}", patternisedLogEvent);
                System.exit(1);
            }
        });

        final BinaryPatternisedFileStream output = new BinaryPatternisedFileStream(new File(
                "/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp"), "bats.patternised.binary.log");

        output.setAutoFlush(false);

        patternCollection.addDestination(new Destination<PatternisedLogEvent>() {
            @Override
            public void send(PatternisedLogEvent patternisedLogEvent) {
                output.send(patternisedLogEvent);
            }
        });

        BinaryFileStream.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp", "bats.binary.log"),
                                new Destination<LogEvent>() {
                                    @Override
                                    public void send(LogEvent logEvent) {
                                        events.increment(1);
                                        patternCollection.send(logEvent);
                                    }
                                });
        bundle.stop();
        Out.out("Events = {}", events.getValue());

        output.close();
    }


}
