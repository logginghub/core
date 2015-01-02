package com.logginghub.utils;

@Deprecated
// TODO : Use logging instead 
public class Tracer {

    private static int baselineDepth;
    private static boolean enabled = false;
    private static int indent;
    private static String filter;
    private static boolean autoIndent = false;

    public static void enable() {
        enabled = true;
    }

    public static void enable(String filter) {
        Tracer.filter = filter;
        enabled = true;
    }

    public static void clearFilter() {
        filter = null;
    }

    public static void disable() {
        enabled = false;
    }

    public static void baseline() {
        baselineDepth = StacktraceUtils.getCurrentDepth();
    }

    public static void trace(String message) {
        if (isEnabled()) {
            traceInternal(2, message);
        }
    }

    private static void traceInternal(int callDepth, String message) {
        String methodName = StacktraceUtils.getCallingClassShortnameAndMethodName(callDepth);

        int currentDepth = StacktraceUtils.getCurrentDepth();
        int relativeDepth = currentDepth - baselineDepth;

        if(autoIndent) {
            relativeDepth = StacktraceUtils.getCurrentDepth();
        }
                
        String indentPadded = StringUtils.padLeft(message, message.length() + (indent * 4));

        StringBuilder builder = new StringBuilder();

        builder.append(" | trace | ");
        builder.append(StringUtils.padRight(Thread.currentThread().getName(), 40));
        builder.append(" | ");
        builder.append(StringUtils.padRight(methodName, 60));
        builder.append(" | ");
        builder.append(StringUtils.repeat("-", relativeDepth));
        builder.append(StringUtils.padLeft(indentPadded, relativeDepth));        
        System.out.println(builder.toString());
    }

    public static void trace(String message, Object... objects) {
        if (isEnabled()) {
            traceInternal(2, StringUtils.format(message, objects));
        }
    }

    private static boolean isEnabled() {

        boolean ok;

        if (enabled) {
            if (filter != null) {
                String calling = StacktraceUtils.getCallingClassAndMethodName(2);
                if (calling.contains(filter)) {
                    ok = true;
                }
                else {
                    ok = false;
                }

            }
            else {
                ok = true;
            }
        }
        else {
            ok = false;
        }

        return ok;
    }

    public static void indent() {
        indent++;
    }

    public static void outdent() {
        indent--;
    }

    public static void autoIndent(boolean autoIndent) {
        Tracer.autoIndent = autoIndent;        
    }

}
