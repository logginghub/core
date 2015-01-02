package com.logginghub.utils.maven;

import com.logginghub.utils.StringUtils;

public class MavenKey {
    
     String artifact;
     String group;
    
    public MavenKey(String group, String artifact) {
        super();
        this.group = group;
        this.artifact = artifact;
    }
    
    public MavenKey() {}

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!getClass().isAssignableFrom(MavenKey.class)) return false;
        MavenKey other = (MavenKey) obj;
        if (artifact == null) {
            if (other.artifact != null) return false;
        }
        else if (!artifact.equals(other.artifact)) return false;
        if (group == null) {
            if (other.group != null) return false;
        }
        else if (!group.equals(other.group)) return false;
        return true;
    }

    @Override public String toString() {
        return StringUtils.format("{}::{}", artifact, group);
    }
    
    public String getArtifact() {
        return artifact;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
}