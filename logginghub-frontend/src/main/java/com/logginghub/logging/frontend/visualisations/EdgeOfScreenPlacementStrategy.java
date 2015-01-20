package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.logging.frontend.modules.ViewDetails;

public class EdgeOfScreenPlacementStrategy implements PlacementStrategy {

    private Random random = new Random();

    private ViewDetails viewDetails;

    public EdgeOfScreenPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        int side = random.nextInt(4);

        int x = 0;
        int y = 0;
        
        int width = viewDetails.width;
        int height = viewDetails.height;

        switch (side) {
            case 0:
                // Top
                x = random.nextInt(width);
                y = 0;
                break;

            case 1:
                // Right
                x = width;
                y = random.nextInt(height);
                break;
            case 2:
                // Bottom
                x = random.nextInt(width);
                y = height;
                break;
            case 3:
                // Left
                x = 0;
                y = random.nextInt(height);
                break;
        }

        entity.getPosition().set(x, y, 0);

        Vector3 velocity = new Vector3(viewDetails.width / 2, viewDetails.height / 2, 0);
        velocity.sub(entity.getPosition()).nor();

        entity.setLifeLimit(20);
        entity.getVelocity().set(velocity);

    }

    public void update(float time) {

    }
}
