package com.logginghub.logging.utils;

import java.util.Arrays;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.Dictionary;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

public class PatternisedDictionaryEvent implements SerialisableObject {

    private int patternID;
    private long time;

    private String[] variables;
    // private Map<String, String> meta = new HashMap<String, String>();
    private LogEvent entry;
    private Dictionary dictionary;

    public PatternisedDictionaryEvent(long time, int patternID, Dictionary dictionary) {
        this.time = time;
        this.dictionary = dictionary;
        this.patternID = patternID;
    }

    public long getTime() {
        return time;
    }

    public int getPatternID() {
        return patternID;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setEntry(LogEvent entry) {
        this.entry = entry;
    }

    public LogEvent getEntry() {
        return entry;
    }

    @Override public String toString() {
        return "PatternisedEvent [patternID=" +
               patternID +
               ", time=" +
               time +
               ", variables =" +
               Arrays.toString(variables) +
               ", entry=" +
               entry +
               "]";
    }

    @Override public void read(SofReader reader) throws SofException {
        this.time = reader.readLong(1);
        this.patternID = reader.readInt(2);
        this.variables = reader.readStringArray(3);
    }

    @Override public void write(SofWriter writer) throws SofException {

        writer.write(1, time);
        writer.write(2, patternID);
        writer.write(3, variables);

    }

    
    public String[] getVariables() {
        return variables;
    }
    
    public void setVariables(String[] variables) {
        this.variables = variables;
    }

}
