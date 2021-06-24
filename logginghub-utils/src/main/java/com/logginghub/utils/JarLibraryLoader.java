package com.logginghub.utils;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class JarLibraryLoader {

    private final static String osName = System.getProperty("os.name");

    public final static boolean IS_WIN32 = osName.startsWith("Windows");
    public final static boolean IS_AIX = osName.equals("AIX");
    public final static boolean IS_HPUX = osName.equals("HP-UX");
    public final static boolean IS_SOLARIS = osName.equals("SunOS");
    public final static boolean IS_LINUX = osName.equals("Linux");
    public final static boolean IS_MAC_OS = osName.equals("Mac OS X") || osName.equals("Darwin");
    public final static boolean IS_OSF1 = osName.equals("OSF1");
    public final static boolean IS_FREEBSD = osName.equals("FreeBSD");
    public final static boolean IS_NETWARE = osName.equals("NetWare");

    private static Set<String> alreadyLoadedLibraries = new HashSet<String>();

    public synchronized static void loadLib(String path, String name) {
        String key = path + "/" + name;
        if (alreadyLoadedLibraries.contains(key)) {
            // Already loaded this one
        }
        else {

            File fileOut = getOutputFile(name);
            if (fileOut.exists()) {
                System.load(fileOut.getAbsolutePath());
                alreadyLoadedLibraries.add(key);
            }
            else {
                try {
                    InputStream in = ResourceUtils.openStream(key);

                    FileUtils.write(in, fileOut);
                    in.close();
                    System.load(fileOut.getAbsolutePath());
                    alreadyLoadedLibraries.add(key);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to load required DLL", e);
                }
            }
        }
    }

    private static File getOutputFile(String name) {

        String folderName = System.getProperty("com.logginghub.utils.JarLibraryLoader.libraryTemp");

        if(StringUtils.isNotNullOrEmpty(folderName)) {
            folderName = FileUtils.checkForHome(folderName);
        }else {
            folderName = System.getProperty("java.io.tmpdir") + "/vllibtemp/";
        }

        File folder = new File(folderName);
        folder.mkdirs();

        File fileOut = new File(folder, name);
        return fileOut;
    }

    public static void loadLibAndAppendToLibraryPath(String path, String name) {
        loadLib(path, name);

        File outputFile = getOutputFile(name);

        String tempPath = outputFile.getParentFile().getAbsolutePath();
        String javaLibPath = System.getProperty("java.library.path");
        if (javaLibPath.contains(tempPath)) {
            // nothing to do, already there
        }
        else {
            String newPath = javaLibPath + File.pathSeparator + tempPath;
            System.setProperty("java.library.path", newPath);
        }
    }

}
