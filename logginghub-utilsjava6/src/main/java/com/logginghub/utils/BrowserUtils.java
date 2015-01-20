package com.logginghub.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.logginghub.utils.FileUtils;

public class BrowserUtils {
    public static void browseTo(URI uri) {

        if (!java.awt.Desktop.isDesktopSupported()) {
            throw new RuntimeException("Desktop not supported");
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            throw new RuntimeException("Browse action not supported");
        }

        try {
            desktop.browse(uri);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to open uri '" + uri + "'", e);
        }
    }

    public static void viewInBrowser(String html) {

        try {
            File file = File.createTempFile("test", ".html");
            FileUtils.write(html, file);
            browseTo(file.toURI());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}