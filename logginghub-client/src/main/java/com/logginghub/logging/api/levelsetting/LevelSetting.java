package com.logginghub.logging.api.levelsetting;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class LevelSetting implements SerialisableObject {

    private String loggerName;
    private String level;

    public LevelSetting(String loggerName, String level) {
        super();
        this.loggerName = loggerName;
        this.level = level;
    }

    public LevelSetting() {}

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override public void read(SofReader reader) throws SofException {
        this.loggerName = reader.readString(0);
        this.level = reader.readString(1);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, loggerName);
        writer.write(1, level);
    }

    @Override public String toString() {
        return "LevelSettings [loggerName=" + loggerName + ", level=" + level + "]";
    }

}
