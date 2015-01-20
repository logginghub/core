package com.logginghub.logging.utils;

import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtilsTokeniser;
import com.logginghub.utils.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class AggregatedPatternParser {

    private List<Node> nodes = new ArrayList<AggregatedPatternParser.Node>();

    protected List<Node> getNodes() {
        return nodes;
    }

    public void parse(String groupBy, String pattern) {
        ValueStripper2 stripper = new ValueStripper2(pattern);
        parse(groupBy, stripper);
    }

    public void parse(String groupBy, ValueStripper2 stripper) {

        StringUtilsTokeniser st = new StringUtilsTokeniser(groupBy);

        nodes.clear();

        while (st.hasMore()) {
            try {
                String pre = st.upTo("{");
                if (!pre.isEmpty()) {
                    nodes.add(new StringNode(pre));
                }
            } catch (IllegalArgumentException e) {
                if (nodes.isEmpty()) {
                    throw new IllegalArgumentException(StringUtils.format(
                            "Failed to parse pattern string '{}' - failed at {} - couldn't find any pattern variables?",
                            groupBy,
                            st.getPointer()));
                }else {
                    // Append the rest of the string to the nodes and we are done
                    nodes.add(new StringNode(st.restOfString()));
                }
            }

            if (st.hasMore() && st.peekChar() == '{') {
                st.skip();

                String token = null;
                try {
                    token = st.upTo("}");
                    st.skip();
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(StringUtils.format(
                            "Failed to parse pattern string '{}' - failed at {} - unclosed pattern brace detected",
                            groupBy,
                            st.getPointer()));
                }

                Node node = null;

                if (node == null) {
                    try {
                        if (token.startsWith("event.")) {
                            LogEventField field = LogEventField.valueOf(token.substring("event.".length()));
                            node = new LogEventFieldNode(field);
                        }
                    } catch (IllegalArgumentException e) {

                    }
                }

                if (node == null) {
                    List<String> labels = stripper.getLabels();
                    int index = labels.indexOf(token);
                    if (index != -1) {
                        node = new PatternNode(index);
                    }
                }

                if (node == null) {
                    try {
                        int value = Integer.parseInt(token);
                        node = new PatternNode(value);
                    } catch (NumberFormatException e2) {

                    }
                }

                if (node == null) {
                    throw new IllegalArgumentException(StringUtils.format(
                            "Failed to parse pattern string '{}' - failed at {}, attempting to parse token '{}' - it wasn't a pattern variable, log event field or an integer",
                            groupBy,
                            st.getPointer(),
                            token));
                } else {
                    nodes.add(node);
                }
            }

        }

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException(StringUtils.format(
                    "Failed to parse pattern string '{}' - no pattern was detected",
                    groupBy));
        }

    }

    public String format(PatternisedLogEvent patternisedLogEvent) {

        StringBuilder builder = new StringBuilder();

        for (Node node : nodes) {
            builder.append(node.visit(patternisedLogEvent));
        }

        String formatted = builder.toString();
        return formatted;

    }

    interface Node {
        String visit(PatternisedLogEvent event);
    }

    private final static class StringNode implements Node {
        String string;

        public StringNode(String pre) {
            string = pre;
        }

        @Override public String toString() {
            return string;
        }

        @Override public String visit(PatternisedLogEvent event) {
            return string;
        }
    }

    private final static class PatternNode implements Node {
        int index;

        public PatternNode(int value) {
            index = value;
        }

        @Override public String toString() {
            return "{" + index + "}";
        }

        @Override public String visit(PatternisedLogEvent event) {
            return event.getVariable(index);
        }
    }

    private final static class LogEventFieldNode implements Node {
        LogEventField field;

        public LogEventFieldNode(LogEventField field2) {
            field = field2;
        }

        @Override public String toString() {
            return "{" + field + "}";
        }

        @Override public String visit(PatternisedLogEvent event) {
            String result = "<Unknown field>";
            switch (field) {
                case level:
                    result = Logger.getLevelName(event.getLevel(), true);
                    break;
                case sequenceNumber:
                    result = Long.toString(event.getSequenceNumber());
                    break;
                case sourceClassName:
                    result = event.getSourceClassName();
                    break;
                case sourceMethodName:
                    result = event.getSourceMethodName();
                    break;
                case message:
                    result = "[message not supported]";
                    break;
                case threadName:
                    result = event.getThreadName();
                    break;
                case time:
                    result = Logger.toLocalDateString(event.getTime()).toString();
                    break;
                case loggerName:
                    result = event.getLoggerName();
                    break;
                case sourceHost:
                    result = event.getSourceHost();
                    break;
                case sourceAddress:
                    result = event.getSourceAddress();
                    break;
                case sourceApplication:
                    result = event.getSourceApplication();
                    break;
                case channel:
                    result = event.getChannel();
                    break;
                case pid:
                    result = Integer.toString(event.getPid());
                    break;
                case formattedException:
                    result = "[formattedException not supported]";
                    break;
                case formattedObject:
                    result = "[formattedObject not supported]";
                    break;
                case metadata:
                    result = "[metadata not supported]";
                    break;
            }
            return result;
        }
    }
}
