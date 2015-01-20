package com.logginghub.logging.encoding;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.InputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.sof.DefaultSofReader;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.StreamReaderAbstraction;

public class SofLogEventReader implements LogEventReader {

    private SofConfiguration configuration = new SofConfiguration();

    public SofLogEventReader(boolean micro) {
        configuration.setMicroFormat(micro);
    }

    @Override public void readAll(InputStream stream, Destination<LogEvent> destination) {
        configuration.registerType(DefaultLogEvent.class, 1);

        StreamReaderAbstraction reader = new StreamReaderAbstraction(new BufferedInputStream(stream), Integer.MAX_VALUE);
        DefaultSofReader sofReader = new DefaultSofReader(reader, configuration);

        try {
            while (true) {
                DefaultLogEvent event = new DefaultLogEvent();
                event.read(sofReader);
                destination.send(event);
            }
        }
        catch (SofException e) {
            if (e.getCause() instanceof EOFException) {
                // Fine
            }
            else {
                e.printStackTrace();
            }
        }

    }

}
