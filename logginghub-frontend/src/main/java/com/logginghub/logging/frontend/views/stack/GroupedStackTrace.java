package com.logginghub.logging.frontend.views.stack;

import java.util.ArrayList;
import java.util.List;

import com.logginghub.logging.messages.StackTrace;

public class GroupedStackTrace {

    private String groupName;
    private List<StackTrace> stackTraces = new ArrayList<StackTrace>();

    public GroupedStackTrace(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
    
    public void add(StackTrace stackTrace) {
        stackTraces.add(stackTrace);
    }

}
