package com.logginghub.logging.api.levelsetting;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class LevelSettingsGroup implements SerialisableObject {

    private List<LevelSetting> settings = new ArrayList<LevelSetting>();

    public void add(LevelSetting setting) {
        settings.add(setting);
    }
    
    public List<LevelSetting> getSettings() {
        return settings;
    }
    
    @Override public void read(SofReader reader) throws SofException {
        int index = 0;
        int count = reader.readInt(index++);
        for (int i = 0; i < count; i++) {
            settings.add((LevelSetting) reader.readObject(index++));
        }
    }

    @Override public void write(SofWriter writer) throws SofException {
        int index = 0;
        writer.write(index++, settings.size());
        for (LevelSetting levelSettings : settings) {
            writer.write(index++, levelSettings);
        }
    }

    @Override public String toString() {
        return "LevelSettingsGroup [settings=" + settings + "]";
    }
    
    

}
