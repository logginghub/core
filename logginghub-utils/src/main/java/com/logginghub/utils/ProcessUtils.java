package com.logginghub.utils;

import java.io.File;
import java.io.IOException;

public class ProcessUtils {

    public static int getPid() {
        try {
            java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
            java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
            java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
            pid_method.setAccessible(true);

            int pid = (Integer) pid_method.invoke(mgmt);
            return pid;
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }

    public static ProcessResults executeInShell(String dir, String command) {

        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = false;

        ProcessResults results = new ProcessResults();
        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(new File(dir), new String[] { "cmd", "/c", command }, echo, out, err);
            wrapper.waitFor();

            results.setReturnCode(wrapper.getProcess().exitValue());
            results.setError(err.getBuffer().toString());
            results.setOutput(out.getBuffer().toString());
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", command), e);
        }

        return results;
    }

    public static ProcessWrapper launchInShell(String command) {
        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = false;

        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(new File("."), new String[] { "cmd", "/C", "start", command }, echo, out, err);
            return wrapper;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", command), e);
        }
    }

    public static ProcessWrapper launchInShell(File file) {

        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = false;

        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(file.getParentFile(), new String[] { "cmd", "/C", "start", file.getName() }, echo, out, err);
            return wrapper;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", file.getAbsolutePath()), e);
        }
    }

    public static ProcessWrapper launchInShell(File file, String arg) {

        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = false;

        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(file.getParentFile(), new String[] { "cmd", "/C", "start", file.getName(), arg }, echo, out, err);
            return wrapper;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", file.getAbsolutePath()), e);
        }
    }

    public static ProcessWrapper launch(String command) {
        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = false;

        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(new File("."), command.split(" "), echo, out, err);
            return wrapper;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start process '%s'", command), e);
        }
    }

    public static ProcessWrapper launchJava(File workingDir, String mainClass, File jar) {
        StringBufferInputStreamReaderThreadListener err = new StringBufferInputStreamReaderThreadListener();
        StringBufferInputStreamReaderThreadListener out = new StringBufferInputStreamReaderThreadListener();
        boolean echo = true;

        ProcessWrapper wrapper;
        try {
            wrapper = ProcessWrapper.execute(workingDir,
                                             new String[] { "cmd", "/C", "java", "-cp", jar.getAbsolutePath(), mainClass, "&&", "pause" },
                                             echo,
                                             out,
                                             err);
            return wrapper;
        }
        catch (IOException e) {
            throw new RuntimeException(String.format("Failed to start java class '%s'", mainClass), e);
        }
    }

}
