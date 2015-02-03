package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.logging.frontend.modules.PatterniserModule;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

/**
 * Created by james on 31/01/15.
 */
public class GCOutputBinder {

    private boolean stepMode = false;

    public void setStepMode(boolean stepMode) {
        this.stepMode = stepMode;
    }

    public boolean isStepMode() {
        return stepMode;
    }

    public void bind(final XYChartDataModel model, PatterniserModule patterniserModule) {

        final Map<String, XYSeriesModel> models = new FactoryMap<String, XYSeriesModel>() {
            @Override protected XYSeriesModel createEmptyValue(String key) {
                XYSeriesModel XYSeriesModel = new XYSeriesModel();
                XYSeriesModel.getLabel().set(key);
                model.getSeries().add(XYSeriesModel);
                return XYSeriesModel;
            }
        };

        patterniserModule.addPatternisedEventListener(new Destination<PatternisedLogEvent>() {
            @Override public void send(PatternisedLogEvent patternisedLogEvent) {
                if (patternisedLogEvent.getPatternID() == 0) {
                    String instanceKey = StringUtils.format("{}.{}", patternisedLogEvent.getSourceHost(), patternisedLogEvent.getSourceApplication());
                    XYSeriesModel XYSeriesModel = models.get(instanceKey);

                    // Need to add two values to form the shape of the step
                    long delay = Long.valueOf(patternisedLogEvent.getVariable(0));
                    long endTime = patternisedLogEvent.getTime();
                    long startTime = endTime - delay;

                    try {
                        Number parsed = NumberFormat.getInstance().parse(patternisedLogEvent.getVariable(1));
                        double height = parsed.doubleValue();

                        if (stepMode) {
                            // Build a step out of the delay and the size of the collection
                            XYSeriesModel.getValues().add(new XYValue(startTime, height));
                            XYSeriesModel.getValues().add(new XYValue(endTime, 0));
                        } else {
                            // Plot a single point based on the delay
                            XYSeriesModel.getValues().add(new XYValue(endTime, delay));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }

}
