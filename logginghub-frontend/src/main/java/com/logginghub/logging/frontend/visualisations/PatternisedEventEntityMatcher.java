package com.logginghub.logging.frontend.visualisations;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.ColourInterpolation;
import com.logginghub.utils.ColourUtils;
import com.logginghub.utils.Destination;

public class PatternisedEventEntityMatcher extends AbstractSource<ParticleSettings> implements Destination<PatternisedLogEvent> {

    private int patternID = -1;
    private String host = "";
    private String application = "";
    private int labelIndex;
    private double maximumValue;
    private double maximumSize;
    private double minimumSize;
    private double minimumVelocity;
    private double velocityFactor;
    private double sizeFactor;
    private String colourGradient;

    private Map<String, ColourInterpolation> interpolators = new HashMap<String, ColourInterpolation>();

    public void send(PatternisedLogEvent event) {

        if (patternID == event.getPatternID()) {

            if (host != null && (host.length() == 0 || event.getSourceHost().contains(host))) {

                if (application != null && (application.length() == 0 || event.getSourceApplication().contains(application))) {

                    String value = event.getVariables()[labelIndex];

                    double doubleValue = Double.parseDouble(value);
                    if (doubleValue > maximumValue) {
                        doubleValue = maximumValue;
                    }
                    double factor = doubleValue / maximumValue;

                    ColourInterpolation interp = getGradient(colourGradient);
                    Color color = interp.interpolate(factor);

                    double velocity;
                    double size;

                    if (velocityFactor != -1) {
                        velocity = (1 - factor) * velocityFactor;
                    }
                    else {
                        // TODO : why is this an arbitrary number!
                        velocity = 0.15;
                    }

                    if (velocity < minimumVelocity) {
                        velocity = minimumVelocity;
                    }

                    if (sizeFactor != -1) {
                        size = factor * sizeFactor;
                    }
                    else {
                        size = 1;
                    }

                    if (!Double.isNaN(maximumSize) && size > maximumSize) {
                        size = maximumSize;
                    }

                    if (!Double.isNaN(minimumSize) && size < minimumSize) {
                        size = minimumSize;
                    }

                    ParticleSettings settings = new ParticleSettings(color, factor, velocity, size, velocity);
                    dispatch(settings);
                }
            }
        }
    }

    private ColourInterpolation getGradient(String colourGradient) {

        ColourInterpolation colourInterpolation = interpolators.get(colourGradient);
        if (colourInterpolation == null) {

            String[] split = colourGradient.split(",");
            Color[] colours = new Color[split.length];
            for (int i = 0; i < split.length; i++) {
                colours[i] = ColourUtils.parseColor(split[i]);
            }

            colourInterpolation = new ColourInterpolation(colours);
            interpolators.put(colourGradient, colourInterpolation);
        }

        return colourInterpolation;
    }

}
