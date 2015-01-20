package com.logginghub.logging.frontend.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)  public class AbstractContainerConfiguration {
    
    @XmlElement private List<StackTraceViewConfiguration> stackTraceView = new ArrayList<StackTraceViewConfiguration>();
    @XmlElement private List<HistoryViewConfiguration> historyView = new ArrayList<HistoryViewConfiguration>();
    @XmlElement private List<RealtimeViewConfiguration> realtimeView = new ArrayList<RealtimeViewConfiguration>();

    public List<StackTraceViewConfiguration> getStackTraceViews() {
        return stackTraceView;
    }
    
    public List<RealtimeViewConfiguration> getRealtimeViews() {
        return realtimeView;
    }
    
    public List<HistoryViewConfiguration> getHistoryViews() {
        return historyView;
    }
}
