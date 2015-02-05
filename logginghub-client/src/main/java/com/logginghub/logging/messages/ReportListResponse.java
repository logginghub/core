package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by james on 04/02/15.
 */
public class ReportListResponse extends BaseRequestResponseMessage {

    private InstanceKey instanceKey;


    private List<ReportDetails> reportDetails = new ArrayList<ReportDetails>();

    public List<ReportDetails> getReportDetails() {
        return reportDetails;
    }

    @Override public void read(SofReader reader) throws SofException {
        super.read(reader);
        ReportDetails[] reportDetailses = (ReportDetails[]) reader.readObjectArray(1, ReportDetails.class);
        Collections.addAll(this.reportDetails, reportDetailses);
        instanceKey = (InstanceKey) reader.readObject(2);
    }

    @Override public void write(SofWriter writer) throws SofException {
        super.write(writer);
        final ReportDetails[] reportsArray = reportDetails.toArray(new ReportDetails[reportDetails.size()]);
        writer.write(1, reportsArray, ReportDetails.class);
        writer.write(2, instanceKey);
    }

    public void setInstanceKey(InstanceKey instanceKey) {
        this.instanceKey = instanceKey;
    }

    public InstanceKey getInstanceKey() {
        return instanceKey;
    }
}
