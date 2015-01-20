package com.logginghub.logging.frontend.visualisations.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class VectorConfig {

    // This is kind of a union
    @XmlAttribute private double x;
    @XmlAttribute private double y;

    @XmlAttribute private double angle = Double.NaN;
    @XmlAttribute private double magnitude = Double.NaN;

    public VectorConfig() {

    }

    public VectorConfig(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngular(double angle, double magnitude) {
        setAngle(angle);
        setMagnitude(magnitude);
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        double value;
        if (isAngular()) {
            double radians = Math.toRadians(angle);
            double calculatedX = magnitude * Math.cos(radians);
            value = calculatedX;
        }
        else {
            value = x;
        }

        return value;
    }

    public double getY() {
        double value;
        if (isAngular()) {
            double radians = Math.toRadians(angle);
            double calculatedY = magnitude * Math.sin(radians);
            value = calculatedY;
        }
        else {
            value = y;
        }

        return value;
    }

    public int getXInt() {
        return (int) getX();
    }

    public int getYInt() {
        return (int) getY();
    }

    @Override public String toString() {
        if (isAngular()) {
            return "VectorConfig [angle=" + angle + ", mag=" + magnitude + "]";
        }
        else {
            return "VectorConfig [x=" + x + ", y=" + y + "]";
        }
    }

    public boolean isAngular() {
        boolean isAngular;
        if (Double.isNaN(angle) || Double.isNaN(magnitude)) {
            isAngular = false;
        }
        else {
            isAngular = true;
        }
        return isAngular;
    }

}
