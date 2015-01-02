package com.logginghub.utils;


public class StacktraceUtils {

    // Taken from
    // http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method
    private static final int CLIENT_CODE_STACK_INDEX;

    static {
        // Finds out the index of "this code" in the returned stack trace -
        // funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(StacktraceUtils.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }

    public static String getCurrentMethodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }

    public static int getCurrentDepth() {
        return Thread.currentThread().getStackTrace().length;
    }

    public static String getCallingMethodName() {
        return getCallingMethodName(1);
    }

    public static String getCallingClassName() {
        return getCallingClassName(1);
    }

    public static String getCallingClassAndMethodName() {
        return getCallingClassAndMethodName(1);
    }

    public static String getCallingMethodName(int depthAdjustment) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int depth = CLIENT_CODE_STACK_INDEX + depthAdjustment;
        if(depth >= stackTrace.length) {
            depth = stackTrace.length - 1;
        }
        return stackTrace[depth].getMethodName();
    }

    public static String getCallingClassName(int depthAdjustment) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int depth = CLIENT_CODE_STACK_INDEX + depthAdjustment;
        if(depth >= stackTrace.length) {
            depth = stackTrace.length - 1;
        }
        return stackTrace[depth].getClassName();
    }

    public static String getCallingClassAndMethodName(int depthAdjustment) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX + depthAdjustment];
        return StringUtils.format("{}.{}", stackTraceElement.getClassName(), stackTraceElement.getMethodName());
    }

    public static String getCallingClassShortnameAndMethodName(int depthAdjustment) {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX + depthAdjustment];
        return StringUtils.format("{}.{}", StringUtils.afterLast(stackTraceElement.getClassName(), "."), stackTraceElement.getMethodName());
    }

    public static String getStackTraceAsString(Throwable throwable) {
        return StringUtils.getStackTraceAsString(throwable);
    }
    
    public static String toString(Throwable throwable) {
        return StringUtils.getStackTraceAsString(throwable);
    }

    public static String getRootMessage(Throwable t) {
        Throwable pointer = t;

        while (pointer.getCause() != null) {
            pointer = pointer.getCause();
        }

        return pointer.getMessage();

    }

    public static Object combineMessages(Throwable t) {
        StringBuilder builder = new StringBuilder();
        
        Throwable pointer = t;
        builder.append(t.getMessage());
        String div = " -> ";
        
        while (pointer != null) {
            builder.append(div);
            builder.append("[");
            builder.append(pointer.getClass().getSimpleName());
            builder.append("] ");
            builder.append(pointer.getMessage());
            pointer = pointer.getCause();
        }

        return builder.toString();

    }

    public static Exception createPopulatedException() {
        RuntimeException re = new RuntimeException();
        re.fillInStackTrace();
        return re;
    }

}
