package com.logginghub.logging.frontend.patterniseddataview;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.swingutils.table.ExtensibleTable;
import com.logginghub.swingutils.table.ExtensibleTableModel;

public class PatternisedDataViewTable extends ExtensibleTable<PatternisedLogEvent>{

    public PatternisedDataViewTable(ExtensibleTableModel<PatternisedLogEvent> model) {
        super(model);
    }

}
