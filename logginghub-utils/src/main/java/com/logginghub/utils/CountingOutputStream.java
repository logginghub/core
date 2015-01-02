package com.logginghub.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class CountingOutputStream extends FilterOutputStream {

    private long count;

    public CountingOutputStream(OutputStream decoratee) {
        super(decoratee);
    }

    public long getCount() {
        return count;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        count += len;
    }

    public void write(int b) throws IOException {
        out.write(b);
        count++;
    }
}