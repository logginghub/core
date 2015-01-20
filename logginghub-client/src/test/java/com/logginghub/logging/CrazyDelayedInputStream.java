package com.logginghub.logging;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An input stream that simulates reading byte array chunks in at a fixed interval.
 * 
 * @author admin
 */
public class CrazyDelayedInputStream extends InputStream {
    private List<byte[]> m_chunkList = new ArrayList<byte[]>();

    private PipedOutputStream m_underlyingOutputStream;
    private PipedInputStream m_underlyingInputStream;

    public CrazyDelayedInputStream(byte[] firstChunk, byte[] lastChunk, long interval) throws IOException {
        m_underlyingOutputStream = new PipedOutputStream();
        m_underlyingInputStream = new PipedInputStream(m_underlyingOutputStream);

        m_chunkList.add(firstChunk);
        m_chunkList.add(lastChunk);
        startTimer(interval);
    }

    private void startTimer(long interval) {
        final Timer timer = new Timer("CrazyDelayedInputStream-Timer");

        TimerTask task = new TimerTask() {
            @Override public void run() {
                byte[] nextChunk = m_chunkList.remove(0);

                try {
                    m_underlyingOutputStream.write(nextChunk);
                    m_underlyingOutputStream.flush();
//                    System.out.println(nextChunk.length + " bytes written to the stream");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                if (m_chunkList.isEmpty()) {
                    timer.cancel();
                }
            }
        };

        timer.schedule(task, 0, interval);
    }

    @Override public int read() throws IOException {
        return m_underlyingInputStream.read();
    }

    @Override public int read(byte[] b) throws IOException {
        int available = 0;

        while ((available = m_underlyingInputStream.available()) == 0) {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {}
        }

        int read = m_underlyingInputStream.read(b, 0, available);
        return read;
    }

}
