package com.logginghub.logging.frontend.visualisations.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class TriggerConfig {

    @XmlAttribute private int pattern = -1;
    @XmlAttribute private String environment = "";
    @XmlAttribute private int label = 0;
    @XmlAttribute private double maximumValue = 100;
    @XmlAttribute private double sizeFactor = 1;
    @XmlAttribute private double maximumSize = Double.NaN;
    @XmlAttribute private double minimumSize = Double.NaN;
    @XmlAttribute private double velocityFactor = 1;
    @XmlAttribute private String colourGradient = "";
    @XmlAttribute private String host = "";
    @XmlAttribute private String application = "";
    @XmlAttribute private double minimumVelocity = 0;

    public double getMaximumSize() {
        return maximumSize;
    }
    
    public double getMinimumSize() {
        return minimumSize;
    }
    
    public double getMaximumValue() {
        return maximumValue;
    }

    public String getHost() {
        return host;
    }

    public String getApplication() {
        return application;
    }

    public void setMaximumValue(double maximumValue) {
        this.maximumValue = maximumValue;
    }

    public String getColourGradient() {
        return colourGradient;
    }

    public void setColourGradient(String colourGradient) {
        this.colourGradient = colourGradient;
    }

    public void setSizeFactor(double sizeFactor) {
        this.sizeFactor = sizeFactor;
    }

    public void setVelocityFactor(double velocityFactor) {
        this.velocityFactor = velocityFactor;
    }

    public double getSizeFactor() {
        return sizeFactor;
    }

    public double getVelocityFactor() {
        return velocityFactor;
    }

    public int getPattern() {
        return pattern;
    }

    public void setPattern(int pattern) {
        this.pattern = pattern;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public void setMaximumSize(double maximumSize) {
        this.maximumSize = maximumSize;
    }

    public void setMinimumSize(double minimumSize) {
        this.minimumSize = minimumSize;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public double getMinimumVelocity() {
        return minimumVelocity;
    }
    
    public void setMinimumVelocity(double minimumVelocity) {
        this.minimumVelocity = minimumVelocity;
    }
}
