package com.logginghub.logging.api.patterns;

import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

/**
 * Represents the capture pattern and label structure for data extraction.
 * 
 * @author James
 * 
 */
public class Pattern implements SerialisableObject {

    private int patternId = -1;
    private String pattern = "";
    private String name = "";
    private boolean debug = false;
    private boolean cleanup = false;

    public Pattern(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public Pattern() {}

    public Pattern(int patternId, String name, String pattern) {
        this.name = name;
        this.patternId = patternId;
        this.pattern = pattern;
    }

    public int getPatternId() {
        return patternId;
    }

    public void setPatternId(int patternId) {
        this.patternId = patternId;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void setCleanup(boolean cleanup) {
        this.cleanup = cleanup;
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (cleanup ? 1231 : 1237);
        result = prime * result + (debug ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + patternId;
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pattern other = (Pattern) obj;
        if (cleanup != other.cleanup) {
            return false;
        }
        if (debug != other.debug) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        }
        else if (!pattern.equals(other.pattern)) {
            return false;
        }
        if (patternId != other.patternId) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return "PatternModel [patternId=" + patternId + ", pattern=" + pattern + ", name=" + name + ", debug=" + debug + ", cleanup=" + cleanup + "]";
    }

    @Override public void read(SofReader reader) throws SofException {
        this.patternId = reader.readInt(0);
        this.name = reader.readString(1);
        this.pattern = reader.readString(2);
        this.debug = reader.readBoolean(3);
        this.cleanup = reader.readBoolean(4);
    }

    @Override public void write(SofWriter writer) throws SofException {
        writer.write(0, patternId);
        writer.write(1, name);
        writer.write(2, pattern);
        writer.write(3, debug);
        writer.write(4, cleanup);
    }

}
