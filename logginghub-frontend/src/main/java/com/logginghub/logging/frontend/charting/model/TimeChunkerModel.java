package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableLong;

/**
 * Encapsulates a time based aggregation of ParserModels. You'll need one TimeChunkerModel for each
 * of the time periods you'd like to aggregate over - for example a 1,000ms chunker for per-second
 * charts, and a 60,000ms chunker for per minute charts.
 */
public class TimeChunkerModel extends Observable {

    private ObservableLong interval = new ObservableLong(1000, this);
    private ObservableList<ParserModel> parserModels = new ObservableList<ParserModel>(ParserModel.class, new ArrayList<ParserModel>());
    
    /**
     * The time interval in milliseconds for this time chunker model.
     * @return
     */
    public ObservableLong getInterval() {
        return interval;
    }
    
    /**
     * The list of parser models using this time period.
     * @return
     */
    public ObservableList<ParserModel> getParserModels() {
        return parserModels;
    }
    
}
