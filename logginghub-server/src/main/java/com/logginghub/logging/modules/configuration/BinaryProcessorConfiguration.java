package com.logginghub.logging.modules.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.logginghub.logging.modules.BinaryProcessorModule;
import com.logginghub.utils.JAXBConfiguration;
import com.logginghub.utils.module.Configures;

@Configures(BinaryProcessorModule.class) @SuppressWarnings("restriction") @XmlRootElement @XmlAccessorType(XmlAccessType.FIELD) public class BinaryProcessorConfiguration {

    @XmlAttribute private long fileCheckIntervalMilliseconds = 1000;
    @XmlAttribute private String inputPath = "binarylogs";
    @XmlAttribute private String outputPath = "binarylogs-processed";
    @XmlAttribute private String resultsPath = "results";
    @XmlAttribute private String prefix = "";
    @XmlAttribute private int httpPort = 8087;
    @XmlElement(name = "customProcessor") private List<String> customProcessors = new ArrayList<String>();
    @XmlElement(name = "regexProcessor") private List<RegexExtractingProcessorConfiguration> regexProcessorsConfiguration = new ArrayList<RegexExtractingProcessorConfiguration>();
    @XmlElement(name = "eventCounter") private List<EventCountingProcessorConfiguration> eventCounterProcessorsConfiguration = new ArrayList<EventCountingProcessorConfiguration>();
    @XmlElement(name = "badEventReport") private List<BadEventsReportConfiguration> badEventReportsConfiguration = new ArrayList<BadEventsReportConfiguration>();

    @Override public String toString() {
        return "DataFileProcessorConfiguration [fileCheckIntervalMilliseconds=" +
               fileCheckIntervalMilliseconds +
               ", inputPath=" +
               inputPath +
               ", outputPath=" +
               outputPath +
               ", resultsPath=" +
               resultsPath +
               ", customProcessors=" +
               customProcessors +
               "]";
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public List<BadEventsReportConfiguration> getBadEventReportsConfiguration() {
        return badEventReportsConfiguration;
    }

    public List<EventCountingProcessorConfiguration> getEventCounterProcessorsConfiguration() {
        return eventCounterProcessorsConfiguration;
    }

    public void setEventCounterProcessorsConfiguration(List<EventCountingProcessorConfiguration> eventCounterProcessorsConfiguration) {
        this.eventCounterProcessorsConfiguration = eventCounterProcessorsConfiguration;
    }

    public List<RegexExtractingProcessorConfiguration> getRegexProcessorsConfiguration() {
        return regexProcessorsConfiguration;
    }

    public void setRegexProcessorsConfiguration(List<RegexExtractingProcessorConfiguration> regexProcessorsConfiguration) {
        this.regexProcessorsConfiguration = regexProcessorsConfiguration;
    }

    public void setCustomProcessors(List<String> processors) {
        this.customProcessors = processors;
    }

    public List<String> getCustomProcessors() {
        return customProcessors;
    }

    public void setFileCheckIntervalMilliseconds(long fileCheckIntervalMilliseconds) {
        this.fileCheckIntervalMilliseconds = fileCheckIntervalMilliseconds;
    }

    public long getFileCheckIntervalMilliseconds() {
        return fileCheckIntervalMilliseconds;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public File getInputFolder() {
        return new File(inputPath);

    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setResultsPath(String resultsPath) {
        this.resultsPath = resultsPath;
    }

    public String getResultsPath() {
        return resultsPath;
    }

    public File getOutputFolder() {
        return new File(outputPath);
    }

    public File getResultsFolder() {
        return new File(resultsPath);
    }

    public static BinaryProcessorConfiguration load(String path) {
        return JAXBConfiguration.loadConfiguration(BinaryProcessorConfiguration.class, path);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
