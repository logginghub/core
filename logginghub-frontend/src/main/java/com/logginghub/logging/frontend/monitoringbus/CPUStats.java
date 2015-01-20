package com.logginghub.logging.frontend.monitoringbus;

import com.logginghub.utils.HTMLBuilder;
import com.logginghub.utils.StreamListener;
import com.logginghub.utils.data.DataStructure;
import com.logginghub.utils.data.DataStructure.Values;

public class CPUStats implements StreamListener<DataStructure>, WebRenderable {

    private MinuteAggregator aggregator = new MinuteAggregator();

    @Override
    public void onNewItem(DataStructure t) {
        Integer doubleValue = t.getIntValue(Values.VMSTAT_CPU_Idle);
        if(doubleValue != null) {
            int idle = 100 - doubleValue;
            aggregator.add(idle);
        }
    }

    @Override
    public void render(HTMLBuilder builder) {
        builder.append("<h1>" + "CPU Stats (via vmstat idle)" + "</h1>");
        aggregator.render(builder);
    }

}
