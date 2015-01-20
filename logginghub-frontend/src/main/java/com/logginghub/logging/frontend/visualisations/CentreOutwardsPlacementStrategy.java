package com.logginghub.logging.frontend.visualisations;

import java.util.Random;

import com.badlogic.gdx.math.Vector3;

public class CentreOutwardsPlacementStrategy implements PlacementStrategy {

    private int width;
    private int height;
    private Random random = new Random();

    public CentreOutwardsPlacementStrategy(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override public void place(Entity entity, ParticleSettings particleSettings) {
        
        entity.getPosition().set(random.nextInt(width), random.nextInt(height), 0);        
        
        Vector3 centre = new Vector3(width/2, height/2, 0);
        
        Vector3 delta = new Vector3(entity.getPosition()).sub(centre).nor();
        entity.getVelocity().set(delta);
        
    }

    @Override public void update(float time) {}

}
