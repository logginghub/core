package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.logginghub.logging.frontend.modules.ViewDetails;

public class FountainPlacementStrategy implements PlacementStrategy {

    private Random random = new Random();
    private ViewDetails viewDetails;

    public FountainPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        entity.getPosition().set(viewDetails.width / 2, viewDetails.height / 20, 0);

        float x = (0.20f - (0.4f * random.nextFloat()));

        double vertical = viewDetails.height - viewDetails.height / 20;

        // Random abuse of the equations of motion
        double v = 0;
        double fudge = 0.010 * particleSettings.getVelocity();
        double a = fudge * GravityUpdateStrategy.GRAVITY;
        double s = vertical;

        double u = Math.sqrt(-2 * s * a);
        double t = Math.sqrt((-2 * s) / a);

        double initialVelocity = u;
        entity.getVelocity().set(x, 1, 0).scl((float) initialVelocity);

        entity.setLifeLimit(t/30);
    }

    @Override public void update(float time) {}

}
