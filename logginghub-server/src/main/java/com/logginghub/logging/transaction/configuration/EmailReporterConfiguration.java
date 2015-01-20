package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD) public class EmailReporterConfiguration {

    @XmlElement EmailTemplateConfiguration successTemplate = new EmailTemplateConfiguration();
    @XmlElement EmailTemplateConfiguration failureTemplate = new EmailTemplateConfiguration();

    @XmlAttribute boolean reportSuccess = false;
    @XmlAttribute boolean reportFailure = true;

    @XmlAttribute String successDestination;
    @XmlAttribute String failureDestination;
    
    @XmlAttribute String warningAt;
    
    public String getSuccessDestination() {
        return successDestination;
    }
    
    public String getFailureDestination() {
        return failureDestination;
    }
    
    public void setSuccessTemplate(EmailTemplateConfiguration successTemplate) {
        this.successTemplate = successTemplate;
    }
    
    public void setFailureTemplate(EmailTemplateConfiguration failureTemplate) {
        this.failureTemplate = failureTemplate;
    }
       
    public void setSuccessDestination(String successDestination) {
        this.successDestination = successDestination;
    }
    
    public void setFailureDestination(String failureDestination) {
        this.failureDestination = failureDestination;
    }
    
    public void setReportFailure(boolean reportFailure) {
        this.reportFailure = reportFailure;
    }

    public void setReportSuccess(boolean reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    public boolean isReportFailure() {
        return reportFailure;
    }

    public boolean isReportSuccess() {
        return reportSuccess;
    }
    
    public EmailTemplateConfiguration getFailureTemplate() {
        return failureTemplate;
    }
    
    public EmailTemplateConfiguration getSuccessTemplate() {
        return successTemplate;
    }

    public String getWarningAt() {
        return warningAt;
    }
    
    public void setWarningAt(String warningAt) {
        this.warningAt = warningAt;
    }
}
