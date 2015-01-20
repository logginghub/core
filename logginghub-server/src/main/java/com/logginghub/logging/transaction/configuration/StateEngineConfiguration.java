package com.logginghub.logging.transaction.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class StateEngineConfiguration {

    @XmlElement List<StateCaptureConfiguration> stateCapture = new ArrayList<StateCaptureConfiguration>();
    @XmlElement TransitionConfiguration transition;
    @XmlElement List<EventReporterConfiguration> eventReporter = new ArrayList<EventReporterConfiguration>();
    @XmlElement List<EmailReporterConfiguration> emailReporter = new ArrayList<EmailReporterConfiguration>();

    public List<EventReporterConfiguration> getEventReporters() {
        return eventReporter;
    }
    
    public List<EmailReporterConfiguration> getEmailReporters() {
        return emailReporter;
    }
    
    public List<StateCaptureConfiguration> getStateCaptures() {
        return stateCapture;
    }

    public TransitionConfiguration getTransition() {
        return transition;
    }

    public void setTransition(TransitionConfiguration transition) {
        this.transition = transition;
    }
}
