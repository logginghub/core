package com.logginghub.logging.transaction.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.logginghub.logging.transaction.EmailContent;

@XmlAccessorType(XmlAccessType.FIELD) public class EmailTemplateConfiguration {

    @XmlAttribute private String message = "";
    @XmlAttribute private String fromAddress = "";
    @XmlAttribute private String toAddress = "";
    @XmlAttribute private String ccAddress = "";
    @XmlAttribute private String bccAddress = "";
    @XmlAttribute private String subject = "";
    @XmlAttribute private boolean html = false;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getCcAddress() {
        return ccAddress;
    }

    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }

    public String getBccAddress() {
        return bccAddress;
    }

    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public EmailContent buildEmailContent() {
        EmailContent content = new EmailContent();
        content.setBccAddress(bccAddress);
        content.setCcAddress(ccAddress);
        content.setFromAddress(fromAddress);
        content.setHtml(html);
        content.setMessage(message);
        content.setSubject(subject);
        content.setToAddress(toAddress);
        return content;

    }

}
