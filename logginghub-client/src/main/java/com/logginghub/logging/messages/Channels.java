package com.logginghub.logging.messages;

import com.logginghub.utils.StringUtils;

public class Channels {
    public static final String stackStrobeRequests = "requests/stack/strobe";
    public static final String stackHistoryRequests = "requests/stack/history";

    public static final String historyUpdates = "updates/history/index";
    public static final String stackSnapshots = "updates/stack/snapshots";
    public static final String telemetryUpdates = "updates/telemetry";
    public static final String patternisedEventUpdates = "updates/patternised";
    public static final String aggregatedEventUpdates = "updates/aggregated";

    public static String patternListRequests = "requests/patterns/list";
    public static String aggregationListRequests = "requests/aggregations/list";

    public static String pingRequests = "requests/ping";

    public static String levelSetting = "requests/levelsetting";

    public static String patternisedHistoryRequests = "requests/patternisedhistory";
    public static String aggregatedHistoryRequests = "requests/aggregatedhistory";
    public static String eventHistoryRequests = "requests/eventhistory";

    public static String reportListRequests = "requests/reports/list";
    public static String reportRunRequests = "requests/reports/run";
    public static String reportExecuteRequests = "requests/reports/execute";

    public static final String getPatternisedStream(int patternID) {
        return StringUtils.format("{}/{}", patternisedEventUpdates, patternID);
    }

    public static String getAggregatedStream(int aggregationID) {
        return StringUtils.format("{}/{}", aggregatedEventUpdates, aggregationID);         
    }
    
    public static final String divider = "/";

    public static String join(String[] channelArray) {
        if (channelArray == null) {
            return null;
        }
        else {
            StringBuilder fullChannel = new StringBuilder();
            String div = "";
            for (int i = 0; i < channelArray.length; i++) {
                fullChannel.append(div);
                fullChannel.append(channelArray[i]);
                div = Channels.divider;
            }

            String channel = fullChannel.toString();
            return channel;
        }
    }

    public static String[] toArray(String channel) {
        return channel.split(divider);
    }

    public static String getPrivateConnectionChannel(int connectionID) {
        return "connections/" + connectionID;
    }

    
}
