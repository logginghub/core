package com.logginghub.utils;

import java.io.File;

public interface FileSyncListener {
    
    public static FileSyncListener noop = new FileSyncListener() {
        public void onFileCreated(File sourceFile, File destFile) {}
        public void onFileUpdated(File sourceFile, File destFile) {}
        public void onFileDeleted(File file) {}
    };
    
    void onFileCreated(File sourceFile, File destFile);
    void onFileUpdated(File sourceFile, File destFile);
    void onFileDeleted(File file);
}
