package com.logginghub.utils.maven;

public interface PomModelProvider {
    PomModel getModel(VersionedMavenKey key);
}
