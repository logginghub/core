package com.logginghub.logging.messages;

import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalIndexRequest extends BaseRequestResponseMessage implements SerialisableObject {

    // TODO : these are risky values to default to, they could end up loading the entire history
    private long start = 0;
    private long end = TimeUtils.parseTimeUTC("00:00:00 1/1/2100");

    public HistoricalIndexRequest() {}

    public HistoricalIndexRequest(long start, long end) {
        super();
        this.start = start;
        this.end = end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }

    public void read(SofReader reader) throws SofException {
        setCorrelationID(reader.readInt(1));
        this.start = reader.readLong(2);
        this.end = reader.readLong(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, getCorrelationID());
        writer.write(2, start);
        writer.write(3, end);
    }

}
