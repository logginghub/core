package com.logginghub.logging.encoding;

import java.io.InputStream;

import com.esotericsoftware.kryo.io.Input;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.repository.KryoWrapper;
import com.logginghub.utils.Destination;

public class KryoLogEventReader implements LogEventReader {

    @Override public void readAll(InputStream stream, Destination<LogEvent> destination) {
        KryoWrapper kryoWrapper = new KryoWrapper();
        Input buffer = new Input(stream); 

        kryoWrapper = new KryoWrapper();
        
        while(true) {
            DefaultLogEvent readObject = kryoWrapper.readObject(buffer, DefaultLogEvent.class);
            destination.send(readObject);
        }
        

    }

}
