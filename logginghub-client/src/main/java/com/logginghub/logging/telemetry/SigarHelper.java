package com.logginghub.logging.telemetry;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.logginghub.utils.JarLibraryLoader;

public class SigarHelper {

    public static double getCPU() {
        try {
            loadLibrary();
            Sigar sigar = new Sigar();
            CpuPerc cpu = sigar.getCpuPerc();

            return cpu.getCombined();
        }
        catch (SigarException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadLibrary() {
        System.setProperty("org.hyperic.sigar.path", "-");
        String path = getPathForOS();
        JarLibraryLoader.loadLib("", path);
    }

    public static int getPid() {
        loadLibrary();
        Sigar sigar = new Sigar();
        return (int) sigar.getPid();
    }

    public static String getPathForOS() {
        String start = "sigar";
        String arch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name");
        String ext;

        if (JarLibraryLoader.IS_WIN32) {
            ext = ".dll";
            osName = "winnt";
        }
        else if (JarLibraryLoader.IS_MAC_OS) {
            start = "libsigar";
            arch = "universal64";
            osName = "macosx";
            ext = ".dylib";
        }
        else if (JarLibraryLoader.IS_LINUX) {
            start = "libsigar";
            osName = "linux";
            if (arch.equals("i386")) {
                arch = "x86";
            }
            ext = ".so";
        }
        else {
            ext = ".so";
        }

        String path = start + "-" + arch + "-" + osName + ext;
        return path;
    }
}
