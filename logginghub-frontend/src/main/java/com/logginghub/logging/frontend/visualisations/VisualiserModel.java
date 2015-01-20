package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.logging.frontend.modules.ViewDetails;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.Out;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

public class VisualiserModel extends Observable {

    private static Random random = new Random();

    private ColourInterpolation colourInterpolation = new ColourInterpolation(Color.blue,
                                                                              Color.green,
                                                                              Color.yellow,
                                                                              Color.red,
                                                                              Color.white,
                                                                              Color.blue);
    private double colourIndex = 0;
    private Vector3 source = new Vector3(500, 100, 0);
    private Vector3 sourceVariance = new Vector3(100f, 0, 0f);
    private Vector3 direction = new Vector3(0, 4, 0);
    private Vector3 directionVariance = new Vector3(0.5f, .1f, 0f);

    private volatile int nextEntity = 0;
    private ObservableProperty<Boolean> varyX = createBooleanProperty("varyX", false);
    private ObservableProperty<Boolean> additiveBlending = createBooleanProperty("additiveBlending", true);
    private ObservableDouble releaseAngle = createDoubleProperty("releaseAngle", 60);
    private ObservableDouble releaseVelocity = createDoubleProperty("releaseVelocity", 30);
    private ObservableDouble xDeviation = createDoubleProperty("xDeviation", 500);

    private ObservableDouble releaseRotationSpeed = createDoubleProperty("releaseRotationSpeed", 0);
    private ObservableInteger releaseX = createIntProperty("releaseX", 50);
    private ObservableInteger releaseY = createIntProperty("releaseX", 50);

    private Logger logger = Logger.getLoggerFor(getClass());
    // private List<Entity> entities = new ArrayList<Entity>();
    // private List<EntitySource> entitieSources = new CopyOnWriteArrayList<EntitySource>();
    // private double gravity;
    private boolean yInverted;

    // private Entity[] entities;

    // private ArrayDeque<Entity> liveEntities;
    // private ArrayDeque<Entity> deadEntities;

    private List<ModleUpdateStrategy> updateStrategies = new ArrayList<ModleUpdateStrategy>();

    private PlacementStrategy placementStrategy = new EdgeOfScreenPlacementStrategy(new ViewDetails(100, 100));

    // private EntitySource entitySource;

    private Entity liveEntitiesHead;
    private Entity deadEntitiesHead;

    private Entity liveEntitiesTail;
    private Entity deadEntitiesTail;

    public final static int liveEntitiesHeadID = -1;
    public final static int liveEntitiesTailID = -10;
    public final static int deadEntitiesHeadID = -2;
    public final static int deadEntitiesTailID = -20;

    private boolean additive;

    private String shape = "star";

    private int entityLifetime;

    private ViewDetails viewDetails;

    public VisualiserModel(int poolsize, ViewDetails viewDetails) {
        this.viewDetails = viewDetails;
        // EntitySource entitySource = new EntitySource(100, 0.001, new
        // Vector3f(200, 200, 0), new Vector3f(0, -10, 0), new Vector3f(2, 4,
        // 0));
        // entitieSources.add(entitySource);

        // liveEntities = new ArrayDeque<Entity>();
        // deadEntities = new ArrayDeque<Entity>();

        // Create the linked list end pointers
        liveEntitiesHead = new Entity(liveEntitiesHeadID);
        deadEntitiesHead = new Entity(deadEntitiesHeadID);

        liveEntitiesTail = new Entity(liveEntitiesTailID);
        deadEntitiesTail = new Entity(deadEntitiesTailID);

        // Link them up
        liveEntitiesHead.previous = null;
        liveEntitiesHead.next = liveEntitiesTail;

        liveEntitiesTail.previous = liveEntitiesHead;
        liveEntitiesTail.next = null;

        deadEntitiesHead.previous = null;
        deadEntitiesHead.next = deadEntitiesTail;

        deadEntitiesTail.previous = deadEntitiesHead;
        deadEntitiesTail.next = null;

        // Initialise the pool into the dead pool
        Entity pointer = deadEntitiesHead;

        for (int i = 0; i < poolsize; i++) {
            Entity spawn = create(i);

            insertBefore(spawn, deadEntitiesTail);

            // Set the natural order pointers
            pointer.naturalNext = spawn;
            spawn.naturalNext = deadEntitiesTail;

            // Create a linked list of the dead entities
            spawn.previous = pointer;
            pointer.next = spawn;
            pointer = spawn;

        }
    }

