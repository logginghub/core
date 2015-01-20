package com.logginghub.logging.frontend.charting.model;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableProperty;

public class StreamConfiguration extends Observable {

    private ObservableProperty<String> streamID = createStringProperty("streamID", null);
    private ObservableInteger patternID = createIntProperty("patternID", -1);
    private ObservableInteger labelIndex = createIntProperty("labelIndex", -1);
    private ObservableList<String> eventElements = createListProperty("eventElements", String.class);

    public StreamConfiguration() {

    }

    public StreamConfiguration(String streamID, int patternID, int labelIndex, String[] elements) {
        this.streamID.set(streamID);
        this.patternID.set(patternID);
        this.labelIndex.set(labelIndex);
        for (String string : elements) {
            eventElements.add(string);
        }
    }

    public ObservableProperty<String> getStreamID() {
        return streamID;
    }

    public ObservableInteger getPatternID() {
        return patternID;
    }
    
    public ObservableInteger getLabelIndex() {
        return labelIndex;
    }
    
    public ObservableList<String> getEventElements() {
        return eventElements;
    }

    @Override public String toString() {
        return "StreamConfiguration [streamID=" +
               streamID +
               ", patternID=" +
               patternID+
               ", labelIndex =" +
               labelIndex +
               ", eventElements=" +
               eventElements +
               "]";
    }

}
