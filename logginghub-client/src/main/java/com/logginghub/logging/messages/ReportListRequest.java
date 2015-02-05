package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Created by james on 04/02/15.
 */
public class ReportListRequest extends BaseRequestResponseMessage {

    private int respondToChannel;

    public int getRespondToChannel() {
        return respondToChannel;
    }

    public void setRespondToChannel(int respondToChannel) {
        this.respondToChannel = respondToChannel;
    }

    @Override public void read(SofReader reader) throws SofException {
        super.read(reader);
        respondToChannel = reader.readInt(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        super.write(writer);
        writer.write(1, respondToChannel);
    }
}
