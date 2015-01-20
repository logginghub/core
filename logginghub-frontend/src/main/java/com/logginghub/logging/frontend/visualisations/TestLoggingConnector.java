package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;

import com.logginghub.logging.frontend.visualisations.configuration.EmitterConfig;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.MovingAverage;
import com.logginghub.utils.RandomWithMomentum;
import com.logginghub.utils.WorkerThread;

public class TestLoggingConnector {

    private ColourInterpolation colourInterpolation = new ColourInterpolation(Color.blue, Color.blue, Color.green, Color.yellow, Color.red);

    private RandomWithMomentum countRandom = new RandomWithMomentum(0, 1, 5, 10, 20);
    private RandomWithMomentum timeRandom = new RandomWithMomentum(0, 0, 100, 10, 20);

    private int perSecondCount = 0;
    private MovingAverage movingAverage = new MovingAverage(3);
    private double ma;

    private int countIncrementer = 10;
    private double sizeTimeMultipler = 1;
    private double releaseCounter = 0;

    private WorkerThread workerThread;

    public void start(final Generator generator) {

         workerThread = WorkerThread.executeOngoing("Generator", 1, new Runnable() {
            @Override public void run() {
                releaseCounter += countRandom.next();
                if (releaseCounter > 1) {
                    int amount = (int) releaseCounter;
                    for (int i = 0; i < amount; i++) {
                        double time = timeRandom.next() / 100d;
                        Color background = colourInterpolation.interpolate(time);
                        double size = time * sizeTimeMultipler;
                        generator.generate(background, time, 0.12, size);
                    }
                    releaseCounter -= amount;
                }
                countIncrementer++;
            }
        });

    }

    public void reinitialise(EmitterConfig config) {
        countRandom.setMax(config.getTestConnectionRate().getY());
        countRandom.setMin(config.getTestConnectionRate().getX());
        countRandom.reset();
        sizeTimeMultipler = config.getSizeTimeMultipler();
    }

    public void stop() {
        workerThread.stop();
    }
}
