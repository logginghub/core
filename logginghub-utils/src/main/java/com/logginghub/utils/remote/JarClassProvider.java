package com.logginghub.utils.remote;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;

public class JarClassProvider implements ClassDataProvider {
    private final JarFile jarFile;

    public JarClassProvider(File jarFile) {
        try {
            this.jarFile = new JarFile(jarFile);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to load jar file from %s", jarFile.getAbsolutePath()), e);
        }
    }

    public byte[] getClassBytes(String classname) {
        String path = StringUtils.classnameToFilename(classname).replace("\\", "/");
        return getResourceBytes(path);
    }

    public String toString() {
        return String.format("[%s] root [%s]", this.getClass().getSimpleName(), jarFile.getName());
    }

    public byte[] getResourceBytes(String path) {
        ZipEntry entry = jarFile.getEntry(path);

        byte[] b = null;

        if (entry != null) {
            try {
                b = FileUtils.readFully(jarFile.getInputStream(entry));
            }
            catch (IOException e) {
                throw new RuntimeException(String.format("Failed to get input stream from jar file for path %s", path), e);
            }
        }

        return b;
    }

    public Collection<String> enumerateClasses() {

        List<String> classNames = new ArrayList<String>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry nextElement = entries.nextElement();
            String name = nextElement.getName();
            if (name.endsWith(".class")) {
                String classname = name.substring(0, name.length() - 6).replace('/', '.');
                classNames.add(classname);
            }
        }

        return classNames;
    }
}