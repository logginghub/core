package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.logging.frontend.visualisations.configuration.EmitterConfig;
import com.logginghub.logging.frontend.visualisations.configuration.VisualisationConfig;

public class EmitterController {

    private VisualiserModel model;
//    private EntitySource entitySource;
    private Vector3 directionVector;
    private Vector3 directionVariance;
    private Vector3 source;

    private double releaseRotationAngle = 0;
    private VisualisationConfig config;
    private EmitterConfig emitterConfig;
    private TestLoggingConnector testConnector;

    public EmitterController(VisualiserModel model, VisualisationConfig config, EmitterConfig emitterConfig) {
        this.model = model;
        this.config = config;
        this.emitterConfig = emitterConfig;
//        setupMainEmitter();
//        bind(model);

        if (emitterConfig.getTestConnectionRate().getY() > 0) {
            testConnector = new TestLoggingConnector();
            testConnector.reinitialise(emitterConfig);
            testConnector.start(new Generator() {
                public void generate(Color color, double factor, double velocity, double size) {
                    EmitterController.this.generate(color, factor, velocity, size, velocity);
                }
            });
        }

    }

    public EmitterConfig getEmitterConfig() {
        return emitterConfig;
    }

    public VisualisationConfig getConfig() {
        return config;
    }

//    private void setupMainEmitter() {
//
//        directionVector = new Vector3f(0, -25, 0);
//
//        VectorConfig sourcePosition = emitterConfig.getSourcePosition();
//        source = new Vector3f(sourcePosition.getX(), sourcePosition.getY(), 0);
//
//        model.setYInverted(true);
//
//        VectorConfig releaseVelocity = emitterConfig.getReleaseVelocity();
//
//        directionVector.x = releaseVelocity.getX();
//        directionVector.y = releaseVelocity.getY();
//
//        directionVariance = new Vector3f(emitterConfig.getDirectionVariance().getX(), emitterConfig.getDirectionVariance().getY(), 0);
//        entitySource = new EntitySource();
//
//        model.addSource(entitySource);
//    }

    private void bind(VisualiserModel model) {

        // model.getVaryX().addListenerAndNotifyCurrent(new ObservablePropertyListener<Boolean>() {
        // @Override public void onPropertyChanged(Boolean oldValue, Boolean newValue) {
        //
        // }
        // });
        //
        // model.getReleaseAngle().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<Double>() {
        // @Override public void onPropertyChanged(Double oldValue, Double newValue) {
        // updateReleaseVectors();
        // }
        // });
        //
        // model.getReleaseVelocity().addListenerAndNotifyCurrent(new
        // ObservablePropertyListener<Double>() {
        // @Override public void onPropertyChanged(Double oldValue, Double newValue) {
        // updateReleaseVectors();
        // }
        // });
        //
        // model.getReleaseX().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>()
        // {
        // @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
        // source.x = newValue;
        // }
        // });
        //
        // model.getReleaseY().addListenerAndNotifyCurrent(new ObservablePropertyListener<Integer>()
        // {
        // @Override public void onPropertyChanged(Integer oldValue, Integer newValue) {
        // source.y = newValue;
        // }
        // });

    }

    protected void updateReleaseVectors() {

        // double radians = Math.toRadians(model.getReleaseAngle().doubleValue());
        // double x = Math.cos(radians);
        // double y = Math.sin(radians);
        //
        // double velocity = model.getReleaseVelocity().doubleValue();
        //
        // directionVector.x = x * velocity;
        // directionVector.y = y * velocity;
        //
        // System.out.println(directionVector);

    }

    public VisualiserModel getModel() {
        return model;
    }

    public void generate(Color color, double factor, double velocity, double size, double spin) {
//        Entity entity = model.spawn(color, velocity, size);
//        entity.setLifeLimit(emitterConfig.getLifeLimit());
////        entity.setGravity(emitterConfig.getGravity());
//        entity.setSpin((float) (spin * -50));
//
//        if (model.getVaryX().asBoolean()) {
//            entity.getPosition().x += (model.getXDeviation().doubleValue() / -2d) + (model.getXDeviation().doubleValue() * factor);
//        }
//
    }

    public void update(float delta) {

        double rotator = model.getReleaseRotationSpeed().doubleValue();
        if (rotator > 0) {
            double x = Math.cos(Math.toRadians(releaseRotationAngle));
            double y = Math.sin(Math.toRadians(releaseRotationAngle));

            directionVector.x = (float) x;
            directionVector.y = (float) y;
            directionVector.scl((float) model.getReleaseVelocity().doubleValue());

            releaseRotationAngle += model.getReleaseRotationSpeed().doubleValue();
        }
    }

    public void reinitialise(EmitterConfig config) {
        this.emitterConfig = config;

        source.x = (float) emitterConfig.getSourcePosition().getX();
        source.y = (float) emitterConfig.getSourcePosition().getY();

        directionVector.x = (float) emitterConfig.getReleaseVelocity().getX();
        directionVector.y = (float) emitterConfig.getReleaseVelocity().getY();

        directionVariance.x = (float) emitterConfig.getDirectionVariance().getX();
        directionVariance.y = (float) emitterConfig.getDirectionVariance().getY();

        testConnector.reinitialise(emitterConfig);
    }

    public void stop() {
        if (testConnector != null) {
            testConnector.stop();
        }
    }

}
