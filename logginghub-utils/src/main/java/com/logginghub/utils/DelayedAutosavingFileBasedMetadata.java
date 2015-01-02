package com.logginghub.utils;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class DelayedAutosavingFileBasedMetadata extends FileBasedMetadata {
    private static final long serialVersionUID = 1L;
    private DelayedAction action;
    
    public DelayedAutosavingFileBasedMetadata(File source, int delayTimeMS) {
        super(source);
        action = new DelayedAction(delayTimeMS, TimeUnit.MILLISECONDS);
    }

    public static DelayedAutosavingFileBasedMetadata load(File source, int saveDelayTimeMS) {
        DelayedAutosavingFileBasedMetadata fileBasedMetadata = new DelayedAutosavingFileBasedMetadata(source, saveDelayTimeMS);
        fileBasedMetadata.load();
        return fileBasedMetadata;
    }

    @Override public Object put(Object key, Object value) {
        Object previous = super.put(key, value);
        action.execute(new Runnable() {
            public void run() {
                save();
            }
        });
        
        return previous;
    }
    
    public DelayedAction getAction() {
        return action;
    }
    
}
