package com.logginghub.logging.hub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD) public class BaseFileLoggerConfiguration implements FileLogConfiguration {
    @XmlAttribute String filename = "hub";
    @XmlAttribute String folder = ".";
    @XmlAttribute long maximumFileSize = 50 * 1024 * 1024;
    @XmlAttribute int numberOfFiles = 3;
    @XmlAttribute int numberOfCompressedFiles = 2;
    @XmlAttribute boolean forceFlush = false;
    @XmlAttribute boolean openWithAppend = false;
    @XmlAttribute boolean autoNewline = true;
    @XmlAttribute boolean writeAsynchronously = true;
    @XmlAttribute int asynchronousQueueWarningSize = 10000;
    @XmlAttribute int asynchronousQueueDiscardSize = 100000;
    @XmlAttribute String extension = ".log";

    @XmlAttribute String formatter= "";
    @XmlAttribute String pattern = "";
    
    @XmlAttribute String channels;
    
    @XmlAttribute String source;
    
    public String getFormatter() {
        return formatter;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public void setFormatter(String formatter) {
        this.formatter = formatter;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public String getFileName() {
        return filename;
    }

    public long getMaximumFileSize() {
        return maximumFileSize;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getNumberOfCompressedFiles() {
        return numberOfCompressedFiles;
    }

    public int getAsynchronousQueueWarningSize() {
        return asynchronousQueueWarningSize;
    }

    public boolean getWriteAsynchronously() {
        return writeAsynchronously;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setNumberOfCompressedFiles(int numberOfCompressedFiles) {
        this.numberOfCompressedFiles = numberOfCompressedFiles;
    }

    public boolean getOpenWithAppend() {
        return openWithAppend;
    }

    public void setOpenWithAppend(boolean openWithAppend) {
        this.openWithAppend = openWithAppend;
    }

    public void setMaximumFileSize(long maximumFileSize) {
        this.maximumFileSize = maximumFileSize;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public void setWriteAsynchronously(boolean writeAsynchronously) {
        this.writeAsynchronously = writeAsynchronously;
    }

    public void setAsynchronousQueueWarningSize(int asynchronousQueueWarningSize) {
        this.asynchronousQueueWarningSize = asynchronousQueueWarningSize;
    }

    public int getAsynchronousQueueDiscardSize() {
        return asynchronousQueueDiscardSize;
    }

    public void setAsynchronousQueueDiscardSize(int asynchronousQueueDiscardSize) {
        this.asynchronousQueueDiscardSize = asynchronousQueueDiscardSize;
    }

    public boolean getAutoNewline() {
        return autoNewline;
    }

    public void setAutoNewline(boolean autoNewline) {
        this.autoNewline = autoNewline;
    }

    public boolean getForceFlush() {
        return forceFlush;
    }

    public void setForceFlush(boolean forceFlush) {
        this.forceFlush = forceFlush;
    }

    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }

    public String getChannels() {
        return channels;
    }
    
    public void setChannels(String channels) {
        this.channels = channels;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    
    public String getExtension() {
        return extension;
    }
    
    public void setExtension(String extension) {
        this.extension = extension;
    }
}
