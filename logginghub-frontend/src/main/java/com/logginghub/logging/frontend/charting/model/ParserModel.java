package com.logginghub.logging.frontend.charting.model;

import java.util.ArrayList;

import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Contains a list of PatternModels, along with a format string that controls which elements of the
 * event are passed upstream with each result.
 * 
 * @author James
 * 
 */
public class ParserModel extends Observable {

    private ObservableProperty<String> format = new ObservableProperty<String>("", this);
    private ObservableList<PatternModel> patterns = new ObservableList<PatternModel>(PatternModel.class, new ArrayList<PatternModel>());

    /**
     * An example would be : "{host}/{source}/{label}" : those parts of the event will be extracted
     * and sent upstream. The most likely use of this is to construct meaningful series names if you
     * want to split out different host/source combinations on a chart (otherwise it'll just
     * aggregate them together.)
     * 
     * @return
     */
    public ObservableProperty<String> getFormat() {
        return format;
    }

    /**
     * The list of patterns that use the format contained in this ParserModel.   
     * @return
     */
    public ObservableList<PatternModel> getPatterns() {
        return patterns;
    }

}
