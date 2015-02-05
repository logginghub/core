package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportRunResponse extends BaseRequestResponseMessage {

    private String result;

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    @Override public void read(SofReader reader) throws SofException {
        super.read(reader);
        result = reader.readString(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        super.write(writer);
        writer.write(1, result);
    }
}
