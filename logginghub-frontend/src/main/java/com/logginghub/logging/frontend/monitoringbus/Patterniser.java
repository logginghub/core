package com.logginghub.logging.frontend.monitoringbus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.logging.Logger;

/**
 * @deprecated Use the {@link com.logginghub.logging.modules.PatterniserModule} module instead going forwards
 * @author James
 *
 */
public class Patterniser {

    private List<ValueStripper2> strippers = new CopyOnWriteArrayList<ValueStripper2>();

    public void add(ValueStripper2 stripper) {
        strippers.add(stripper);
    }

    public PatternisedLogEvent patternise(LogEvent t) {
        PatternisedLogEvent patternised = null;
        for (ValueStripper2 valueStripper2 : strippers) {
            patternised = valueStripper2.patternise2(t);
            if (patternised != null) {
                break;
            }
        }

       return patternised;
    }

    public void dumpPatterns(Logger logger) {
        for (ValueStripper2 valueStripper2 : strippers) {
            logger.info("Patter '{}' : '{}'", valueStripper2.getPatternName(), valueStripper2.getRawPattern());
        }
    }

}
