package com.logginghub.utils.logging;

public class LevelSetter {
    private String partialClass;
    private int level;

    public int getLevel() {
        return level;
    }
    
    public String getPartialClass() {
        return partialClass;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setPartialClass(String partialClass) {
        this.partialClass = partialClass;
    }

    public void apply(Logger logger) {
        if(logger.getName().contains(partialClass)) {
            logger.setLevel(level);
        }
    }

    @Override public String toString() {
        return "LevelSetter [partialClass=" + partialClass + ", level=" + level + "]";
    }
    
    
    
}
