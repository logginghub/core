package com.logginghub.logging.repository;

import com.logginghub.logging.messages.HistoricalIndexElement;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class DiskIndexElement extends HistoricalIndexElement implements SerialisableObject {

    private String dataFile;
    private long startPosition;
    private long endPosition;

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public void read(SofReader reader) throws SofException {
        super.read(reader);
        dataFile = reader.readString(7);
        startPosition = reader.readLong(8);
        endPosition = reader.readLong(8);
    }

    public void write(SofWriter writer) throws SofException {
        super.write(writer);
        writer.write(7, dataFile);
        writer.write(8, startPosition);
        writer.write(9, endPosition);
    }

    public void set(DiskIndexElement other) {
        super.set(other);
        this.dataFile = other.dataFile;
        this.startPosition = other.startPosition;
        this.endPosition = other.endPosition;
    }

    @Override public String toString() {
        return "DiskIndexElement [dataFile=" +
               dataFile +
               ", time=" +
               getTime() +
               ", startPosition=" +
               startPosition +
               ", endPosition=" +
               endPosition +
               ", interval=" +
               getInterval() +
               ", infoCount=" +
               getInfoCount() +
               ", warningCount=" +
               getWarningCount() +
               ", severeCount=" +
               getSevereCount() +
               ", otherCount=" +
               getOtherCount() +
               "]";
    }

    public long getStartPosition() {
        return startPosition;
    }
    
    public long getEndPosition() {
        return endPosition;
    }
    
    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }
    
    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

}
