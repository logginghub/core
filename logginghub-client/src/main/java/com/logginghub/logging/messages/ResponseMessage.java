package com.logginghub.logging.messages;

import com.logginghub.utils.Result;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class ResponseMessage implements RequestResponseMessage, SerialisableObject {

    private SerialisableObject payload;
    private int correlationID;
    private String failureReason;
    private String unsuccessfulReason;

    public ResponseMessage() {
    }

    public ResponseMessage(int correlationID) {
        super();
        this.correlationID = correlationID;
    }

    public ResponseMessage(int correlationID, SerialisableObject payload) {
        super();
        this.correlationID = correlationID;
        this.payload = payload;
    }

    public void read(SofReader reader) throws SofException {
        this.correlationID = reader.readInt(0);
        this.failureReason = reader.readString(1);
        this.unsuccessfulReason = reader.readString(2);
        this.payload = (SerialisableObject) reader.readObject(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(0, correlationID);
        writer.write(1, failureReason);
        writer.write(2, unsuccessfulReason);
        writer.write(3, payload);
    }

    public SerialisableObject getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "ResponseMessage [correlationID=" + correlationID + ", payload=" + payload + "]";
    }

    public boolean wasSuccessful() {
        return failureReason == null && unsuccessfulReason == null;
    }

    public boolean wasFailure() {
        return failureReason != null;
    }

    public boolean wasUnsuccessful() {
        return unsuccessfulReason != null;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getUnsuccessfulReason() {
        return unsuccessfulReason;
    }

    public void setupResult(Result<?> result) {
        if (result.isFailure()) {
            this.failureReason = result.getExternalReason();
        } else if (result.isUnsuccessful()) {
            this.unsuccessfulReason = result.getExternalReason();
        }
    }

    @Override
    public void setCorrelationID(int requestID) {
        this.correlationID = requestID;
    }

    @Override
    public int getCorrelationID() {
        return correlationID;
    }
}
