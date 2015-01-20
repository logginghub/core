package com.logginghub.logging.frontend.visualisations;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.logging.frontend.modules.ViewDetails;

public class IncrementalEdgeOfScreenPlacementStrategy implements PlacementStrategy {

    private int value;
    private int side = 0;

    private ViewDetails viewDetails;

    public IncrementalEdgeOfScreenPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        int x = 0;
        int y = 0;

        int increment = 5;

        int height = viewDetails.height;
        int width = viewDetails.width;

        switch (side) {
            case 0:
                // Top
                x = value += increment;
                y = height;

                if (value > width) {
                    side++;
                    value = height;
                }
                break;

            case 1:
                // Right
                x = width;
                y = value -= increment;
                if (value < 0) {
                    side++;
                    value = width;
                }
                break;
            case 2:
                // Bottom
                x = value -= increment;
                y = 0;
                if (value < 0) {
                    side++;
                    value = 0;
                }
                break;
            case 3:
                // Left
                x = 0;
                y = value += increment;
                if (value > height) {
                    side = 0;
                    value = 0;
                }
                break;
        }

        entity.getPosition().set(x, y, 0);

        Vector3 velocity = new Vector3(width / 2, height / 2, 0);
        velocity.sub(entity.getPosition()).nor().scl(10);

        entity.getVelocity().set(velocity);

    }

    public void update(float time) {

    }
}
