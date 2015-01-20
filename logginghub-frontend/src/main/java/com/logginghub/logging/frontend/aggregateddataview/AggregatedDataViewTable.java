package com.logginghub.logging.frontend.aggregateddataview;

import com.logginghub.logging.messaging.AggregatedLogEvent;
import com.logginghub.swingutils.table.ExtensibleTable;
import com.logginghub.swingutils.table.ExtensibleTableModel;

public class AggregatedDataViewTable extends ExtensibleTable<AggregatedLogEvent>{

    public AggregatedDataViewTable(ExtensibleTableModel<AggregatedLogEvent> model) {
        super(model);
    }

}
