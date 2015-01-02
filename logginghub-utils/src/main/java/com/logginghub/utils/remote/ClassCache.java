package com.logginghub.utils.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple file based cache for class files.
 * 
 * @author James
 */
public class ClassCache
{
    private static Logger logger = Logger.getLogger(ClassCache.class.getName());
    private final File m_base;

    public ClassCache(File base)
    {
        m_base = base;
    }
    
    public void cache(String classname, byte[] data) throws IOException
    {
        logger.fine(String.format("Caching [%s] (%d bytes)",
                                  classname,
                                  data.length));

        File classfile = getFileForClass(classname);
        classfile.getParentFile().mkdirs();

        BufferedOutputStream bos = null;
        try
        {
            FileOutputStream fos = new FileOutputStream(classfile);
            bos = new BufferedOutputStream(fos);
            bos.write(data);

            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("Written cache file at [%s]",
                                          classfile.getAbsolutePath()));
            }
        }
        finally
        {
            if (bos != null)
            {
                bos.close();
            }
        }
    }

    private File getFileForClass(String classname)
    {
        String filename = classnameToFilename(classname);
        File classfile = new File(m_base, filename);
        return classfile;
    }

    public byte[] load(String classname) throws IOException
    {
        if (logger.isLoggable(Level.FINE))
        {
            logger.fine(String.format("Trying to find [%s] in the class cache",
                                      classname));
        }

        File classfile = getFileForClass(classname);
        if (classfile.exists())
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("[%s] found in the cache at path [%s]",
                                          classname,
                                          classfile.getAbsolutePath()));
            }

            FileInputStream fis = new FileInputStream(classfile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bais = new ByteArrayOutputStream();
            try
            {
                byte[] buffer = new byte[100 * 1024];
                int read = 0;
                while ((read = bis.read(buffer)) > 0)
                {
                    bais.write(buffer, 0, read);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(String.format("Faield to read input stream"),
                                           e);
            }
            finally
            {
                bis.close();
            }

            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("[%s] - %d bytes read",
                                          classname,
                                          bais.size()));
            }

            return bais.toByteArray();
        }
        else
        {
            if (logger.isLoggable(Level.FINE))
            {
                logger.fine(String.format("[%s] could not be found in the cache (expected it here [%s]",
                                          classname,
                                          classfile.getAbsolutePath()));
            }

            return null;
        }
    }

    public static String classnameToFilename(String classname)
    {
        String filename = classname.replace('.', File.separatorChar) + ".class";
        return filename;
    }
}
