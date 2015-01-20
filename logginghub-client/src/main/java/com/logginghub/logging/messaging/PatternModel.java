package com.logginghub.logging.messaging;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableProperty;

/**
 * Represents the capture pattern and label structure for the data extraction part of charting
 * 
 * @author James
 * 
 */
/*
 * Do not rename this class as the name is used the frontend charting configuration!
 */
public class PatternModel extends Observable {

    private ObservableInteger patternID = createIntProperty("patternID", -1);
    private ObservableProperty<String> pattern = createStringProperty("pattern", "");
    private ObservableProperty<String> name = createStringProperty("name", "");
    private ObservableProperty<Boolean> debug = createBooleanProperty("debug", false);
    private ObservableProperty<Boolean> cleanUp = createBooleanProperty("cleanUp", false);

    public PatternModel(String name, String pattern) {
        getName().set(name);
        getPattern().set(pattern);
    }

    public PatternModel() {}

    /**
     * Get the value of the pattern - which is the pattern structure itself. Needs to be passed to a
     * ValueStripper2 most likely to do anything with it.
     * 
     * @return
     */
    public ObservableProperty<String> getPattern() {
        return pattern;
    }

    /**
     * Get the unique ID of this pattern; this is the value that will be used when storing the
     * patternised data so it must be unique for a dataset
     * 
     * @return
     */
    public ObservableInteger getPatternID() {
        return patternID;
    }

    /**
     * Patterns can be named - not sure what this does though!
     * 
     * @return
     */
    public ObservableProperty<String> getName() {
        return name;
    }

    /**
     * Patterns in debug mode may output extra logging information, depending on the implementation.
     * 
     * @return
     */
    public ObservableProperty<Boolean> getDebug() {
        return debug;
    }

    /**
     * Should we apply pattern cleanup logic to this pattern? See {@link ValueStripper2} for more
     * details.
     * 
     * @return
     */
    public ObservableProperty<Boolean> getCleanUp() {
        return cleanUp;
    }

  
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cleanUp == null) ? 0 : cleanUp.hashCode());
        result = prime * result + ((debug == null) ? 0 : debug.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PatternModel other = (PatternModel) obj;
        if (cleanUp == null) {
            if (other.cleanUp != null) {
                return false;
            }
        }
        else if (!cleanUp.equals(other.cleanUp)) {
            return false;
        }
        if (debug == null) {
            if (other.debug != null) {
                return false;
            }
        }
        else if (!debug.equals(other.debug)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (pattern == null) {
            if (other.pattern != null) {
                return false;
            }
        }
        else if (!pattern.equals(other.pattern)) {
            return false;
        }
        return true;
    }

    // public static PatternModel fromXml(XmlEntry xmlEntry) {
    // PatternModel patternModel = new PatternModel();
    // patternModel.getName().set(xmlEntry.getAttribute("name"));
    // patternModel.getPattern().set(xmlEntry.getAttribute("pattern"));
    // patternModel.getDebug().set(xmlEntry.getBooleanAttribute("debug"));
    // patternModel.getCleanUp().set(xmlEntry.getBooleanAttribute("cleanup"));
    // return patternModel;
    // }

}
