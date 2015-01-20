package com.logginghub.logging.api.levelsetting;

import com.logginghub.logging.api.patterns.InstanceDetails;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class LevelSettingsConfirmation implements SerialisableObject {

    private InstanceDetails instanceDetails = new InstanceDetails();
    
    @Override public void read(SofReader reader) throws SofException {
        instanceDetails =  (InstanceDetails) reader.readObject(0);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, instanceDetails);
    }

    public void setInstanceDetails(InstanceDetails instanceDetails) {
        this.instanceDetails = instanceDetails;
    }
    
    public InstanceDetails getInstanceDetails() {
        return instanceDetails;
    }
    
    
}
