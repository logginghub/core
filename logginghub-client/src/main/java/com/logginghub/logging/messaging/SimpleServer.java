package com.logginghub.logging.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.logeventformatters.SingleLineLogEventTextFormatter;
import com.logginghub.logging.messages.PartialMessageException;

public class SimpleServer implements Runnable {
    private Thread m_thread;
    private boolean m_keepRunning = true;
    private ServerSocket m_serverSocket;
    private Socket m_clientSocket;
    private List<LogEvent> m_messages = new ArrayList<LogEvent>();
    private CountDownLatch m_countdownLatch;
    private int m_port = 666;

    public SimpleServer() {

    }

    public void run() {
        try {
            m_serverSocket = new ServerSocket(m_port);
            m_clientSocket = m_serverSocket.accept();

            SingleLineLogEventTextFormatter formatter = new SingleLineLogEventTextFormatter();

            ByteBuffer decodeBuffer = ByteBuffer.allocate(4096);

            InputStream inputStream = m_clientSocket.getInputStream();

            byte[] inputBuffer = new byte[4096];

            while (m_keepRunning) {
                int bytesRead = inputStream.read(inputBuffer);

                // Resize the decode buffer if needs be
                if (decodeBuffer.remaining() < bytesRead) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(decodeBuffer.capacity());
                    newBuffer.put(decodeBuffer);
                    decodeBuffer = newBuffer;
                }

                decodeBuffer.put(inputBuffer, 0, bytesRead);

                try {
                    decodeBuffer.flip();
                    decodeBuffer.mark();
                    LogEvent event = LogEventCodex.decode(decodeBuffer);
                    System.out.println(formatter.format(event));
                    decodeBuffer.compact();
                }
                catch (PartialMessageException e) {
                    decodeBuffer.reset();
                    decodeBuffer.flip();
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int getListeningPort() {
        return m_port;
    }

    public void start() {
        m_thread = new Thread(this, "Simple server thread");
        m_keepRunning = true;
        m_thread.start();
    }

    public void stop() {
        m_keepRunning = false;
        try {
            m_serverSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m_clientSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            m_thread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<LogEvent> getMessages() {
        return m_messages;
    }

    /**
     * Wait for the message bucket to contain this many items
     * 
     * @param i
     */
    public void waitForMessages(int i) {
        synchronized (m_messages) {
            int currentMessages = m_messages.size();
            if (currentMessages < i) {
                int diff = i - currentMessages;
                m_countdownLatch = new CountDownLatch(diff);
            }
        }

        try {
            m_countdownLatch.await(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
