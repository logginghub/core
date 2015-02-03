package com.logginghub.logging.frontend.charting.newmodel;

import com.logginghub.logging.frontend.modules.PatterniserModule;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.StringUtils;

import java.util.Map;

/**
 * Created by james on 02/02/15.
 */
public class PerformanceBinder {
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
                if (patternisedLogEvent.getPatternID() == 1) {
                    String instanceKey = StringUtils.format("{}.{}", patternisedLogEvent.getSourceHost(), patternisedLogEvent.getSourceApplication());
                    XYSeriesModel XYSeriesModel = models.get(instanceKey);

                    // Need to add two values to form the shape of the step
                    double time = Double.parseDouble(patternisedLogEvent.getVariable(0));
                    long endTime = patternisedLogEvent.getTime();

                    // Plot a single point based on the time
                    XYSeriesModel.getValues().add(new XYValue(endTime, time));
                }
            }
        });
    }
}
