package com.logginghub.logging.frontend.configuration;

import com.logginghub.logging.filters.TimeFieldFilter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;


@XmlAccessorType(XmlAccessType.FIELD)
public class CustomDateFilterConfiguration {

    @XmlAttribute private String label;
    @XmlAttribute private String field;
    @XmlAttribute private String type;
    @XmlAttribute private long defaultValue = TimeFieldFilter.ACCEPT_ALL;
    @XmlAttribute private int width = 100;

    public String getType() {
        return type;
    }

    public long getDefaultValue() {
        return defaultValue;
    }

    public String getField() {
        return field;
    }

    public String getLabel() {
        return label;
    }

    public int getWidth() {
        return width;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDefaultValue(long defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
