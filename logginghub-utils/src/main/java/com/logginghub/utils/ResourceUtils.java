package com.logginghub.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ResourceUtils {
    private static final String FILE = "file:";
    private static final String CLASSPATH = "classpath:";

    public static InputStream openStream(String resourcePath) {
        InputStream loaded;
        if (resourcePath.startsWith(CLASSPATH)) {
            String stripped = resourcePath.substring(CLASSPATH.length());
            loaded = openStreamFromClassPath(ResourceUtils.class, stripped);
        }
        else if (resourcePath.startsWith(FILE)) {
            String stripped = resourcePath.substring(FILE.length());
            loaded = FileUtils.openStream(stripped);
        }
        else {
            try {
                loaded = new FileInputStream(resourcePath);
            }
            catch (IOException e) {
                try {
                    loaded = openStreamFromClassPath(ResourceUtils.class, resourcePath);
                }
                catch (RuntimeException ee) {
                    try {
                        loaded = openStreamFromClassPath(ResourceUtils.class, "/" + resourcePath);
                    }
                    catch (RuntimeException ee2) {
                        try {
                            loaded = openStreamFromURL(resourcePath);
                        }
                        catch (RuntimeException ee3) {

                            throw new RuntimeException(String.format("Failed to load resource '%s' from the file system or the classpath, nor from a URL; please check your path.",
                                                                     resourcePath));
                        }
                    }
                }
            }
        }

        return loaded;
    }

    private static InputStream openStreamFromURL(String resourcePath) {
        try {
            URL url = new URL(resourcePath);
            return url.openStream();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(String.format("Resource path '%s' wasn't a valid url", resourcePath), e);
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Resource path '%s' couldn't be opened as a url", resourcePath), e);
        }

    }

    public static URL openURL(String resourcePath) {
        URL loaded = null;
        if (resourcePath.startsWith(CLASSPATH)) {
            String stripped = resourcePath.substring(CLASSPATH.length());
            loaded = openURLFromClassPath(ResourceUtils.class, stripped);
        }
        else if (resourcePath.startsWith(FILE)) {
            String stripped = resourcePath.substring(FILE.length());
            try {
                loaded = new File(stripped).toURL();
            }
            catch (MalformedURLException e) {
                throw new RuntimeException(String.format("Failed to convert url %s", resourcePath), e);
            }
        }
        else {
            try {
                File file = new File(resourcePath);
                if (file.exists()) {
                    loaded = file.toURL();
                }
            }
            catch (IOException e) {

            }

            if (loaded == null) {
                try {
                    loaded = openURLFromClassPath(ResourceUtils.class, resourcePath);
                }
                catch (RuntimeException ee) {
                    try {
                        loaded = openURLFromClassPath(ResourceUtils.class, "/" + resourcePath);
                    }
                    catch (RuntimeException ee2) {

                        throw new RuntimeException(String.format("Failed to load resource '%s' from the file system or the classpath; please check your path.",
                                                                 resourcePath));
                    }
                }
            }
        }

        return loaded;
    }

    public static URL openURLFromClassPath(Class<?> base, String filename) {
        URL resource = base.getResource(filename);
        if (resource == null) {
            throw new RuntimeException(String.format("Failed to find resource '%s' on the classpath using class '%s' as the base",
                                                     filename,
                                                     base.getName()));
        }
        return resource;
    }

    public static InputStream openStreamFromClassPath(Class<?> base, String filename) {
        InputStream resourceAsStream = base.getResourceAsStream(filename);
        if (resourceAsStream == null) {
            throw new RuntimeException(String.format("Failed to find resource '%s' on the classpath using class '%s' as the base",
                                                     filename,
                                                     base.getName()));
        }

        return resourceAsStream;
    }

    public static String[] readLines(String path) {

        try {
            InputStream stream = openStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            reader.close();
            return lines.toArray(new String[] {});

        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load resource '" + path + "'", e);
        }

    }

    public static String readFromClassPath(Class<?> base, String filename) {

        String read;

        if (base != null) {
            InputStream stream = openStreamFromClassPath(base, filename);
            read = FileUtils.read(stream);
        }
        else {

            InputStream stream = ResourceUtils.class.getResourceAsStream(filename);
            if (stream == null) {
                stream = ResourceUtils.class.getResourceAsStream("/" + filename);
            }

            if (stream == null) {
                throw new RuntimeException(String.format("Failed to find resource '%s' on the classpath in that path, or relative to the root",
                                                         filename));
            }

            read = FileUtils.read(stream);
        }

        return read;
    }

    public static String read(String resourcePath) {

        String loaded;
        if (resourcePath.startsWith(CLASSPATH)) {
            String stripped = resourcePath.substring(CLASSPATH.length());
            loaded = readFromClassPath(ResourceUtils.class, stripped);
        }
        else if (resourcePath.startsWith(FILE)) {
            String stripped = resourcePath.substring(FILE.length());
            loaded = FileUtils.read(stripped);
        }
        else {
            try {
                loaded = FileUtils.read(resourcePath);
            }
            catch (RuntimeException e) {
                try {
                    loaded = readFromClassPath(null, resourcePath);
                }
                catch (RuntimeException ee) {
                    throw new RuntimeException(String.format("Failed to load resource '%s' from the file system or the classpath; please check your path.",
                                                             resourcePath));
                }
            }
        }

        return loaded;
    }

    public static String readOrNull(String resourcePath) {

        String result;
        try {
            result = read(resourcePath);
        }
        catch (RuntimeException re) {
            if (re.getMessage().startsWith("Failed to load")) {
                result = null;
            }
            else {
                throw re;
            }
        }
        return result;
    }

    public static Image loadImage(String resourcePath) {
        URL url = ResourceUtils.openURL(resourcePath);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.getImage(url);
        return image;
    }

    public static Icon loadIcon(String string) {
        return new ImageIcon(loadImage(string));
    }
}
