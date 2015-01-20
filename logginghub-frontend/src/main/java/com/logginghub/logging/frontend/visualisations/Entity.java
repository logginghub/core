package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

public class Entity {

    // 38830595

    public Entity previous;
    public Entity next;

    private Logger logger = Logger.getLoggerFor(Entity.class);
    private Vector3 position = new Vector3(0, 0, 0);
    private Vector3 startingPosition = new Vector3(0, 0, 0);
    private Vector3 velocity = new Vector3(0, 0, 0);
    private boolean isAlive = true;
    private Color color = Color.red;
    private double lifetime;
    private double lifeLimit = 10;
    // private EntitySource source;
    private double size;
    private float rotation;
    private float spin = -1;
    // private double gravity = -0.02;
    private float mass;
    private int id;

    public Entity naturalNext;
    private double sizeIncrement = 0;

    // public Entity(EntitySource parent) {
    // this.source = parent;
    // }
    //
    // public EntitySource getSource() {
    // return source;
    // }

    public Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void update(double time) {

        position.add(velocity);

        // logger.trace("Updated position to {} (velocity {})", position, velocity);
        // Apply gravity
        // velocity.y += gravity;
        lifetime += time;
        rotation += spin;
    }

    @Override public String toString() {
        return StringUtils.format("Entity [id='{}' prev='{}' next='{}' isAlive='{}' position='{}' velocity='{}' lifetime='{}'",
                                  id,
                                  previous == null ? "null" : previous.id,
                                  next == null ? "null" : next.id,
                                  isAlive,
                                  position,
                                  velocity,
                                  lifetime);
    }

    public void setLifetime(double lifetime) {
        this.lifetime = lifetime;
    }

    public double getLifetime() {
        return lifetime;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setAlive(boolean b) {
        this.isAlive = b;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public float getRotation() {
        return rotation;
    }

    public double getLifeLimit() {
        return lifeLimit;
    }

    public void setLifeLimit(double lifeLimit) {
        this.lifeLimit = lifeLimit;
    }

    // public void setGravity(double gravity) {
    // this.gravity = gravity;
    // }
    //
    // public double getGravity() {
    // return gravity;
    // }

    public void setSpin(float spin) {
        this.spin = spin;
    }

    public float getSpin() {
        return spin;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getMass() {
        return mass;
    }

    public double getSizeIncrement() {
        return sizeIncrement;
    }

    public void setSizeIncrement(double sizeIncrement) {
        this.sizeIncrement = sizeIncrement;
    }

    public void setStartingPosition(Vector3 startingPosition) {
        this.startingPosition = startingPosition;
    }
    
    public Vector3 getStartingPosition() {
        return startingPosition;
    }
}
