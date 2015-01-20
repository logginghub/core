package com.logginghub.logging.frontend.visualisations;

public interface PlacementStrategy {

    void place(Entity entity, ParticleSettings particleSettings);
    void update(float time);
    
}
