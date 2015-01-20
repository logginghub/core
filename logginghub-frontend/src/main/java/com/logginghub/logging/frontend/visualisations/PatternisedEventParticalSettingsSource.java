package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.util.Random;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.Destination;

public class PatternisedEventParticalSettingsSource extends AbstractSource<ParticleSettings> implements Destination<PatternisedLogEvent> {

    private Random random = new Random();
    private ColourInterpolation levelColourInterpolation = new ColourInterpolation(Color.blue, Color.green, Color.yellow, Color.red);

    private double highClamp = 150;
    private double highLevel = 100;
    private double lowLevel = 0;

    private int patternID;
    private int labelIndex = 0;

    public void send(PatternisedLogEvent t) {

        if (t.getPatternID() == patternID) {

            String timeString = t.getVariables()[labelIndex];
            if (timeString != null) {

                double time = Double.parseDouble(timeString);

                if (time > highClamp) {
                    time = highClamp;
                }

                double factor = (time - lowLevel) / (highLevel - lowLevel);

                double size = 0.5 + (1.0 * factor);

                ColourInterpolation interp = levelColourInterpolation;
                Color color = interp.interpolate(factor);

                double velocity = 0.7 + (0.5 * (1 - factor));

                float mass = 1;
                float lifetime = 10;

                ParticleSettings settings = new ParticleSettings(color, 1, velocity, size, 1);
                settings.setMass(mass);
                settings.setLifetime(lifetime);
                dispatch(settings);
            }
        }
    }

    public void setPatternID(int patternID) {
        this.patternID = patternID;
    }
    
    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }
    
    public int getPatternID() {
        return patternID;
    }
    
    public int getLabelIndex() {
        return labelIndex;
    }
    
}
