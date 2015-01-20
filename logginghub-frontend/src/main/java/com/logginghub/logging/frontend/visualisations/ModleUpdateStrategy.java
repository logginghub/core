package com.logginghub.logging.frontend.visualisations;

public interface ModleUpdateStrategy {

    void updateAll(double time, VisualiserModel model);

    void updateEntity(double time, Entity entity);

}
