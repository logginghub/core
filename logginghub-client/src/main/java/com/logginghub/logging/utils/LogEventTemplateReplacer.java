package com.logginghub.logging.utils;

import com.logginghub.logging.LogEvent;
import com.logginghub.utils.logging.Logger;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to replace log event values into string templates.
 */
public class LogEventTemplateReplacer {


    public static String replace(String input, LogEvent event) {

        Pattern patt = Pattern.compile("\\$\\{(.*?)\\}");
        Matcher m = patt.matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        while (m.find()) {
            String text = m.group(1);
            String replacement = getReplacement(text, event);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();

    }

    private static String getReplacement(String field, LogEvent event) {

        String replacement;

        if (field.equalsIgnoreCase("message")) {
            replacement = event.getMessage();
        } else if (field.equalsIgnoreCase("channel")) {
            replacement = event.getChannel();
        } else if (field.equalsIgnoreCase("originTime")) {
            replacement = Logger.toDateString(event.getOriginTime()).toString();
        } else if (field.equalsIgnoreCase("sequence")) {
            replacement = "" + event.getSequenceNumber();
        } else if (field.equalsIgnoreCase("formattedException")) {
            replacement = event.getFormattedException();
        } else if (field.equalsIgnoreCase("formattedObject")) {
            replacement = event.getFormattedObject() != null ? Arrays.toString(event.getFormattedObject()) : "null";
        } else if (field.equalsIgnoreCase("level")) {
            replacement = LoggingUtils.getJuliLevel(event.getLevel()).getLocalizedName();
        } else if (field.equalsIgnoreCase("hubTime")) {
            replacement = Logger.toDateString(event.getHubTime()).toString();
        } else if (field.equalsIgnoreCase("loggerName")) {
            replacement = event.getLoggerName();
        } else if (field.equalsIgnoreCase("pid")) {
            replacement = "" + event.getPid();
        } else if (field.equalsIgnoreCase("sourceAddress")) {
            replacement = event.getSourceAddress();
        } else if (field.equalsIgnoreCase("sourceApplication")) {
            replacement = event.getSourceApplication();
        } else if (field.equalsIgnoreCase("sourceClassName")) {
            replacement = event.getSourceClassName();
        } else if (field.equalsIgnoreCase("sourceHost")) {
            replacement = event.getSourceHost();
        } else if (field.equalsIgnoreCase("sourceMethodName")) {
            replacement = event.getSourceMethodName();
        } else if (field.equalsIgnoreCase("threadName")) {
            replacement = event.getThreadName();
        } else {
            replacement = event.getMetadata().get(field);
        }

        return replacement;

    }

}
