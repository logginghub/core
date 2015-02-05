package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportExecuteResult implements SerialisableObject {

    private String result;
    private int returnCode;

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override public void read(SofReader reader) throws SofException {
        result = reader.readString(1);
        returnCode = reader.readInt(2);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(1, result);
        writer.write(2, returnCode);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("ReportExecuteResult{");
        sb.append("result='").append(result).append('\'');
        sb.append(", returnCode=").append(returnCode);
        sb.append('}');
        return sb.toString();
    }
}

