package com.logginghub.logging.frontend.visualisations.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.badlogic.gdx.graphics.Color;
import com.logginghub.utils.ColourUtils;

@XmlAccessorType(XmlAccessType.FIELD) public class BoxConfig {
    @XmlAttribute private double x;
    @XmlAttribute private double y;
    @XmlAttribute private double width;
    @XmlAttribute private double height;
    @XmlAttribute private String colour;
    @XmlAttribute private String borderColour;
    @XmlAttribute private int borderWidth=1;
    @XmlAttribute private String text="";
    @XmlAttribute private double textX;
    @XmlAttribute private double textY;
    
    private Color libGDXColour;
    private Color libGDXBorderColour;
    
    public String getBorderColour() {
        return borderColour;
    }
    
    public int getBorderWidth() {
        return borderWidth;
    }
    
    public void setBorderColour(String borderColour) {
        this.borderColour = borderColour;
        java.awt.Color parseColor = ColourUtils.parseColor(borderColour);
        libGDXBorderColour = new Color(parseColor.getRed(), parseColor.getGreen(), parseColor.getBlue(),  parseColor.getAlpha());
    }
    
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public double getTextX() {
        return textX;
    }
    
    public double getTextY() {
        return textY;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getColour() {
        return colour;
    }
    
    public void setColour(String colour) {
        this.colour = colour;
        java.awt.Color parseColor = ColourUtils.parseColor(colour);
        libGDXColour = new Color(parseColor.getRed(), parseColor.getGreen(), parseColor.getBlue(),  parseColor.getAlpha());
    }
    
    public double getHeight() {
        return height;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }

    public Color getLibGDXColour() {
        if(libGDXColour == null) {
            setColour(colour);
        }
        return libGDXColour;         
    }
    
    public Color getLibGDXBorderColour() {
        if(libGDXBorderColour ==null){
            setBorderColour(borderColour);
        }
        return libGDXBorderColour;
    }
}
