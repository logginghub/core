package com.logginghub.utils.remote;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LightweightRemoteClassLoader extends ClassLoader
{
    private ObjectOutputStream m_objectOutputStream;
    private Map<String, Class> classes = new HashMap<String, Class>();
    private static Logger logger = Logger.getLogger(LightweightRemoteClassLoader.class.getName());
    private final ObjectInputStream m_objectInputStream;
    private ClassCache m_cache = new ClassCache(new File("./classcache/"));
    private List<String> m_dontCachePackages = new ArrayList<String>();

    public LightweightRemoteClassLoader(ClassLoader classLoader,
                                        ObjectOutputStream socketHandler,
                                        ObjectInputStream objectInputStream)
    {
        super(classLoader);
        m_objectOutputStream = socketHandler;
        m_objectInputStream = objectInputStream;

        m_dontCachePackages.add("com.logginghub");

        logger.info(String.format("RemoteControlClassLoader created"));
    }

    // @Override public URL getResource(String name)
    // {
    // logger.info(String.format("LRCL getting resource : %s", name));
    // return super.getResource(name);
    // }

    @Override public InputStream getResourceAsStream(String name)
    {
        logger.fine(String.format("LRCL looking for resource (as stream) : %s",
                                  name));
        InputStream stream = super.getResourceAsStream(name);

        if (stream == null)
        {
            logger.fine(String.format("Parent stream was null, trying to load the resource remotely"));
            try
            {
                byte[] loadClassData = loadClassData(name);
                if (loadClassData.length > 0)
                {
                    stream = new ByteArrayInputStream(loadClassData);
                }
                else
                {
                    logger.fine(String.format("Remote stream was empty, failed to find resource %s",
                                                 name));
                    stream = null;
                }
            }
            catch (IOException e)
            {
                logger.log(Level.WARNING,
                           String.format("Failed to load resource [%s]", name),
                           e);
                stream = null;
            }
            catch (ClassNotFoundException e)
            {
                logger.log(Level.WARNING,
                           String.format("Failed to load resource [%s]", name),
                           e);
                stream = null;
            }
        }

        return stream;
    }

    //    
    // @Override protected URL findResource(String name)
    // {
    // logger.info(String.format("LRCL looking for resource : %s", name));
    // URL url = super.findResource(name);
    // if(url == null)
    // {
    //            
    // }
    //        
    // return url;
    // }

    public Class loadClass(String className) throws ClassNotFoundException
    {
        return findClass(className);
    }

    @Override public Class findClass(String className)
                    throws ClassNotFoundException
    {
        if (logger.isLoggable(Level.FINER))
        {
            logger.finer(String.format("Loading class [%s]", className));
        }

        byte classBytes[];
        Class result = null;
        result = (Class) classes.get(className);
        if (result != null)
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("Loading class [%s] from the cache",
                                          className));
            }

            return result;
        }

        try
        {
            if (logger.isLoggable(Level.FINER))
            {
                logger.finer(String.format("Attempting to load system class"));
            }
            Class<?> findSystemClass = findSystemClass(className);
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("Loading class [%s] from the system class loader",
                                          className));
            }
            classes.put(className, findSystemClass);
            return findSystemClass;
        }
        catch (Exception e)
        {
            if (logger.isLoggable(Level.FINER))
            {
                logger.finer(String.format("System class failed, it must be a normal class"));
            }
        }

        try
        {
            if (isCacheable(className))
            {
                classBytes = m_cache.load(className);
                if (classBytes != null)
                {
                    if (logger.isLoggable(Level.FINE))
                    {
                        logger.fine(String.format("Loading class [%s] from the disk cache",
                                                  className));
                    }
                    result = defineClass(className,
                                         classBytes,
                                         0,
                                         classBytes.length,
                                         null);
                    classes.put(className, result);
                    return result;
                }
            }
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING,
                       String.format("Failed to load class [%s] from the disk cache, will try and find it remotely",
                                     className),
                       e);
        }

        try
        {
            classBytes = loadClassData(className);
            if (classBytes.length == 0)
            {
                throw new ClassNotFoundException(String.format("Failed to find class %s",
                                                               className));
            }

            result = defineClass(className,
                                 classBytes,
                                 0,
                                 classBytes.length,
                                 null);
            
            classes.put(className, result);
            if(isCacheable(className))
            {
                m_cache.cache(className, classBytes);
            }
            return result;
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING,
                       String.format("Failed to load class [%s]", className),
                       e);
            return null;
        }
    }

    private boolean isCacheable(String className)
    {
        boolean isCacheable = true;

        List<String> dontCachePackages = m_dontCachePackages;
        for (String string : dontCachePackages)
        {
            if (className.startsWith(string))
            {
                isCacheable = false;
                break;
            }
        }
        return isCacheable;
    }

    private synchronized byte[] loadClassData(String className)
                    throws IOException, ClassNotFoundException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine(String.format("Attempting remote load of class %s",
                                      className));
        }

        if (logger.isLoggable(Level.FINE))
        {
            logger.fine(String.format("Sending request object"));
        }
        m_objectOutputStream.writeUnshared(className);

        if (logger.isLoggable(Level.FINE))
        {
            logger.fine(String.format("Reading response object"));
        }
        byte[] data = (byte[]) m_objectInputStream.readUnshared();

        if (logger.isLoggable(Level.FINE))
        {            
            logger.fine(String.format("Reposonse received, class data %d bytes",
                                      data.length));
            logger.fine(String.format("Loading class [%s] remotely", className));
        }

        return data;
    }

}
