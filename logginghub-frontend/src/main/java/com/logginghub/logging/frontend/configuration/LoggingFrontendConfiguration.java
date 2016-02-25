package com.logginghub.logging.frontend.configuration;

import com.logginghub.utils.JAXBConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD) @XmlRootElement public class LoggingFrontendConfiguration {

    @XmlElement(name = "environment") private List<EnvironmentConfiguration> environments = new ArrayList<EnvironmentConfiguration>();
    @XmlAttribute private String productName = "Logging Front End - ";
    @XmlAttribute private String title = "no title";
    @XmlAttribute private String chartingConfigurationFile = "charting.xml";
    @XmlAttribute private String kryoHubHost;
    @XmlAttribute private int kryoHubPort;
    @XmlAttribute private boolean showDashboard = false;
    @XmlAttribute private boolean popoutCharting = false;
    @XmlElement(name = "selectedRowFormat") private RowFormatConfiguration selectedRowFormat = new RowFormatConfiguration();
    @XmlAttribute private boolean showOldCharting = false;
    @XmlAttribute private boolean showExperimental = true;
    @XmlAttribute private boolean showViewMenu = true;
    @XmlAttribute private boolean showChartingEditor = true;
    @XmlAttribute private boolean showHeapSlider = false;
    @XmlAttribute private boolean showHubClearEvents = false;

    @XmlAttribute private String backgroundColour = null;

    @XmlElement List<RemoteChartConfiguration> remoteCharting = new ArrayList<RemoteChartConfiguration>();
    @XmlAttribute private int localRPCPort = DONT_USE_LOCAL_RPC;
    public final static int DONT_USE_LOCAL_RPC = -1;

    public LoggingFrontendConfiguration() {

    }

    public String getBackgroundColour() {
        return backgroundColour;
    }

    public void setBackgroundColour(String backgroundColour) {
        this.backgroundColour = backgroundColour;
    }

    public int getLocalRPCPort() {
        return localRPCPort;
    }

    public List<RemoteChartConfiguration> getRemoteCharting() {
        return remoteCharting;
    }
    
    public RowFormatConfiguration getSelectedRowFormat() {
        return selectedRowFormat;
    }

    public boolean isShowHubClearEvents() {
        return showHubClearEvents;
    }

    public void setLocalRPCPort(int localRPCPort) {
        this.localRPCPort = localRPCPort;
    }

    public void setSelectedRowFormat(RowFormatConfiguration selectedRowFormat) {
        this.selectedRowFormat = selectedRowFormat;
    }

    public void setPopoutCharting(boolean popoutCharting) {
        this.popoutCharting = popoutCharting;
    }

    public boolean isPopoutCharting() {
        return popoutCharting;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setShowHubClearEvents(boolean showHubClearEvents) {
        this.showHubClearEvents = showHubClearEvents;
    }

    public void setShowOldCharting(boolean showOldCharting) {
        this.showOldCharting = showOldCharting;
    }

    public boolean isShowOldCharting() {
        return showOldCharting;
    }

    public void setShowDashboard(boolean showDashboard) {
        this.showDashboard = showDashboard;
    }

    public boolean isShowDashboard() {
        return showDashboard;
    }

    public List<EnvironmentConfiguration> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<EnvironmentConfiguration> environments) {
        this.environments = environments;
    }

    public String getKryoHubHost() {
        return kryoHubHost;
    }

    public void setKryoHubHost(String kryoHubHost) {
        this.kryoHubHost = kryoHubHost;
    }

    public int getKryoHubPort() {
        return kryoHubPort;

    }

    public void setKryoHubPort(int kryoHubPort) {
        this.kryoHubPort = kryoHubPort;
    }

    public static LoggingFrontendConfiguration loadConfiguration(String configurationPath) {
        LoggingFrontendConfiguration configuration = JAXBConfiguration.loadConfiguration(LoggingFrontendConfiguration.class, configurationPath);
        return configuration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override public String toString() {
        return "LoggingFrontendConfiguration [title=" +
               title +
               ", environments=" +
               environments +
               ", kryoHubHost=" +
               kryoHubHost +
               ", kryoHubPort=" +
               kryoHubPort +
               "]";
    }

    public void setChartingConfigurationFile(String chartingConfigurationFile) {
        this.chartingConfigurationFile = chartingConfigurationFile;
    }

    public String getChartingConfigurationFile() {
        return chartingConfigurationFile;
    }

    public boolean isShowExperimental() {
        return showExperimental;
    }
    
    public boolean isShowChartingEditor() {
        return showChartingEditor;
    }
    
    public void setShowChartingEditor(boolean showChartingEditor) {
        this.showChartingEditor = showChartingEditor;
    }

    public boolean isShowHeapSlider() {
        return showHeapSlider;
    }

    public void setShowExperimental(boolean showExperimental) {
        this.showExperimental = showExperimental;
    }
    
    public void setShowHeapSlider(boolean showHeapSlider) {
        this.showHeapSlider = showHeapSlider;
    }

    public boolean isShowViewMenu() {
        return showViewMenu;
    }



}

