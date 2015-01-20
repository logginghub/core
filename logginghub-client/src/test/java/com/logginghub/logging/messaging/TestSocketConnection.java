package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.messaging.SocketConnection;
import com.logginghub.testutils.CustomRunner;

@RunWith(CustomRunner.class)
public class TestSocketConnection {

    @Test public void test_level_filter() throws IOException {

        InputStream inputStream = Mockito.mock(InputStream.class);
        OutputStream outputStream = Mockito.mock(OutputStream.class);
        Socket socket = Mockito.mock(Socket.class);
        Mockito.when(socket.getOutputStream()).thenReturn(outputStream);
        Mockito.when(socket.getInputStream()).thenReturn(inputStream);

        SocketConnection connection = new SocketConnection(socket);
        connection.start();

        // Setup two events - on info and one warning
        DefaultLogEvent infoEvent = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent warningEvent = LogEventFactory.createFullLogEvent1();

        infoEvent.setLevel(Level.INFO.intValue());
        warningEvent.setLevel(Level.WARNING.intValue());

        connection.send(warningEvent);
        connection.send(infoEvent);
        connection.waitForSend();

        // Make sure clientA received them
        Mockito.verify(outputStream, Mockito.times(2)).write(Mockito.any(byte[].class));

        // Set clientA's filter level to warning
        connection.setLevelFilter(Level.WARNING.intValue());

        connection.send(warningEvent);
        connection.send(infoEvent);
        connection.waitForSend();

        // clientA shouldn't have received the info one a second time
        Mockito.verify(outputStream, Mockito.times(3)).write(Mockito.any(byte[].class));

        // Set back to info
        connection.setLevelFilter(Level.INFO.intValue());

        connection.send(warningEvent);
        connection.send(infoEvent);
        connection.waitForSend();

        // Make sure clientA received them
        Mockito.verify(outputStream, Mockito.times(5)).write(Mockito.any(byte[].class));

        connection.close();
    }

}
