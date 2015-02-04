package com.logginghub.logging.frontend.views.stack;

import com.logginghub.logging.messages.StackSnapshot;
import com.logginghub.logging.messages.StackTrace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grouper {

    private List<ThreadGroupModel> groupings;

    public Grouper(List<ThreadGroupModel> groupings) {
        this.groupings = groupings;
    }

    public void process(StackSnapshot snapshot) {

        Map<String, GroupedStackTrace> grouped = new HashMap<String, GroupedStackTrace>();
        
        StackTrace[] traces = snapshot.getTraces();
        for (StackTrace stackTrace : traces) {
            
            String threadName = stackTrace.getThreadName();
            
            for (ThreadGroupModel threadGroupingModel : groupings) {
                
                if(threadGroupingModel.passes(threadName)) {
        
                    String groupName = threadGroupingModel.getName().get();
                    GroupedStackTrace groupedStackTrace = grouped.get(groupName);
                    if(groupedStackTrace == null) {
                        groupedStackTrace = new GroupedStackTrace(groupName);
                        grouped.put(groupName, groupedStackTrace);
                    }
                    
                    groupedStackTrace.add(stackTrace);
                    
                }
                
            }
            
        }
        
    }

}
