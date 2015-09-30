package com.logginghub.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class CountingInputStream extends FilterInputStream {

    private volatile long currentCount;
    private long mark = -1;

    public CountingInputStream(InputStream decorated) {
        super(decorated);
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
        }
        return result;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            currentCount += result;
        }
        return result;
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