package com.logginghub.logging.frontend.configuration;

import com.logginghub.logging.frontend.views.logeventdetail.DetailedLogEventTableModel.ColumnTarget;

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
    @XmlAttribute private String metadata;
    @XmlAttribute private String renderer = ColumnTarget.Renderer.Normal.name();

    public String getAlignment() {
        return alignment;
    }

    public String getMetadata() {
        return metadata;
    }

    public int getOrder() {
        return order;
    }

    public String getRenderer() {
        return renderer;
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

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setRenderer(String renderer) {
        this.renderer = renderer;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
