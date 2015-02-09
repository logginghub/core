package com.logginghub.logging.messages;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalAggregatedDataResponse extends BaseRequestResponseMessage implements SerialisableObject {

    private AggregatedLogEvent[] events;

    private CompressedBlock<AggregatedLogEvent> compressedBlock = new CompressedBlock<AggregatedLogEvent>();

    private boolean lastBatch;

    public HistoricalAggregatedDataResponse() {}

    public AggregatedLogEvent[] getEvents() {
        return events;
    }

    public void setEvents(AggregatedLogEvent[] events) {
        this.events = events;
    }

    @SuppressWarnings("unchecked") public void read(SofReader reader) throws SofException {
        setCorrelationID(reader.readInt(1));
        this.compressedBlock = (CompressedBlock<AggregatedLogEvent>) reader.readObject(2);
        compressedBlock.setSofConfiguration(reader.getConfiguration());
        this.events = compressedBlock.decodeAll(AggregatedLogEvent.class);
        this.lastBatch = reader.readBoolean(3);
    }

    public void write(SofWriter writer) throws SofException {
        compressedBlock.clear();
        compressedBlock.addAll(events);
        writer.write(1, getCorrelationID());
        writer.write(2, compressedBlock);
        writer.write(3, lastBatch);
        
    }

    public void setLastBatch(boolean lastBatch) {
        this.lastBatch = lastBatch;
    }

    public boolean isLastBatch() {
        return lastBatch;
    }
}
