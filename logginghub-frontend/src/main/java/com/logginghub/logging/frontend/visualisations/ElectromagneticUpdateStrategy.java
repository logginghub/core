package com.logginghub.logging.frontend.visualisations;

import com.badlogic.gdx.math.Vector3;
import com.logginghub.utils.Out;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.WorkerThread;

public class ElectromagneticUpdateStrategy implements ModleUpdateStrategy {

    private Vector3 direction = new Vector3();

    private Vector3 forceA = new Vector3();
    private Vector3 forceB = new Vector3();

    private WorkerThread thread;

    public void updateAll(double time, VisualiserModel model) {
        Stopwatch sw = Stopwatch.start("Updating");
        Entity entities = model.getLiveEntitiesHead();

        int comparisons = 0;
        int evaluations = 0;

        Entity primaryPointer = entities.next;
        while (primaryPointer != null) {

            Entity secondaryPointer = primaryPointer.next;
            while (secondaryPointer != null) {
                if (primaryPointer.isAlive() && secondaryPointer.isAlive()) {
                    evaluate(primaryPointer, secondaryPointer);
                    evaluations++;
                }
                comparisons++;
                secondaryPointer = secondaryPointer.next;
            }

            primaryPointer = primaryPointer.next;
        }

        Out.out("{}/{} in {}", evaluations, comparisons, sw.stopAndFormat());

    }

    private void evaluate(Entity a, Entity b) {

        direction.set(b.getPosition());
        direction.sub(a.getPosition());

        double distance = direction.len2();
        if (distance > 0) {

            double constant = 1;
            double masses = a.getMass() * b.getMass() * constant;

            double force = masses / (0.1 * distance);

            double maxForce = 0.1;
            if (force > maxForce) {
                force = maxForce;
            }

            direction.nor();

            // Apply the force to the normalised direction vector
            forceA.set(direction).scl((float) (force / a.getMass()) / 1);
            forceB.set(direction).scl((float) (force / b.getMass()) / 1);

            // Debug.out("a {} b {}", forceA, forceB);

            a.getVelocity().add(forceA);
            b.getVelocity().sub(forceB);

            // direction.nor().scl((float) ((float) force * a.getSize()));
            // direction.nor().scl((float) ((float) force / b.getSize()));
            // b.getVelocity().sub(direction);
        }
    }

    // public void updateAllx(FireworksModel model) {
    // Stopwatch sw = Stopwatch.start("Updating");
    // ArrayDeque<Entity> entities = model.getLiveEntities();
    //
    // Iterator<Entity> mainIterator = entities.iterator();
    //
    // int comparisons = 0;
    // for (int i = 0; i < entities.size(); i++) {
    // for (int j = i + 1; j < entities.size(); j+=10) {
    //
    // Entity a = entities.;
    // Entity b = entities[j];
    //
    // if (a.isAlive() && b.isAlive()) {
    //
    // comparisons++;
    // direction.set(b.getPosition());
    // direction.sub(a.getPosition());
    //
    // double distance = direction.len2();
    // if (distance > 0) {
    //
    // double constant = 1;
    // double masses = a.getMass() * b.getMass() * constant;
    //
    // double force = masses / (0.1 * distance);
    //
    // double maxForce = 1;
    // if (force > maxForce) {
    // force = maxForce;
    // }
    //
    // direction.nor();
    //
    // // Apply the force to the normalised direction vector
    // forceA.set(direction).scl((float) (force / a.getMass()) / 1);
    // forceB.set(direction).scl((float) (force / b.getMass()) / 1);
    //
    // // Debug.out("a {} b {}", forceA, forceB);
    //
    // a.getVelocity().add(forceA);
    // b.getVelocity().sub(forceB);
    //
    // // direction.nor().scl((float) ((float) force * a.getSize()));
    // // direction.nor().scl((float) ((float) force / b.getSize()));
    // // b.getVelocity().sub(direction);
    // }
    // }
    //
    // }
    // }
    //
    // // Debug.out("{} comparisons", comparisons);
    // // sw.stopAndDump();
    //
    // }

    @Override public void updateEntity(double time, Entity entity) {

    }

    public void startAsync(final VisualiserModel model) {
        thread = WorkerThread.executeOngoing("LoggingHub-EMUpdateStrategyWorker", 50, new Runnable() {
            @Override public void run() {
                updateAll(50/1000, model);
            }
        });
    }

    public void stop() {
        if(thread != null) {
            thread.stop();
        }
    }

}
