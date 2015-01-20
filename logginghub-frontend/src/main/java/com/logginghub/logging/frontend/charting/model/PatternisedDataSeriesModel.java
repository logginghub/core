package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

/**
 * Contains a list of PatternisedDataModels.
 * 
 * @author James
 * 
 */
public class PatternisedDataSeriesModel extends Observable {

    private ObservableList<PatternisedDataModel> patternised = new ObservableList<PatternisedDataModel>(PatternisedDataModel.class, new ArrayList<PatternisedDataModel>());

    public ObservableList<PatternisedDataModel> getPatternised() {
        return patternised;
    }
    
}
