package com.logginghub.logging.frontend.visualisations;

import com.logginghub.logging.frontend.modules.ViewDetails;

public class VisualisationProfile {

    private VisualiserModel model;
    private ElectromagneticUpdateStrategy electromagneticUpdateStrategy;
    private ViewDetails viewDetails;

    public VisualisationProfile(VisualiserModel model, ViewDetails viewDetails) {
        this.model = model;
        this.viewDetails = viewDetails;
    }

    public void reset() {
        model.setPlacementStrategy(null);
        model.getUpdateStrategies().clear();
        if (electromagneticUpdateStrategy != null) {
            electromagneticUpdateStrategy.stop();
            electromagneticUpdateStrategy = null;
        }
    }

    public void setupEmEdge() {
        model.setShape("circle");
        electromagneticUpdateStrategy = new ElectromagneticUpdateStrategy();
        electromagneticUpdateStrategy.startAsync(model);
        model.setPlacementStrategy(new EdgeOfScreenPlacementStrategy(viewDetails));
    }

    public void setupEdge() {
        model.setPlacementStrategy(new EdgeOfScreenPlacementStrategy(viewDetails));
    }

    public void setupIncrementalEdge() {
        model.setPlacementStrategy(new IncrementalEdgeOfScreenPlacementStrategy(viewDetails));
    }

    public void setupRotateCentre() {
        model.setAdditive(true);
        model.setPlacementStrategy(new RotateCentreScreenPlacementStrategy(viewDetails));
    }

    public void setupRotateOffsetCentre() {
        model.setShape("star");
        model.setAdditive(false);
        model.setPlacementStrategy(new RotateOffsetCentreScreenPlacementStrategy(viewDetails));
    }

    public void setupStarField() {
        model.setShape("circle");
        // ElectromagneticUpdateStrategy electromagneticUpdateStrategy = new
        // ElectromagneticUpdateStrategy();
        // electromagneticUpdateStrategy.startAsync(model);
        StarfieldUpdateStrategy scaleUpdateStrategy = new StarfieldUpdateStrategy(viewDetails);
        model.getUpdateStrategies().add(scaleUpdateStrategy);
        model.setPlacementStrategy(new StarFieldPlacementStrategy(viewDetails));
    }

    public void setupFountain() {
        model.setPlacementStrategy(new FountainPlacementStrategy(viewDetails));
        model.getUpdateStrategies().add(new GravityUpdateStrategy());
    }

}
