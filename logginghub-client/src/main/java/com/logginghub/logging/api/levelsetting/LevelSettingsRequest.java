package com.logginghub.logging.api.levelsetting;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class LevelSettingsRequest implements SerialisableObject {

    private InstanceFilter instanceFilter = new InstanceFilter();
    private LevelSettingsGroup levelSettings = new LevelSettingsGroup();

    @Override public void read(SofReader reader) throws SofException {
        this.instanceFilter = (InstanceFilter) reader.readObject(0);
        this.levelSettings = (LevelSettingsGroup) reader.readObject(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, instanceFilter);
        writer.write(1, levelSettings);
    }

    public InstanceFilter getFilter() {
        return instanceFilter;
    }

    public LevelSettingsGroup getLevelSettings() {
        return levelSettings;
    }

    public void setFilter(InstanceFilter filter) {
        this.instanceFilter = filter;
    }

    public void setLevelSettings(LevelSettingsGroup levelSettings) {
        this.levelSettings = levelSettings;
    }

    @Override public String toString() {
        return "LevelSettingsRequest [instanceFilter=" + instanceFilter + ", levelSettings=" + levelSettings + "]";
    }


    
}
