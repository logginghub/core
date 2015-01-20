package com.logginghub.logging.frontend.services;

import java.util.List;

import com.logginghub.logging.api.patterns.Pattern;
import com.logginghub.logging.messaging.PatternModel;
import com.logginghub.utils.observable.ObservableList;

public interface PatternManagementService {

    ObservableList<PatternModel> listPatterns();
    
    List<String> getPatternNames();
    ObservableList<Pattern> getPatterns();
    
    Pattern getPatternByID(int patternID);

    String getLabelName(int patternID, int labelIndex);
    String getPatternName(int patternID);
    
}
