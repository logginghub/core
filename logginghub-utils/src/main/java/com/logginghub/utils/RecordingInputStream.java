package com.logginghub.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by james on 24/08/15.
 */
public class RecordingInputStream extends InputStream {

    private final InputStream inputStream;
    private CircularByteArray data = new CircularByteArray(1 * 1024 * 1024);

    public RecordingInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        int read = inputStream.read();
        data.write((byte) read);
        return read;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int read = inputStream.read(b);
        if (read == -1) {
            data.write((byte) -1);
        } else {
            data.write(b, 0, read);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = inputStream.read(b, off, len);
        if (read == -1) {
            data.write((byte) -1);
        } else {
            data.write(b, off, read);
        }
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        // TODO : god knows what we should do here?
        throw new NotImplementedException();
        //        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    public byte[] getRecordedData() {
        return data.getBytes();
    }

    public long getRecodedLength() {
        return data.getLength();
    }
}
