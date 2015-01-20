package com.logginghub.logging.frontend.visualisations;

import com.logginghub.logging.frontend.modules.ViewDetails;

public class StarfieldUpdateStrategy implements ModleUpdateStrategy {

    private ViewDetails viewDetails;

    public StarfieldUpdateStrategy(ViewDetails details) {
        this.viewDetails = details;
    }

    @Override public void updateAll(double time, VisualiserModel model) {}

    @Override public void updateEntity(double time, Entity entity) {
        
        entity.getPosition().z = entity.getPosition().z - 0.1f;

        if (entity.getPosition().z < 0) {
            entity.setLifetime(entity.getLifeLimit());
        }

        float k = (float) (128.0 / entity.getPosition().z);
        entity.getPosition().x = entity.getStartingPosition().x * k + (viewDetails.width / 2);
        entity.getPosition().y = entity.getStartingPosition().y * k + (viewDetails.height / 2);

        float size = (32 - entity.getPosition().z) / 32;
        entity.setSize(size);
        
    }
}
