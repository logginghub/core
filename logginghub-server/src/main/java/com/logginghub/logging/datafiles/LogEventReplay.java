package com.logginghub.logging.datafiles;

import com.logginghub.logging.utils.BinaryFileStream;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.Out;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.logging.LoggerPerformanceInterface.EventContext;

import java.io.File;

/**
 * Created by james on 17/09/15.
 */
public class LogEventReplay {

    public static void main(String[] args) {
        StatBundle bundle = new StatBundle();
        final IntegerStat events = bundle.createIncremental("events");
        bundle.startPerSecond(Logger.root());

        Stopwatch start = Stopwatch.start("Reading events");

        BinaryFileStream.replayEventContexts(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp",
                                                      "bats.binary.log"), new Destination<EventContext>() {
            @Override
            public void send(EventContext eventContext) {
                events.increment(1);
            }
        });
        bundle.stop();
        Out.out("Events = {N}", events.getTotal());
        start.stopAndDumpInterval();
    }


}
