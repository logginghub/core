package com.logginghub.logging.datafiles;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.BinaryFileStream;
import com.logginghub.utils.Destination;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.Out;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.logging.Logger;

import java.io.File;

/**
 * Created by james on 17/09/15.
 */
public class LogEventReplay {

    public static void main(String[] args) {
        StatBundle bundle = new StatBundle();
        final IntegerStat events = bundle.createIncremental("events");
        bundle.startPerSecond(Logger.root());

        BinaryFileStream.replay(new File("/Users/james/development/git/marketstreamer/marketstreamer-core/mso-trading/tmp", "bats.binary.log"),
                                new Destination<LogEvent>() {
                                    @Override
                                    public void send(LogEvent logEvent) {
                                        events.increment(1);
                                    }
                                });
        bundle.stop();
        Out.out("Events = {}", events.getValue());
    }


}
