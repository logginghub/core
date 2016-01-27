package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;


@XmlAccessorType(XmlAccessType.FIELD)
public class CustomFilterConfiguration {

    @XmlAttribute private String label;
    @XmlAttribute private String field;
    @XmlAttribute private String type;
    @XmlAttribute private String defaultValue = "";
    @XmlAttribute private int width = 100;
    @XmlAttribute private String choices = "";

    public String getChoices() {
        return choices;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue() {
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

    public void setDefaultValue(String defaultValue) {
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
