package com.logginghub.logging.modules.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.modules.SocketTextReaderModule;
import com.logginghub.logging.transaction.configuration.LogEventTemplateConfiguration;
import com.logginghub.utils.VLPorts;
import com.logginghub.utils.module.Configures;

@Configures(SocketTextReaderModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class SocketTextReaderConfiguration {
    @XmlAttribute private int port = VLPorts.getSocketTextReader1DefaultPort();
    @XmlAttribute private String bindAddress = "0.0.0.0";
    @XmlAttribute private String eventDestinationRef;

    @XmlElement private LogEventTemplateConfiguration template = new LogEventTemplateConfiguration();

    public SocketTextReaderConfiguration() {
        template.setMessage("${message}");
    }

    public LogEventTemplateConfiguration getTemplate() {
        return template;
    }

    public void setTemplate(LogEventTemplateConfiguration template) {
        this.template = template;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEventDestinationRef() {
        return eventDestinationRef;
    }

    public void setEventDestinationRef(String eventDestinationRef) {
        this.eventDestinationRef = eventDestinationRef;
    }

    public String getBindAddress() {
        return bindAddress;
    }
    
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
}
