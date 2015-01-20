package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.util.Random;
import java.util.logging.Level;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.Destination;
import com.logginghub.utils.logging.Logger;

public class RawEventParticalSettingsSource extends AbstractSource<ParticleSettings> implements Destination<LogEvent> {

    private Random random = new Random();
    private ColourInterpolation levelColourInterpolation = new ColourInterpolation(Color.pink, // n/a
                                                                                   Color.pink, // n/a
                                                                                   Color.pink, // n/a
                                                                                   Color.green.darker(), // finest
                                                                                   Color.green, // finer
                                                                                   Color.green.brighter(), // fine
                                                                                   Color.pink, // n/a
                                                                                   Color.white, // config
                                                                                   Color.blue, // info
                                                                                   Color.yellow, // warning
                                                                                   Color.red // severe
    );

    public void send(LogEvent t) {

        int level = t.getLevel();

        double size;
        double doubleValue = level;
//        if (level <= Level.INFO.intValue()) {
            // Add some random variation to the colours
            int variation = 30;
            doubleValue += variation - random.nextInt(variation * 2);
//        }

        double factor = doubleValue / Level.SEVERE.intValue();

        ColourInterpolation interp = levelColourInterpolation;
        Color color = interp.interpolate(factor);

        double velocity;

        if (level <= Level.INFO.intValue()) {
            // Add some random variation to the colours
//            int variation = 60;
            doubleValue += variation - random.nextInt(variation * 2);
            size = factor * 0.9;
        }
        else {
            size = factor * 1.1;
        }

        velocity = 0.15;

        float mass = 1;
        float lifetime = 10;

        switch (level) {
            case Logger.finest:
                velocity = 1.5;
                mass = 0.4f;
                break;
            case Logger.finer:
                velocity = 1.4;
                mass = 0.6f;
                break;
            case Logger.fine:
                velocity = 1.3;
                mass = 0.8f;
                break;
            case Logger.info:
                velocity = 1.2;
                mass = 1f;
                break;
            case Logger.warning:
                lifetime = 20;
                velocity = 1.1;
                mass = 50f;
                break;
            case Logger.severe:
                lifetime = 20;
                velocity = 1;
                mass = 100f;
                break;
        }

        ParticleSettings settings = new ParticleSettings(color, 1, velocity, size, 1);
        settings.setMass(mass);
        settings.setLifetime(lifetime);
        dispatch(settings);
    }
}
