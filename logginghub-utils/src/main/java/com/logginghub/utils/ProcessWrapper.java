package com.logginghub.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProcessWrapper {
    private String[] command;
    private Process process;

    private InputStream errorStream;
    private InputStream inputStream;

    private InputStreamReaderThread inputThread;
    private InputStreamReaderThread errorThread;
    private InputStreamReaderThreadListener outputHandler;
    private InputStreamReaderThreadListener errorHandler;

    // private static Logger logger =
    // Logger.getLogger(ProcessWrapper.class.getName());

    public static ProcessWrapper execute(String string, InputStreamReaderThreadListener outputHandler, InputStreamReaderThreadListener errorHandler)
                    throws IOException {
        return execute(new String[] { string }, false, outputHandler, errorHandler);
    }

    public static ProcessWrapper execute(List<String> command) throws IOException {
        String[] commandArray = (String[]) command.toArray(new String[] {});
        return execute(commandArray, false, null, null);
    }

    public static ProcessWrapper execute(String[] command, boolean echo) throws IOException {
        InputStreamReaderThreadListener outputHanlder = new StringBufferInputStreamReaderThreadListener();
        InputStreamReaderThreadListener errorHanlder = new StringBufferInputStreamReaderThreadListener();
        return execute(command, echo, outputHanlder, errorHanlder);
    }

    public static ProcessWrapper execute(String[] command) throws IOException {
        return execute(command, false);
    }

    public static ProcessWrapper execute(List<String> command, boolean echo) throws IOException {
        String[] commandArray = (String[]) command.toArray(new String[] {});
        return execute(commandArray, echo, null, null);
    }

    public static ProcessWrapper execute(InputStreamReaderThreadListener outputHandler, InputStreamReaderThreadListener errorHandler, String... args)
                    throws IOException {
        return execute(args, false, outputHandler, errorHandler);
    }

    public static ProcessWrapper execute(File workingDirectory,
                                         List<String> command,
                                         boolean echo,
                                         InputStreamReaderThreadListener outputHandler,
                                         InputStreamReaderThreadListener errorHandler) throws IOException {
        return execute(workingDirectory, StringUtils.toArray(command), echo, outputHandler, errorHandler);

    }

    public static ProcessWrapper execute(String name,
                                         File workingDirectory,
                                         List<String> command,
                                         boolean echo,
                                         InputStreamReaderThreadListener outputHandler,
                                         InputStreamReaderThreadListener errorHandler) throws IOException {
        return execute(name, workingDirectory, StringUtils.toArray(command), echo, outputHandler, errorHandler);

    }

    /**
     * Starts the process in a clone of the parent environment, but leaves dealing with the output
     * and error streams up to the caller.
     * 
     * @param workingDirectory
     * @param command
     * @return
     * @throws IOException
     */
    public static ProcessWrapper execute(File workingDirectory, String[] command) throws IOException {
        ProcessWrapper wrapper = new ProcessWrapper();
        wrapper.command = command;

        Map<String, String> getenv = System.getenv();
        String[] envp = new String[getenv.size()];
        Iterator<String> iterator = getenv.keySet().iterator();
        for (int i = 0; i < envp.length; i++) {
            String key = iterator.next();
            envp[i] = new StringBuilder().append(key).append("=").append(getenv.get(key)).toString();
        }

        Process exec = Runtime.getRuntime().exec(command, envp, workingDirectory);
        wrapper.process = exec;
        wrapper.errorStream = exec.getErrorStream();
        wrapper.inputStream = exec.getInputStream();
        return wrapper;
    }

    public static ProcessWrapper execute(File workingDirectory,
                                         String[] command,
                                         boolean echo,
                                         InputStreamReaderThreadListener outputHandler,
                                         InputStreamReaderThreadListener errorHandler) throws IOException {
        return execute("ProcessReader", workingDirectory, command, echo, outputHandler, errorHandler);
    }

    public static ProcessWrapper execute(String name,
                                         File workingDirectory,
                                         String[] command,
                                         boolean echo,
                                         InputStreamReaderThreadListener outputHandler,
                                         InputStreamReaderThreadListener errorHandler) throws IOException {
        ProcessWrapper wrapper = new ProcessWrapper();
        wrapper.command = command;
        wrapper.outputHandler = outputHandler;
        wrapper.errorHandler = errorHandler;

        // logger.info(String.format("Executing [%s]",
        // Arrays.toString(command)));

        Map<String, String> getenv = System.getenv();
        String[] envp = new String[getenv.size()];
        Iterator<String> iterator = getenv.keySet().iterator();
        for (int i = 0; i < envp.length; i++) {
            String key = iterator.next();
            envp[i] = new StringBuilder().append(key).append("=").append(getenv.get(key)).toString();
        }

        Process exec = Runtime.getRuntime().exec(command, envp, workingDirectory);        
        wrapper.process = exec;

        wrapper.errorStream = exec.getErrorStream();
        wrapper.inputStream = exec.getInputStream();

        wrapper.inputThread = new InputStreamReaderThread(wrapper.inputStream, outputHandler);
        wrapper.errorThread = new InputStreamReaderThread(wrapper.errorStream, errorHandler);

        wrapper.inputThread.setName(name);
        wrapper.errorThread.setName(name);

        if (echo) {
            wrapper.inputThread.echoTo(System.out);
            wrapper.errorThread.echoTo(System.err);
        }
        wrapper.inputThread.start();
        wrapper.errorThread.start();

        return wrapper;
    }

    public static ProcessWrapper execute(String[] command,
                                         boolean echo,
                                         InputStreamReaderThreadListener outputHandler,
                                         InputStreamReaderThreadListener errorHandler) throws IOException {
        return execute(new File("."), command, echo, outputHandler, errorHandler);
    }

    public Process getProcess() {
        return process;
    }

    public String[] getCommand() {
        return command;
    }

    public void waitFor() {
        if (process != null) {
            try {
                process.waitFor();
            }
            catch (InterruptedException e) {

            }
        }
                
        inputThread.join();
        errorThread.join();
    }

    /**
     * Wait for the wrapped process to complete and package up the output, error and return codes
     * into a ProcessResults object.
     * 
     * @return
     */
    // public ProcessResults getResults()
    // {
    // waitFor();
    // ProcessResults results = new ProcessResults();
    // results.setOutput(getOutput());
    // results.setError(getError());
    // results.setReturnCode(getProcess().exitValue());
    // return results;
    // }

    public InputStream getErrorStream() {
        return errorStream;
    }

    public InputStreamReaderThread getErrorThread() {
        return errorThread;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public InputStreamReaderThread getInputThread() {
        return inputThread;
    }

    public InputStreamReaderThreadListener getErrorHandler() {
        return errorHandler;
    }

    public InputStreamReaderThreadListener getOutputHandler() {
        return outputHandler;
    }

    public void closeProcessCleanly() {
        process.destroy();
        try {
            process.waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
                       
        FileUtils.closeQuietly(process.getInputStream(), process.getOutputStream(), process.getErrorStream());
    }

}
