package com.logginghub.logging.messages;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class HealthCheckResponse implements SerialisableObject {

    private String type;
    private String content;
    private boolean moreToFollow;

    public HealthCheckResponse() {}

    public void setMoreToFollow(boolean moreToFollow) {
        this.moreToFollow = moreToFollow;
    }
    
    public boolean isMoreToFollow() {
        return moreToFollow;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void read(SofReader reader) throws SofException {
        this.type = reader.readString(1);
        this.content = reader.readString(2);
        this.moreToFollow = reader.readBoolean(3);
    }

    public void write(SofWriter writer) throws SofException {
        writer.write(1, type);
        writer.write(2, content);
        writer.write(3, moreToFollow);
    }

}
