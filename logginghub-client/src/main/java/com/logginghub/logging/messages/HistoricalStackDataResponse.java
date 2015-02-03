package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalStackDataResponse extends BaseRequestResponseMessage implements SerialisableObject {

    private StackSnapshot[] snapshots;

    private CompressedBlock<StackSnapshot> compressedBlock = new CompressedBlock<StackSnapshot>();

    private boolean lastBatch;

    public HistoricalStackDataResponse() {}

    public StackSnapshot[] getSnapshots() {
        return snapshots;
    }

    private int jobNumber;

    public void setSnapshots(StackSnapshot[] snapshots) {
        this.snapshots = snapshots;
    }

    @SuppressWarnings("unchecked") public void read(SofReader reader) throws SofException {
        setCorrelationID(reader.readInt(1));
        this.compressedBlock = (CompressedBlock<StackSnapshot>) reader.readObject(2);
        compressedBlock.setSofConfiguration(reader.getConfiguration());
        this.snapshots = compressedBlock.decodeAll(StackSnapshot.class);
        this.lastBatch = reader.readBoolean(3);
        this.jobNumber = reader.readInt(4);
    }

    public void write(SofWriter writer) throws SofException {
        compressedBlock.clear();
        compressedBlock.addAll(snapshots);
        writer.write(1, getCorrelationID());
        writer.write(2, compressedBlock);
        writer.write(3, lastBatch);
        writer.write(4, jobNumber);
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public void setLastBatch(boolean lastBatch) {
        this.lastBatch = lastBatch;
    }

    public boolean isLastBatch() {
        return lastBatch;
    }
}
