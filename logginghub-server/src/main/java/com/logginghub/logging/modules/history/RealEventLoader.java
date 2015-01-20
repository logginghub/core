
package com.logginghub.logging.modules.history;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.utils.KryoVersion1Decoder;
import com.logginghub.logging.utils.LogEventBlockElement;
import com.logginghub.utils.IntegerStat;
import com.logginghub.utils.StatBundle;
import com.logginghub.utils.logging.Logger;

public class RealEventLoader {

    public static void loadEvents(final int count, final List<DefaultLogEvent> events) {
        StatBundle bundle = new StatBundle();
        final IntegerStat stat = bundle.createIncremental("Read per second");
        File inputFile = new File("x:\\temp\\20130308.140000.logdata");

        KryoVersion1Decoder reader = new KryoVersion1Decoder();
        com.logginghub.utils.StreamListener<LogEvent> b = new com.logginghub.utils.StreamListener<LogEvent>() {
            @Override public void onNewItem(LogEvent t) {
                DefaultLogEvent defaultLogEvent = (DefaultLogEvent) t;
                if (t.getMessage().length() > 1000) {}
                else {
                    stat.increment();
                    if (events.size() < count) {
                        events.add(defaultLogEvent);
                    }
                }

            }
        };
        com.logginghub.utils.StreamListener<LogEventBlockElement> a = new com.logginghub.utils.StreamListener<LogEventBlockElement>() {
            @Override public void onNewItem(LogEventBlockElement t) {}
        };

        bundle.startPerSecond(Logger.root());
        try {
            // jshaw - we read more than we need incase the size filter cuts some out
            reader.readFileInternal(inputFile, a, b, 0, count + 1000);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        bundle.stop();
    }

    
}
