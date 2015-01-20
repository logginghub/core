package com.logginghub.logging.repository;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Timer;
import java.util.zip.Deflater;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.listeners.LogEventListener;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.TimerUtils;

public class BinaryLogFileWriter implements LogEventWriter, LogEventListener {

    private int bufferSize;

    private long lastEventTime;

    private Output buffer;
    private ByteBuffer compressionBuffer;

    private int flushAt;
    private Deflater deflator = new Deflater();

    private DateFormat fileDateFormat = new FileDateFormat();

    private long currentPeriodStart = 0;
    private long currentPeriodEnd = 0;

    private Kryo kryo;
    private File outputFolder;
    private DataOutputStream outputStream;

    private int events = 0;

    private File currentFile;

    private Timer statsTimer;

    public BinaryLogFileWriter(File fileDestination) {
        setBufferSize(10 * 1024 * 1024);
        kryo = new KryoWrapper();
        openFile(fileDestination);
    }

    private ByteBuffer allocateBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        return buffer;
    }

    public void startStatsTimer() {
        statsTimer = TimerUtils.everySecond("StatsTimer", new Runnable() {
            public void run() {
                System.out.println(String.format("Events processed %,5d : buffer size %.1f MB (%.0f%%)",
                                                 events,
                                                 buffer.position() / 1024f / 1024f,
                                                 100f * (buffer.position() / (float) flushAt)));
                events = 0;
            }
        });
    }

    public void write(LogEvent event) {
        events++;
     
        kryo.writeObject(buffer, event);

        if (buffer.position() > flushAt) {
            flush();
        }
    }

    private void overrideEventTime(LogEvent event) {
        DefaultLogEvent mutableEvent = (DefaultLogEvent) event;
        mutableEvent.setLocalCreationTimeMillis(System.currentTimeMillis());
    }



    private void openFile(File destination) {

        currentFile = destination;
//        System.out.println(String.format("Opening file '%s'", currentFile.getAbsolutePath()));

        boolean append = true;
        try {
            outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(currentFile, append)));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("Failed to open file '%s' for writing (append=true)", currentFile.getAbsolutePath()),
                                       e);
        }
    }

    public void flush() {

        // We make the bold assumption that the compressed data will be smaller
        // than the original. As we are concatenating quite a few events
        // together, this is bound to be true! (I hope!)

        int inflatedSize = buffer.position();
        deflator.setInput(buffer.getBuffer(), 0, inflatedSize);
        deflator.finish();
        int deflatedSize = deflator.deflate(compressionBuffer.array());
        deflator.reset();

        try {
            outputStream.writeInt(deflatedSize);
            outputStream.writeInt(inflatedSize);
            outputStream.write(compressionBuffer.array(), 0, deflatedSize);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write compressed data to stream", e);
        }

        buffer.clear();
        compressionBuffer.clear();
    }


    public void close() {
        if (statsTimer != null) {
            statsTimer.cancel();
            statsTimer = null;
        }
        flush();
        FileUtils.closeQuietly(outputStream);
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setBufferSize(long sizeInBytes) {
        bufferSize = (int) sizeInBytes;        
        buffer = new Output(bufferSize);
        compressionBuffer = allocateBuffer();
        flushAt = (int) (bufferSize * 0.8f);
    }

    @Override public void open() throws IOException {
        // Opens automatically in the constructor
    }

    @Override public void onNewLogEvent(LogEvent event) {
        write(event);
    }
}
