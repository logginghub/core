package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD) public class PatternConfiguration {

    @XmlAttribute int patternID;
    @XmlAttribute String pattern;
    @XmlAttribute String name;

    @XmlAttribute boolean debug = false;
    @XmlAttribute boolean cleanup = true;

    public PatternConfiguration() {}

    public PatternConfiguration(int patternID, String name, String pattern) {
        this.patternID = patternID;
        this.name = name;
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

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public int getPatternID() {
        return patternID;
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }
  
    @Override public String toString() {
        return "PatternConfiguration [patternID=" +
               patternID +
               ", pattern=" +
               pattern +
               ", name=" +
               name +
               ", debug=" +
               debug +
               ", cleanup=" +
               cleanup +
               "]";
    }

}
