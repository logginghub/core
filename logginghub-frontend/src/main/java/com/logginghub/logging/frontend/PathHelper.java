package com.logginghub.logging.frontend;

import java.io.File;

public class PathHelper {
    
    private static File logViewerSettingsPath;

    static {
        logViewerSettingsPath = getDefaultLogViewerSettingsPath();
    }
    
    public static File getColumnsFile(String propertiesName) {
        File logViewer = getLogViewerSettingsPath();
        File properties = new File(logViewer, propertiesName + ".columns.properties");
        return properties;
    }

    public static File getSettingsFile(String propertiesName) {
        File logViewer = getLogViewerSettingsPath();
        File properties = new File(logViewer, propertiesName + ".properties");
        return properties;
    }

    public static void setLogViewerSettingsPath(File logViewerSettingsPath) {
        PathHelper.logViewerSettingsPath = logViewerSettingsPath;
    }
    
    public static File getLogViewerSettingsPath() {
        return logViewerSettingsPath;
    }
    
    public static File getDefaultLogViewerSettingsPath() {
        String userhomePath = System.getProperty("user.home");
        File userhome = new File(userhomePath);
        File vertexlabs = new File(userhome, ".vertexlabs");
        File logViewer = new File(vertexlabs, "logviewer");
        logViewer.mkdirs();
        return logViewer;
    }
}
