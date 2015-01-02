package com.logginghub.utils.remote;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.logginghub.utils.logging.Logger;

public class ClasspathResolver {
    private List<ClassDataProvider> classProviders = new ArrayList<ClassDataProvider>();
    
    private static final Logger logger = Logger.getLoggerFor(ClasspathResolver.class);

    public ClasspathResolver() {
        String classpath = System.getProperty("java.class.path");

        String[] split = classpath.split(Character.toString(File.pathSeparatorChar));
        for (String string : split) {
            File path = new File(string);
            if (path.exists()) {
                if (path.isDirectory()) {
                    classProviders.add(new FolderClassProvider(path));
                    logger.debug("Added new folder class provider for path : {}", path.getAbsolutePath());

                }
                else if (path.isFile()) {
                    classProviders.add(new JarClassProvider(path));
                    logger.debug("Added new jar class provider for path : {}", path.getAbsolutePath());
                }
            }
        }
    }

    public enum Type {
        Class,
        Resource
    };

    public byte[] getResourceBytes(String resourcePath) {
        return getBytes(resourcePath, Type.Resource);
    }

    public byte[] getClassBytes(String className) {
        return getBytes(className, Type.Class);
    }

    public List<ClassDataProvider> getClassProviders() {
        return classProviders;
    }

    public byte[] getBytes(String className, Type type) {
        logger.trace("Data request for {} name {}", type, className);

        byte[] classBytes = null;
        for (ClassDataProvider provider : classProviders) {
            if (type == Type.Class) {
                classBytes = provider.getClassBytes(className);
            }
            else {
                classBytes = provider.getResourceBytes(className);
            }

            if (classBytes != null) {
                logger.trace("Found data through provider {}", provider);
                break;
            }
            else {
                logger.trace("Didn't find {} through provider {}", type, provider);
            }
        }
        
        if (classBytes != null) {
            logger.trace("Found {} bytes of data", classBytes.length);
        }
        else {
            logger.trace("Failed to find the {} anywhere, returning empty", type);
            classBytes = new byte[] {};
        }

        return classBytes;
    }

}
