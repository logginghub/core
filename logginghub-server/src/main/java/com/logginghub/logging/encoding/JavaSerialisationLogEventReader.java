package com.logginghub.logging.encoding;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Destination;

public class JavaSerialisationLogEventReader implements LogEventReader {

    @Override public void readAll(InputStream stream, Destination<LogEvent> destination) {

        BufferedInputStream bis = new BufferedInputStream(stream, 100 * 1024 * 1024);

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);

            while (true) {
                try {
                    DefaultLogEvent dle = (DefaultLogEvent) ois.readObject();
                    destination.send(dle);
                }
                catch (EOFException eof) {

                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
