package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.logging.frontend.modules.ViewDetails;

public class RotateOffsetCentreScreenPlacementStrategy implements PlacementStrategy {

    private float angle = 0;
    private Random random = new Random();
    private float radius = 20;

    private boolean updateOnTime = true;
    private boolean updateOnPlace = true;
    private ViewDetails viewDetails;

    public RotateOffsetCentreScreenPlacementStrategy(ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
    }

    public void setUpdateOnPlace(boolean updateOnPlace) {
        this.updateOnPlace = updateOnPlace;
    }

    public void setUpdateOnTime(boolean updateOnTime) {
        this.updateOnTime = updateOnTime;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {

        double randomisedAngle = 1 - random.nextInt(2) + angle;
        
        float x = (float) Math.sin(Math.toRadians(randomisedAngle));
        float y = (float) Math.cos(Math.toRadians(randomisedAngle));

        int centreX = viewDetails.width / 2;
        int centreY = viewDetails.height / 2;

        float releaseX = centreX + (radius * x);
        float releaseY = centreY + (radius * y);

        Vector3 pointOnEdge = new Vector3(releaseX, releaseY, 0);

        float nextFloat = 5 + random.nextFloat();
//        if(nextFloat  < 0.5f){
//            nextFloat = 0.5f;
//        }
        
        entity.getPosition().set(pointOnEdge);
        entity.getVelocity().set(x, y, 0).scl((float) particleSettings.getVelocity()).scl(nextFloat);

        if (updateOnPlace) {
            angle += 0.01;
        }
    }

    public void update(float time) {
        if (updateOnTime) {
            angle += 3 + (3 * random.nextFloat());
        }
    }
}
