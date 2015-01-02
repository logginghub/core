package com.logginghub.utils.maven;

public class MavenDependency extends VersionedMavenKey {

    private String scope;
    private boolean isOptional = false;

    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public boolean isRuntime() {
        return (scope.equals("compile") || scope.equals("runtime")) && !isOptional;
    }

    public MavenKey extractKey() {
        return new MavenKey(group, artifact);
         
    }

}
