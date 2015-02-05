package com.logginghub.logging.messages;

import com.logginghub.utils.Result;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportExecuteResponse extends BaseRequestResponseMessage {

    private Result<ReportExecuteResult> result;
    private InstanceKey instanceKey;


    @Override public void read(SofReader reader) throws SofException {
        super.read(reader);
        result = (Result<ReportExecuteResult>) reader.readObject(1);
        instanceKey = (InstanceKey) reader.readObject(2);
    }

    @Override public void write(SofWriter writer) throws SofException {
        super.write(writer);
        writer.write(1, result);
        writer.write(2, instanceKey);
    }

    public void setResult(Result<ReportExecuteResult> result) {
        this.result = result;
    }

    public Result<ReportExecuteResult> getResult() {
        return result;
    }

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("ReportExecuteResponse{");
        sb.append("result=").append(result);
        sb.append(", instanceKey=").append(instanceKey);
        sb.append('}');
        return sb.toString();
    }
}
