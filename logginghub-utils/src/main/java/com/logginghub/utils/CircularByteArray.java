package com.logginghub.utils;

/**
 * Created by james on 24/08/15.
 */
public class CircularByteArray {
    private byte[] data;
    private int currentPosition;
    private int size;
    private boolean wrapped = false;

    public CircularByteArray(int size) {
        this.size = size;
        data = new byte[this.size];
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        currentPosition = 0;
        wrapped = false;
    }

    public void write(byte[] buf, int pos, int len) {

        if (len > size) {
            int cut = len - size;
            // Only going to fit the last bit - so we may as well blat the entire array
            System.arraycopy(buf, pos + cut, data, 0, size);
            currentPosition = 0;
            wrapped = true;
        } else {
            // Going to fit everything, can we fit it after the currentPosition?
            int remaining = size - currentPosition;
            if (remaining >= len) {
                // Yes - easy single copy
                System.arraycopy(buf, pos, data, currentPosition, len);
                currentPosition += len;
            } else {
                // No - have to wrap the buffer around
                System.arraycopy(buf, pos, data, currentPosition, remaining);
                System.arraycopy(buf, pos + remaining, data, 0, len - remaining);
                currentPosition = len - remaining;
                wrapped = true;
            }

        }
    }

    public int getLength() {
        int length;
        if (wrapped) {
            length = size;
        } else {
            length = currentPosition;
        }
        return length;
    }

    public void write(byte value) {
        data[currentPosition] = value;
        currentPosition++;
        if (currentPosition == size) {
            currentPosition = 0;
            wrapped = true;
        }
    }

    public void write(byte[] buf) {
        write(buf, 0, buf.length);
    }

    public byte[] getBytes() {
        byte[] copy;
        if (wrapped) {
            copy = new byte[size];

            // Copy the oldest chunk, from the write pointer to the end of the array
            System.arraycopy(data, currentPosition, copy, 0, size - currentPosition);

            // Copy the youngest chunk, from the start to the write pointer
            System.arraycopy(data, 0, copy, size - currentPosition, currentPosition);

        } else {
            // Not full yet
            copy = new byte[currentPosition];
            System.arraycopy(data, 0, copy, 0, currentPosition);
        }

        return copy;
    }
}
