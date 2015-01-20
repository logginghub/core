package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.logginghub.logging.frontend.modules.ViewDetails;

public class RotateCentreScreenPlacementStrategy implements PlacementStrategy {

    private int angle = 0;
    private Random random = new Random();
    private float radius = 3;
    private ViewDetails viewDetails;

    public RotateCentreScreenPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        float x = (float) Math.sin(Math.toRadians(angle));
        float y = (float) Math.cos(Math.toRadians(angle));

        int centreX = viewDetails.width / 2;
        int centreY = viewDetails.height / 2;

        float releaseX = centreX + (radius * x);
        float releaseY = centreY + (radius * y);

        entity.getPosition().set(releaseX, releaseY, 0);
        entity.getVelocity().set(x, y, 0).scl(2);
        angle += 3.1;
    }

    public void update(float time) {

    }
}
