package com.logginghub.logging.frontend.visualisations;

public class GravityUpdateStrategy implements ModleUpdateStrategy{

    public static final double GRAVITY = -4;

    @Override public void updateAll(double time, VisualiserModel model) {}

    @Override public void updateEntity(double time, Entity entity) {
        entity.getVelocity().y += time * GRAVITY;
    }

}
