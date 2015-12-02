package com.logginghub.utils.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by james on 07/10/15.
 */
public class PatternMetadataContainer {

    private Map<Integer, PatternMetadata> metadata = new HashMap<Integer, PatternMetadata>();


    public PatternMetadata getMetadataForPattern(int patternId) {
        return metadata.get(patternId);
    }

    public void addPatternMetadata(int patternId, PatternMetadata patternMetadata) {
        metadata.put(patternId, patternMetadata);
    }
}
