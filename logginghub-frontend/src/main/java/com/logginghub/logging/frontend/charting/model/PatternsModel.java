package com.logginghub.logging.frontend.charting.model;

import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableList;

/**
 * Contains a list of PatternModels/
 * 
 * @author James
 * 
 */
public class PatternsModel extends Observable  {

    private ObservableList<PatternModel> patterns = createListProperty("patterns", PatternModel.class);

    /**
     * The list of patterns that use the format contained in this ParserModel.
     * 
     * @return
     */
    public ObservableList<PatternModel> getPatterns() {
        return patterns;
    }

}
