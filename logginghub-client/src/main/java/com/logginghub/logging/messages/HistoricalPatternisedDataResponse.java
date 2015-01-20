package com.logginghub.logging.messages;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalPatternisedDataResponse extends BaseRequestResponseMessage implements SerialisableObject {

    private PatternisedLogEvent[] events;

    private CompressedBlock<PatternisedLogEvent> compressedBlock = new CompressedBlock<PatternisedLogEvent>();

    private boolean lastBatch;

    public HistoricalPatternisedDataResponse() {
       
    }

    public PatternisedLogEvent[] getEvents() {
        return events;
    }

    public void setEvents(PatternisedLogEvent[] events) {
        this.events = events;
    }

    @SuppressWarnings("unchecked") public void read(SofReader reader) throws SofException {
        setCorrelationID(reader.readInt(1));
        this.compressedBlock = (CompressedBlock<PatternisedLogEvent>) reader.readObject(2);
        this.events = compressedBlock.decodeAll(PatternisedLogEvent.class);
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
