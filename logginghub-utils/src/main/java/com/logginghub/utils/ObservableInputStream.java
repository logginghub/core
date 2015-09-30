package com.logginghub.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ObservableInputStream extends FilterInputStream {

    private volatile long currentCount;
    private long mark = -1;

    public interface StreamListener {
        void onRead(int value);
        void onRead(byte[] b, int off, int len);
        void onEndOfStream();
    }

    private List<StreamListener> streamListeners = new CopyOnWriteArrayList<StreamListener>();

    public ObservableInputStream(InputStream decorated) {
        super(decorated);
    }

    public void addListener(StreamListener streamListener) {
        streamListeners.add(streamListener);
    }

    public void removeListener(StreamListener streamListener) {
        streamListeners.remove(streamListener);
    }

    public long getCount() {
        return currentCount;
    }

    public void mark(int readlimit) {
        in.mark(readlimit);
        mark = currentCount;
    }

    public int read() throws IOException {
        int result = in.read();
        if (result != -1) {
            currentCount++;
            notifyRead(result);
        }else{
            notifyEndOfStream();
        }
        return result;
    }

    private void notifyRead(int result) {
        for (StreamListener streamListener : streamListeners) {
            streamListener.onRead(result);
        }
    }

    private void notifyEndOfStream() {
        for (StreamListener streamListener : streamListeners) {
            streamListener.onEndOfStream();
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            currentCount += result;
            notifyRead(b, off, result);
        }else{
            notifyEndOfStream();
        }
        return result;
    }

    private void notifyRead(byte[] b, int off, int result) {
        for (StreamListener streamListener : streamListeners) {
            streamListener.onRead(b, off, result);
        }
    }

    public void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        in.reset();
        currentCount = mark;
    }

    public long skip(long numberOfBytes) throws IOException {
        long result = in.skip(numberOfBytes);
        currentCount += result;
        return result;
    }
}