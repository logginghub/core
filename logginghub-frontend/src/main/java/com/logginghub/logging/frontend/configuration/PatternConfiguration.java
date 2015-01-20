package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.messaging.PatternModel;


/**
 * @deprecated This uses the 'value' field whereas the new versions use 'pattern'.  Check {@link PatternModel} or the new PatternConfiguration in vl-logging
 * @author James
 *
 */
@XmlAccessorType(XmlAccessType.FIELD) public class PatternConfiguration {

    @XmlAttribute String value;
    @XmlAttribute String name;

    @XmlAttribute boolean debug = false;
    @XmlAttribute boolean cleanup = true;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override public String toString() {
        return "PatternConfiguration [value=" + value + ", name=" + name + ", debug=" + debug + ", cleanup=" + cleanup + "]";
    }
    
    

}
