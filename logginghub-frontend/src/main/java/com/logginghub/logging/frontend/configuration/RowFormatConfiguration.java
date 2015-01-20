package com.logginghub.logging.frontend.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class RowFormatConfiguration {

    @XmlAttribute private String foregroundColour = "black";
    @XmlAttribute private String backgroundColour = "cyan";
    @XmlAttribute private String borderColour = "black";
    @XmlAttribute private int borderLineWidth = 1;
    @XmlAttribute private String font = null;

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public String getBorderColour() {
        return borderColour;
    }

    public int getBorderLineWidth() {
        return borderLineWidth;
    }

    public String getFont() {
        return font;
    }

    public String getForegroundColour() {
        return foregroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public void setBorderColour(String borderColour) {
        this.borderColour = borderColour;
    }

    public void setBorderLineWidth(int borderLineWidth) {
        this.borderLineWidth = borderLineWidth;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public void setForegroundColour(String foregroundColour) {
        this.foregroundColour = foregroundColour;
    }
}
