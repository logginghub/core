package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportRunRequest extends BaseRequestResponseMessage {

    private String reportName;

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportName() {
        return reportName;
    }

    @Override public void read(SofReader reader) throws SofException {
        super.read(reader);
        reportName = reader.readString(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        super.write(writer);
        writer.write(1, reportName);
    }
}
