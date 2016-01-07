package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Encapsulates settings for a particular column
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ColumnConfiguration {
    @XmlAttribute private String name;
    @XmlAttribute private int width;
    @XmlAttribute private int order;
    @XmlAttribute private String alignment;

    public String getAlignment() {
        return alignment;
    }

    public int getOrder() {
        return order;
    }

    public int getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
