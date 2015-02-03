package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HistoricalStackDataJobKillRequest extends BaseRequestResponseMessage implements SerialisableObject {

    private int jobNumber;

    public HistoricalStackDataJobKillRequest() {}

    public HistoricalStackDataJobKillRequest(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public void read(SofReader reader) throws SofException {
        setCorrelationID(reader.readInt(1));
        this.jobNumber = reader.readInt(2);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, getCorrelationID());
        writer.write(2, jobNumber);
    }

}
