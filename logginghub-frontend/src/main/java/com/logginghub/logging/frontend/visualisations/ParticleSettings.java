package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

public class ParticleSettings {

    private Color color;
    private double factor;
    private double velocity;
    private double velocity2;
    private double size;
    private float mass;
    private float lifetime;

    public ParticleSettings(Color color, double factor, double velocity, double size, double velocity2) {
        this.color = color;
        this.factor = factor;
        this.velocity = velocity;
        this.size = size;
        this.velocity2 = velocity2;
    }

    public ParticleSettings() {
        this(Color.white, 1, 1, 1, 1);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public double getVelocity() {
        return velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public double getVelocity2() {
        return velocity2;
    }

    public void setVelocity2(double velocity2) {
        this.velocity2 = velocity2;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getMass() {
        return mass;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public float getLifetime() {
        return lifetime;
    }
    
}