    private void insertBefore(Entity toInsert, Entity before) {

        // a -> before -> b
        // a -> toInsert -> before -> b

        // Give the new item the right links
        toInsert.previous = before.previous;
        toInsert.next = before;

        // Replace 'before' in the chain with the new entry
        before.previous.next = toInsert;

        // Link before to the new entry
        before.previous = toInsert;
    }

    private Entity create(int id) {
        Entity entity = new Entity(id);
        entity.setAlive(false);
        return entity;
    }

    // public List<EntitySource> getEntitieSources() {
    // return entitieSources;
    // }

    public List<ModleUpdateStrategy> getUpdateStrategies() {
        return updateStrategies;
    }

    public void update(double time) {

        for (ModleUpdateStrategy modleUpdateStrategy : updateStrategies) {
            modleUpdateStrategy.updateAll(time, this);
        }

        if (placementStrategy != null) {
            placementStrategy.update((float) time);
        }

        Entity pointer = liveEntitiesHead.next;
        while (pointer != liveEntitiesTail) {

            Entity entity = pointer;

            // Debug.out("Updating live entity '{}'", entity);

            for (ModleUpdateStrategy modleUpdateStrategy : updateStrategies) {
                modleUpdateStrategy.updateEntity(time, entity);
            }

            if (entity.isAlive()) {
                entity.update(time);

                // if ((!yInverted && entity.getPosition().y >= worldHeight - 50) || (yInverted
                // && entity.getPosition().y < -50)) {
                // entity.setAlive(false);
                // }

                boolean kill = false;

                if (entity.getLifetime() > entity.getLifeLimit()) {
                    kill = true;
                }

                int epsilon = -20;
                if (entity.getPosition().x < epsilon ||
                    entity.getPosition().y < epsilon ||
                    entity.getPosition().x > viewDetails.width - epsilon ||
                    entity.getPosition().y > viewDetails.height -epsilon) {
                    kill = true;
                }

                if (kill) {
                    // Need to jigger the pointer here so the iteration carries on smoothly
                    pointer = entity.previous;

                    // Now detach the entity
                    killEntity(entity);
                }

            }

            // Move to the next live entity
            pointer = pointer.next;

        }
        // entities.addAll(newEntities);
    }

    public void killEntity(Entity entity) {
        entity.getPosition().set(-100, -100, -100);
        // Debug.out("Killing entity '{}'", entity);
        detach(entity);
        insertBefore(entity, deadEntitiesTail);
        entity.setAlive(false);
    }

    // public List<Entity> getEntities() {
    // return entities;
    // }

    // public List<EntitySource> getEntitySources() {
    // return entitieSources;
    // }
    //
    // public void addSource(EntitySource entitySource) {
    // entitieSources.add(entitySource);
    // }

    public void setYInverted(boolean yInverted) {
        this.yInverted = yInverted;
    }

    public ObservableProperty<Boolean> getVaryX() {
        return varyX;
    }

    public ObservableDouble getReleaseAngle() {
        return releaseAngle;
    }

    public ObservableDouble getReleaseVelocity() {
        return releaseVelocity;
    }

    public ObservableProperty<Boolean> getAdditiveBlending() {
        return additiveBlending;
    }

    public ObservableDouble getXDeviation() {
        return xDeviation;
    }

    public ObservableInteger getReleaseX() {
        return releaseX;
    }

    public ObservableInteger getReleaseY() {
        return releaseY;
    }

    public ObservableDouble getReleaseRotationSpeed() {
        return releaseRotationSpeed;
    }

