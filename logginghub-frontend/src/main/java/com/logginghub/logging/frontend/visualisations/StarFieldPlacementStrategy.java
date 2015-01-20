package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.logginghub.logging.frontend.modules.ViewDetails;

public class StarFieldPlacementStrategy implements PlacementStrategy {

    private Random random = new Random();
    private ViewDetails viewDetails;

    public StarFieldPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    public float randomRange(float minVal, float maxVal) {
        return (float) (Math.floor(Math.random() * (maxVal - minVal - 1)) + minVal);
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        entity.setLifeLimit(20);

        float angle = random.nextInt(360);
        float radius = random.nextInt(100);

        double t = 2 * Math.PI * random.nextDouble();
        double u = random.nextDouble() + random.nextDouble();
        double r = 100 * ((u > 1) ? 2 - u : u);
        double x = r * Math.cos(t);
        double y = r * Math.sin(t);

        // float x = (float) (Math.cos(Math.toRadians(angle)) * (1 + radius));
        // float y = (float) (Math.sin(Math.toRadians(angle)) * (1 + radius));

        entity.getStartingPosition().x = (float) x;
        entity.getStartingPosition().y = (float) y;
        entity.getPosition().z = 32;

        // Vector3 centre = new Vector3(width / 2, height / 2, 0);

        // entity.getPosition().set(centre.x + x, centre.y + y, 0);

        // Vector3 delta = new Vector3(entity.getPosition()).sub(centre).nor();

        // The closer to the centre of the screen, the larger and faster the element will go
        // float distanceFromCentre = delta.len2();

        // float factor = 1/distanceFromCentre;

        // float velocityRandom = random.nextFloat();
        // entity.getVelocity().set(delta).scl(factor);

        // float sizeRandom = random.nextFloat();
        entity.setSize(1);

        // if (sizeRandom > 0.9f) {
        // entity.setSizeIncrement(0.001 * factor);
        // }else{
        // entity.setSizeIncrement(0.001 * sizeRandom);
        // }

    }

    @Override public void update(float time) {}

}
