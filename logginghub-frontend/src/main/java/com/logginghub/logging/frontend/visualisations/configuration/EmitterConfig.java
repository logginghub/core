package com.logginghub.logging.frontend.visualisations.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class EmitterConfig {

    @XmlElement private VectorConfig sourcePosition = new VectorConfig();
    @XmlElement private VectorConfig releaseVelocity = new VectorConfig();
    @XmlElement private VectorConfig directionVariance = new VectorConfig();

    @XmlAttribute private boolean showGui = false;
    @XmlAttribute private String name = "";
    @XmlAttribute private double lifeLimit = 150;
    @XmlAttribute private double gravity = -0.01;

    @XmlElement private TriggerConfig trigger;
    @XmlElement private LevelTriggerConfig levelTrigger;

    @XmlElement private VectorConfig testConnectionRate = new VectorConfig(0, 0);
    @XmlAttribute private double sizeTimeMultipler = 1;
    @XmlAttribute private int particleLimit = 5000;

    public LevelTriggerConfig getLevelTrigger() {
        return levelTrigger;
    }
    
    public TriggerConfig getTrigger() {
        return trigger;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VectorConfig getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(VectorConfig sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public VectorConfig getReleaseVelocity() {
        return releaseVelocity;
    }

    public void setReleaseVelocity(VectorConfig releaseVelocity) {
        this.releaseVelocity = releaseVelocity;
    }

    public VectorConfig getDirectionVariance() {
        return directionVariance;
    }

    public void setDirectionVariance(VectorConfig directionVariance) {
        this.directionVariance = directionVariance;
    }

    public boolean isShowGui() {
        return showGui;
    }

    public void setShowGui(boolean showGui) {
        this.showGui = showGui;
    }

    public VectorConfig getTestConnectionRate() {
        return testConnectionRate;
    }

    public void setTestConnectionRate(VectorConfig testConnectionRate) {
        this.testConnectionRate = testConnectionRate;
    }

    public double getSizeTimeMultipler() {
        return sizeTimeMultipler;
    }

    public void setSizeTimeMultipler(double sizeTimeMultipler) {
        this.sizeTimeMultipler = sizeTimeMultipler;
    }

    @Override public String toString() {
        return "EmitterConfig [sourcePosition=" + sourcePosition + ", name=" + name + "]";
    }

    public double getLifeLimit() {
        return lifeLimit;
    }

    public void setLifeLimit(double lifeLimit) {
        this.lifeLimit = lifeLimit;
    }

    public int getParticleLimit() {
        return particleLimit;
         
    }

    public double getGravity() {
        return gravity;
    }
    
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
    
}
