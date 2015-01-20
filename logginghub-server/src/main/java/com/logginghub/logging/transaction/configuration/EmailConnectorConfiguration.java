package com.logginghub.logging.transaction.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.transaction.EmailConnector;
import com.logginghub.utils.module.Configures;

@Configures(EmailConnector.class) @XmlAccessorType(XmlAccessType.FIELD) public class EmailConnectorConfiguration {

    @XmlElement List<String> property = new ArrayList<String>();
    @XmlAttribute private String password;
    @XmlAttribute private String username;
    @XmlAttribute private String smtpHost;
    @XmlAttribute private String sendingThottle = "10 seconds";

    public String getSendingThottle() {
        return sendingThottle;
    }

    public void setSendingThottle(String sendingThottle) {
        this.sendingThottle = sendingThottle;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getProperties() {
        return property;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getSMTPHost() {
        return smtpHost;
    }
}
