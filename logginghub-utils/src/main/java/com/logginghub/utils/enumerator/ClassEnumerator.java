package com.logginghub.utils.enumerator;

import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassEnumerator
{
    public static Package enumerate(Class<?> c)
    {
        Package p = new Package();
        
        String property = System.getProperty("java.class.path");
        String[] split = property.split(";");
        for(String string : split)
        {
            process(string, p, c);
        }               

        return p;
    }

    private static void process(String string, Package p, Class<?> c)
    {
        if(string.endsWith(".jar"))
        {
            processJar(string, p, c);
        }
        else
        {
            processFolder(string, p, c);
        }
    }

    private static void processFolder(String string, Package p, Class<?> c)
    {
        File baseFolder = new File(string);

        recurse(baseFolder, baseFolder, p, c);
    }

    private static void recurse(File folder, File baseFolder, Package p, Class<?> c)
    {
        File[] listFiles = folder.listFiles();
        if(listFiles != null)
        {
            for(File file : listFiles)
            {
                if(file.isDirectory())
                {
                    Package child = p.addChild(file.getName());
                    recurse(file, baseFolder, child, c);
                }
                else
                {
                    String filename = file.getAbsolutePath();
                    if(filename.endsWith(".class"))
                    {
                        String baseFolderName = baseFolder.getAbsolutePath();
                        String relativeFilename = filename.substring(baseFolderName.length() + 1,
                                                                     filename.length() - 6);
                        String classname = relativeFilename.replace(File.separatorChar, '.');

                        loadClass(classname, p, c);
                    }
                }
            }
        }

    }

    private static void loadClass(String classname, Package p, Class<?> c)
    {
        try
        {
//            System.out.println("Checking if " + classname + " implements " + c);
            Class<?> forName = Class.forName(classname);
            if(c != null)
            {
                if(c.isAssignableFrom(forName) && !forName.isInterface() && c != forName)
                {
                    System.out.println("Found class " + forName);
                    p.addClass(forName);
                }
            }
            else
            {
                p.addClass(forName);
            }
        }
        catch (NoClassDefFoundError error)
        {
            // jshaw - this might seem like a daft thing to do, but quite
            // often you can't actually load all the classes on the
            // classpath because dependencies are all sorted at compile time...
            // System.out.println("Shit " + classname);
            // error.printStackTrace();
        }
        catch(VerifyError error)
        {
            // jshaw - another blinder, to do with obfuscation...
        }
        catch(IncompatibleClassChangeError error)
        {
            // jshaw - same...
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    private static void processJar(String string, Package p, Class<?> c)
    {
        try
        {
            JarFile jarFile = new JarFile(string);
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
//                System.out.println(entry);

                String name = entry.getName();
                if(name.endsWith(".class"))
                {
                    String relativeFilename = name.substring(0, name.length() - 6);
                    String classname = relativeFilename.replace('/', '.');

                    loadClass(classname, p, c);
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        ClassEnumerator.enumerate(null);
    }

}
