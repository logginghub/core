package com.logginghub.logging.modules.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.logginghub.logging.modules.SQLExtractModule;
import com.logginghub.utils.module.Configures;

@Configures(SQLExtractModule.class) @XmlAccessorType(XmlAccessType.FIELD) public class SQLExtractConfiguration {

    @XmlAttribute private String logEventDestinationRef;
    @XmlElement private List<DatabaseConfiguration> database = new ArrayList<DatabaseConfiguration>();
    @XmlElement private List<SQLExtractQueryConfiguration> query = new ArrayList<SQLExtractQueryConfiguration>();
    
    public String getLogEventDestinationRef() {
        return logEventDestinationRef;
    }
    
    public void setLogEventDestinationRef(String logEventDestinationRef) {
        this.logEventDestinationRef = logEventDestinationRef;
    }
    
    public List<DatabaseConfiguration> getDatabases() {
        return database;
    }
    
    public List<SQLExtractQueryConfiguration> getQueries() {
        return query;
    }

}
