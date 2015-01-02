package com.logginghub.utils.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InputStreamReaderThread
{
    private InputStream m_inputStream;

    private List<InputStreamReaderListener> m_listeners = new CopyOnWriteArrayList<InputStreamReaderListener>();

    public void addInputStreamReaderThreadListener(InputStreamReaderListener listener)
    {
        m_listeners.add(listener);
    }

    public void removeInputStreamReaderThreadListener(InputStreamReaderListener listener)
    {
        m_listeners.remove(listener);
    }

    private void fireBytesRead(byte[] buffer, int offset, int length)
    {
        for (InputStreamReaderListener listener : m_listeners)
        {
            listener.onBytesRead(buffer, offset, length);
        }
    }

    public InputStreamReaderThread(InputStream inputStream)
    {
        m_inputStream = inputStream;
    }

    public void start()
    {
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    int read = -1;
                    byte[] inputBuffer = new byte[100 * 1024];

                    while (read > 0)
                    {
                        read = m_inputStream.read(inputBuffer);
                        fireBytesRead(inputBuffer, 0, read);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
