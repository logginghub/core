package com.logginghub.utils.maven;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.VersionNumber;

public class VersionedMavenKey extends MavenKey {
    
     VersionNumber versionNumber;
    private String classifier;
    
    public VersionedMavenKey(String group, String artifact, VersionNumber versionNumber) {
        super(group, artifact);
        this.versionNumber = versionNumber;
    }
    
    public VersionedMavenKey() {}

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
//        if (!getClass().isAssignableFrom(VersionedMavenKey.class)) return false;
        VersionedMavenKey other = (VersionedMavenKey) obj;
        if (artifact == null) {
            if (other.artifact != null) return false;
        }
        else if (!artifact.equals(other.artifact)) return false;
        if (group == null) {
            if (other.group != null) return false;
        }
        else if (!group.equals(other.group)) return false;
        if (versionNumber == null) {
            if (other.versionNumber != null) return false;
        }
        else if (!versionNumber.equals(other.versionNumber)) return false;
        return true;
    }

    @Override public String toString() {
        return StringUtils.format("{}::{}::{}", getArtifact(), getGroup(), versionNumber);
    }
    
    
    public VersionNumber getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(VersionNumber versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
    
    public String getClassifier() {
        return classifier;
    }
}