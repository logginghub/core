package com.logginghub.utils.swing;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.logginghub.utils.logging.Logger;

public class DoubleSpinner extends JSpinner {

    private static final long serialVersionUID = 1L;
    private static final double STEP_RATIO = 0.1;

    private SpinnerNumberModel model;

    private static final Logger logger = Logger.getLoggerFor(DoubleSpinner.class);

    public DoubleSpinner() {
        super();
        // Model setup
        model = new SpinnerNumberModel(0.0, -1000, 1000, 0.1);
        this.setModel(model);

        // Step recalculation
        this.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Double value = getDouble();

                String[] s = value.toString().split("\\.");
                int decimalPlaces = s[s.length - 1].length();
                
                // Steps are sensitive to the current magnitude of the value
                
                double stepSize;
                if(decimalPlaces == 0) {
                    stepSize =1;
                }else {
                    stepSize = 1d / (Math.pow(10, decimalPlaces));
                }
                
                logger.info("DP {} step {}", decimalPlaces, stepSize);
                model.setStepSize(stepSize);
            }
        });
    }

    /**
     * Returns the current value as a Double
     */
    public Double getDouble() {
        return (Double) getValue();
    }

}