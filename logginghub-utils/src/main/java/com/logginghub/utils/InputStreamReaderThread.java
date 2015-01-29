package com.logginghub.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classic input stream reader thread, suitable for sucking bytes out of the input stream and
 * storing them in a string builder. Most obviously useful for reading output and error streams from
 * a process. Can optionally echo the results out to a print stream.
 * 
 * @author James
 * 
 */
public class InputStreamReaderThread {

    private StringBuilder lineBuffer = new StringBuilder();
    private InputStream inputStream;
    private boolean daemon = false;
    private PrintStream echo;

    private List<InputStreamReaderThreadListener> listeners = new CopyOnWriteArrayList<InputStreamReaderThreadListener>();
    private String name = "InputStreamReaderThread";
    private Thread thread;

    public InputStreamReaderThread(InputStream errorStream) {
        this.inputStream = errorStream;
    }

    public InputStreamReaderThread(InputStream errorStream, InputStreamReaderThreadListener listener) {
        this.inputStream = errorStream;
        if (listener != null) {
            addListener(listener);
        }
    }

    public InputStreamReaderThread(InputStream errorStream, InputStreamReaderThreadListener listener, boolean daemon) {
        this.inputStream = errorStream;
        this.daemon = daemon;
        if (listener != null) {
            addListener(listener);
        }
    }

    public void addListener(InputStreamReaderThreadListener listener) {
        listeners.add(listener);
    }

    public void removeListener(InputStreamReaderThreadListener listener) {
        listeners.remove(listener);
    }

    private void fireOnCharacter(char c) {
        for (InputStreamReaderThreadListener inputStreamReaderThreadListener : listeners) {
            inputStreamReaderThreadListener.onCharacter(c);
        }
    }

    private void fireOnLine(String line) {
        for (InputStreamReaderThreadListener inputStreamReaderThreadListener : listeners) {
            inputStreamReaderThreadListener.onLine(line);
        }
    }

    public void start() {
        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    int read = -1;

                    while ((read = inputStream.read()) != -1) {
                        char read2 = (char) read;
                        fireOnCharacter(read2);

                        if (read2 == '\n' || read2 == '\r') {
                            if (lineBuffer.length() > 0) {
                                fireOnLine(lineBuffer.toString());
                                lineBuffer = new StringBuilder();
                            }
                        }
                        else {
                            lineBuffer.append(read2);
                        }

                        if (echo != null) {
                            echo.print(read2);
                        }
                    }
                }
                catch (IOException e) {
                    if (!e.getMessage().equalsIgnoreCase("Stream Closed")) {
                        e.printStackTrace();
                    }

                }
            }
        }, name);

        thread.setDaemon(daemon);
        thread.start();
    }

    public void echoTo(PrintStream out) {
        echo = out;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void join() {
        if (thread != null) {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

}
