package com.logginghub.logging.repository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;
import com.logginghub.utils.sof.StreamWriterAbstraction;

public class DiskIndex {

    private File file;

    private List<DiskIndexElement> elements = new ArrayList<DiskIndexElement>();

    private StreamWriterAbstraction streamWriterAbstraction;
    private SofConfiguration configuration = new SofConfiguration();

    public DiskIndex(File file) {
        this.file = file;
        configuration.registerType(DiskIndexElement.class, 0);
    }
    
    public List<DiskIndexElement> getElements() {
        return elements;
    }

    public synchronized void write(DiskIndexElement element) throws IOException {

        if (streamWriterAbstraction == null) {
            FileOutputStream fis = new FileOutputStream(file);
            BufferedOutputStream bis = new BufferedOutputStream(fis);
            streamWriterAbstraction = new StreamWriterAbstraction(bis);
        }

        try {
            SofSerialiser.write(streamWriterAbstraction, element, configuration);
        }
        catch (SofException e) {
            throw new IOException(e);
        }

    }

    public synchronized void close() {
        if (streamWriterAbstraction != null) {
            FileUtils.closeQuietly(streamWriterAbstraction);
            streamWriterAbstraction = null;
        }
    }
    
    public void readElements() throws IOException {

        elements.clear();

        if (file.exists()) {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            SofSerialiser.readAll(bis, DiskIndexElement.class, new Destination<DiskIndexElement>() {
                @Override public void send(DiskIndexElement t) {
                    elements.add(t);
                }
            });
        }

    }

    public File getFile() {
        return file;
         
    }
}
