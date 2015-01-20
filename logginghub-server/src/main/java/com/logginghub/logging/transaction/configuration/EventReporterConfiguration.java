package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class EventReporterConfiguration {

    @XmlElement LogEventTemplateConfiguration successEventTemplate = new LogEventTemplateConfiguration();
    @XmlElement LogEventTemplateConfiguration failureEventTemplate = new LogEventTemplateConfiguration();
    @XmlAttribute boolean reportSuccess = false;
    @XmlAttribute boolean reportFailure = true;
    @XmlAttribute boolean singleLine = false;
    @XmlAttribute String successDestination;
    @XmlAttribute String failureDestination;
    @XmlAttribute String warningAt;
    
    public EventReporterConfiguration() {
        successEventTemplate.setLevel("info");
        failureEventTemplate.setLevel("warning");
    }

    public void setWarningAt(String warningAt) {
        this.warningAt = warningAt;
    }

    public String getWarningAt() {
        return warningAt;
    }

    public boolean isReportSuccess() {
        return reportSuccess;
    }

    public void setReportSuccess(boolean reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    public boolean isReportFailure() {
        return reportFailure;
    }

    public void setReportFailure(boolean reportFailure) {
        this.reportFailure = reportFailure;
    }

    public LogEventTemplateConfiguration getSuccessEventTemplate() {
        return successEventTemplate;
    }

    public void setSuccessEventTemplate(LogEventTemplateConfiguration successEventTemplate) {
        this.successEventTemplate = successEventTemplate;
    }

    public LogEventTemplateConfiguration getFailureEventTemplate() {
        return failureEventTemplate;
    }

    public void setFailureEventTemplate(LogEventTemplateConfiguration failureEventTemplate) {
        this.failureEventTemplate = failureEventTemplate;
    }

    public String getSuccessDestination() {
        return successDestination;
    }

    public String getFailureDestination() {
        return failureDestination;
    }

    public void setSuccessDestination(String successDestination) {
        this.successDestination = successDestination;
    }

    public void setFailureDestination(String failureDestination) {
        this.failureDestination = failureDestination;
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }
}
