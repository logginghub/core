package com.logginghub.logging.frontend.views.historicalstack.historicalevents;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 03/02/15.
 */
public class HistoricalStackUnderlyingModel {

    private List<HistoricalStackTableRow> rows = new ArrayList<HistoricalStackTableRow>();
    private int timePeriods = 100;

    public List<HistoricalStackTableRow> getRows() {
        return rows;
    }

    public int getTimePeriods() {
        return timePeriods;
    }
}
