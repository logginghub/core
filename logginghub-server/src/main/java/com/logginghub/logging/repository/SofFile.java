package com.logginghub.logging.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofRuntimeException;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamReaderAbstraction;
import com.logginghub.utils.sof.StreamWriterAbstraction;

public class SofFile {

    private File file;

    private StreamWriterAbstraction streamWriterAbstraction;
    private static SofConfiguration configuration = new SofConfiguration();

    static {
        configuration.registerType(DiskIndexElement.class, 0);
        configuration.registerType(DefaultLogEvent.class, 1);
    }

    public SofFile(File file) {
        this.file = file;
    }

    public void write(SerialisableObject item) throws IOException {
        if (streamWriterAbstraction == null) {
            FileOutputStream fis = new FileOutputStream(file);
            BufferedOutputStream bis = new BufferedOutputStream(fis);
            streamWriterAbstraction = new StreamWriterAbstraction(bis);
        }

        try {
            SofSerialiser.write(streamWriterAbstraction, item, configuration);
        }
        catch (SofException e) {
            throw new IOException(e);
        }
    }

    public void close() {
        FileUtils.closeQuietly(streamWriterAbstraction);
    }

    public File getFile() {
        return file;
    }

    public long getPosition() {
        if (streamWriterAbstraction == null) {
            return 0;
        }
        else {
            return streamWriterAbstraction.getPosition();
        }
    }

    public static <T extends SerialisableObject> void readAll(Class<T> clazz, File file, Destination<T> destination) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        SofSerialiser.readAll(bis, clazz, destination, configuration);
        FileUtils.closeQuietly(bis);
    }

    public static <T extends SerialisableObject> List<T> readAll(Class<T> clazz, File dataFile) throws IOException {
        final List<T> list = new ArrayList<T>();
        readAll(clazz, dataFile, new Destination<T>() {
            @Override public void send(T t) {
                list.add(t);
            }
        });

        return list;
    }

    public static <T extends SerialisableObject> void readBetween(long startPosition,
                                                                  long endPosition,
                                                                  Class<T> clazz,
                                                                  File file,
                                                                  Destination<T> destination) throws IOException {
        
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.skip(startPosition);
        long limit = endPosition - startPosition;
        StreamReaderAbstraction streamReaderAbstraction = new StreamReaderAbstraction(bis, (int) limit);
        try {

            while (streamReaderAbstraction.getPosition() < limit) {
                T t = SofSerialiser.read(streamReaderAbstraction, configuration);
                destination.send(t);
            }
        }
        catch (SofException e) {
            if (e.getCause() instanceof EOFException) {
                // Fine
            }
            else {
                throw new SofRuntimeException(e);
            }
        }
        FileUtils.closeQuietly(bis);
        
    }

    public void flush() throws IOException {
        streamWriterAbstraction.flush();
    }

}