    public Entity spawn(ParticleSettings t) {

        Entity entity = deadEntitiesHead.next;
        if (entity == deadEntitiesTail) {
            // Need to resuse one of the live entries
            entity = liveEntitiesHead.next;
            detach(entity);
            insertBefore(entity, liveEntitiesTail);
        }
        else {
            // We still have dead entries to resuse
            detach(entity);
            insertBefore(entity, liveEntitiesTail);
        }

        entity.setAlive(true);
        entity.setLifetime(0);
        entity.setLifeLimit(t.getLifetime());
        entity.getPosition().set(source);
        // entity.getVelocity().set(direction);
        entity.setSize(t.getSize());
        entity.setMass(t.getMass());

        // double velocity = 0.1 * random.nextDouble();

        int angle = random.nextInt(360);
        double factor = random.nextDouble();

        double vx = factor * directionVariance.x * Math.cos(Math.toRadians(angle));
        double vy = factor * directionVariance.y * Math.sin(Math.toRadians(angle));

        double sx = factor * sourceVariance.x * Math.cos(Math.toRadians(angle));
        double sy = factor * sourceVariance.y * Math.sin(Math.toRadians(angle));

        entity.getPosition().x += sx;
        entity.getPosition().y += sy;

        // entity.getVelocity().x += vx;
        // entity.getVelocity().y += vy;

        // entity.getVelocity().randomise(directionVariance);
        // entity.getVelocity().multiply(velocity);

        if (t.getColor() == null) {
            entity.setColor(colourInterpolation.interpolate(colourIndex));
            colourIndex += 0.0001;
            if (colourIndex > 1) {
                colourIndex = 0;
            }
        }
        else {
            entity.setColor(t.getColor());
        }

        if (placementStrategy != null) {
            placementStrategy.place(entity, t);
        }

        return entity;
    }

    private void detach(Entity entity) {
        try {
            entity.previous.next = entity.next;
            entity.next.previous = entity.previous;
        }
        catch (NullPointerException e) {
            Out.out("NPE detaching entity '{}' with prev '{}' and next '{}'", entity, entity.previous, entity.next);
        }
    }

    // public Entity spawn(Color color, double velocity, double size) {
    // Entity entity = entities[nextEntity++];
    // if (nextEntity == entities.length) {
    // nextEntity = 0;
    // }
    // entity.setAlive(true);
    // entity.setLifetime(0);
    // entity.getPosition().set(source);
    // entity.getVelocity().set(direction);
    // entity.setSize(size);
    //
    // // double velocity = 0.1 * random.nextDouble();
    //
    // int angle = random.nextInt(360);
    // double factor = random.nextDouble();
    //
    // double vx = factor * directionVariance.x * Math.cos(Math.toRadians(angle));
    // double vy = factor * directionVariance.y * Math.sin(Math.toRadians(angle));
    //
    // double sx = factor * sourceVariance.x * Math.cos(Math.toRadians(angle));
    // double sy = factor * sourceVariance.y * Math.sin(Math.toRadians(angle));
    //
    // entity.getPosition().x += sx;
    // entity.getPosition().y += sy;
    //
    // entity.getVelocity().x += vx;
    // entity.getVelocity().y += vy;
    //
    // // entity.getVelocity().randomise(directionVariance);
    // // entity.getVelocity().multiply(velocity);
    //
    // if (color == null) {
    // entity.setColor(colourInterpolation.interpolate(colourIndex));
    // colourIndex += 0.0001;
    // if (colourIndex > 1) {
    // colourIndex = 0;
    // }
    // }
    // else {
    // entity.setColor(color);
    // }
    //
    // placementStrategy.place(entity);
    //
    // return entity;
    // }

    // public Entity[] getEntities() {
    // return entities;
    // }

    public Entity getLiveEntitiesHead() {
        return liveEntitiesHead;
    }

    public Entity getDeadEntitiesHead() {
        return deadEntitiesHead;
    }

    public Entity getLiveEntitiesTail() {
        return liveEntitiesTail;
    }

    public Entity getDeadEntitiesTail() {
        return deadEntitiesTail;
    }

    public void setPlacementStrategy(PlacementStrategy placementStrategy) {
        this.placementStrategy = placementStrategy;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public boolean isAdditive() {
        return additive;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getShape() {
        return shape;
    }

    public void reset() {
        // TODO : doesn't work
        //
        // Entity pointer = liveEntitiesHead.next;
        // while (pointer != liveEntitiesTail) {
        //
        // Entity entity = pointer;
        //
        // // if ((!yInverted && entity.getPosition().y >= worldHeight - 50) || (yInverted
        // // && entity.getPosition().y < -50)) {
        // // entity.setAlive(false);
        // // }
        //
        // boolean kill = true;
        //
        // if (kill) {
        // // Need to jigger the pointer here so the iteration carries on smoothly
        // pointer = entity.previous;
        //
        // // Now detach the entity
        // killEntity(entity);
        // }
        //
        // }
        //
        // // Move to the next live entity
        // pointer = pointer.next;

        // entities.addAll(newEntities);
    }
}
