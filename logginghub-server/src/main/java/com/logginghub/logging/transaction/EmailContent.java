package com.logginghub.logging.transaction;

public class EmailContent {

    private String message = "";
    private String fromAddress = "";
    private String toAddress = "";
    private String ccAddress = "";
    private String bccAddress = "";
    private String subject = "";
    private boolean html = false;

    public String getBccAddress() {
        return bccAddress;
    }
    
    public String getCcAddress() {
        return ccAddress;
    }
    
    public void setBccAddress(String bccAddress) {
        this.bccAddress = bccAddress;
    }
    
    public void setCcAddress(String ccAddress) {
        this.ccAddress = ccAddress;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }
    
    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isHTML() {
        return html;
    }
    
    public void setHtml(boolean html) {
        this.html = html;
    }

    @Override public String toString() {
        return "EmailContent [subject=" +
               subject +
               ", message=" +
               message +
               ", toAddress=" +
               toAddress +
               ", fromAddress=" +
               fromAddress +
               ", ccAddress=" +
               ccAddress +
               ", bccAddress=" +
               bccAddress +
               ", html=" +
               html +
               "]";
    }

    
    
}
