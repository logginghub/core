package com.logginghub.analytics;

import java.util.HashMap;
import java.util.Set;

import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.model.TimeSeriesDataPoint;
import com.logginghub.utils.FactoryMap;

public class Grouper {

    private String groupDivider = "/";

    private Log log = Log.create(this);

    public HashMap<String, TimeSeriesData> group(final TimeSeriesData fromFile, int... keyIndex) {

        final String legend = buildLegend(fromFile, keyIndex);
        
        @SuppressWarnings("serial") FactoryMap<String, TimeSeriesData> groups = new FactoryMap<String, TimeSeriesData>() {
            @Override protected TimeSeriesData createEmptyValue(String seriesName) {
                TimeSeriesData data = new TimeSeriesData();
                data.setLegend(legend);
                data.setKeysLegend(fromFile.getKeysLegend());
                data.setValuesLegend(fromFile.getValuesLegend());
                return data;
            }
        };

        for (TimeSeriesDataPoint dataPoint : fromFile) {
            String seriesName = buildKey(dataPoint, keyIndex);
            groups.get(seriesName).add(dataPoint.copy());
        }

        log.info("Created %d groups", groups.keySet().size());
        Set<String> seriesName = groups.keySet();
        for (String string : seriesName) {
            TimeSeriesData timeSeriesData = groups.get(string);
            log.info("Series [%s] legend [%s] : %d items", string, timeSeriesData.getLegend(), timeSeriesData.size());
        } 
        

        return groups;
    }

    private String buildLegend(TimeSeriesData dataPoint, int[] keyIndex) {

        String[] keys = dataPoint.getKeysLegend();
        StringBuilder key = new StringBuilder();
        String div = "";
        for (int i : keyIndex) {
            key.append(div);
            key.append(keys[i]);
            div = groupDivider;
        }

        return key.toString();
    }
    
    private String buildKey(TimeSeriesDataPoint dataPoint, int[] keyIndex) {

        String[] keys = dataPoint.getKeys();
        StringBuilder key = new StringBuilder();
        String div = "";
        for (int i : keyIndex) {
            key.append(div);
            key.append(keys[i]);
            div = groupDivider;
        }

        return key.toString();
    }
}
