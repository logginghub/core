package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.components.QuickFilterHistoryEntryModel;
import com.logginghub.logging.frontend.configuration.ColumnConfiguration;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HighlighterConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.NameMappingConfiguration;
import com.logginghub.logging.frontend.model.ColumnSettingsModel.ColumnSettingModel;
import com.logginghub.utils.NetUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ModelBuilder {

    public LoggingFrontendModel buildModel(LoggingFrontendConfiguration configuration) {

        LoggingFrontendModel model = new LoggingFrontendModel();
        model.setShowDashboard(configuration.isShowDashboard());
        model.setPopoutCharting(configuration.isPopoutCharting());
        model.setSelectedRowFormat(RowFormatModel.fromConfiguration(configuration.getSelectedRowFormat()));
        model.setShowHubClearEvents(configuration.isShowHubClearEvents());

        List<EnvironmentConfiguration> environments = configuration.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            EnvironmentModel environmentModel = buildModel(environmentConfiguration);
            model.getEnvironments().add(environmentModel);
        }


        return model;
    }

    private EnvironmentModel buildModel(EnvironmentConfiguration environmentConfiguration) {

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.set(EnvironmentModel.Fields.Name, environmentConfiguration.getName());
        environmentModel.set(EnvironmentModel.Fields.Channel, environmentConfiguration.getChannel());
        environmentModel.set(EnvironmentModel.Fields.OpenOnStartup, environmentConfiguration.isOpenOnStartup());
        environmentModel.set(EnvironmentModel.Fields.AutoLocking, environmentConfiguration.isAutoLocking());
        environmentModel.set(EnvironmentModel.Fields.RepoEnabled, environmentConfiguration.isRepoEnabled());
        environmentModel.set(EnvironmentModel.Fields.RepoConnectionPoints, environmentConfiguration.getRepoConnectionPoints());
        environmentModel.set(EnvironmentModel.Fields.ShowHistoryTab, environmentConfiguration.isShowHistoryTab());

        environmentConfiguration.setupDefaultLogConfiguration();
        environmentModel.setClustered(environmentConfiguration.isClustered());
        environmentModel.setEventMemoryMB(environmentConfiguration.getEventMemoryMB());
        environmentModel.setAutoRequestHistory(environmentConfiguration.getAutoRequestHistory());
        environmentModel.setDisableAutoScrollPauser(environmentConfiguration.getDisableAutoScrollPauser());

        environmentModel.setOutputLogConfiguration(environmentConfiguration.getOutputLogConfiguration());
        environmentModel.setWriteOutputLog(environmentConfiguration.isWriteOutputLog());

        environmentModel.setFilterCaseSensitive(environmentConfiguration.isFilterCaseSensitive());
        environmentModel.setFilterUnicode(environmentConfiguration.isFilterUnicode());

        environmentModel.getColumnSettingsModel().setDisableColumnFile(environmentConfiguration.isDisableColumnFile());

        List<HubConfiguration> hubs = environmentConfiguration.getHubs();
        if (environmentConfiguration.isClustered()) {
            HubConnectionModel model = new HubConnectionModel();

            for (HubConfiguration hub : hubs) {
                int port = hub.getPort();
                InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress(hub.getHost(), port);
                model.addConnectionPoint(inetSocketAddress);
            }

            model.set(HubConnectionModel.Fields.Host, "Clustered - N/A");
            model.set(HubConnectionModel.Fields.Name, "Hub cluster");
            model.set(HubConnectionModel.Fields.Channel, null);
            model.set(HubConnectionModel.Fields.Port, -1);
            model.set(HubConnectionModel.Fields.ConnectionState, HubConnectionModel.ConnectionState.NotConnected);

            environmentModel.getHubConnectionModels().add(model);

        } else {
            for (HubConfiguration hubConfiguration : hubs) {
                HubConnectionModel hubModel = buildModel(hubConfiguration);
                environmentModel.getHubConnectionModels().add(hubModel);
            }
        }

        List<HighlighterConfiguration> highlighters = environmentConfiguration.getHighlighters();
        for (HighlighterConfiguration highlighterConfiguration : highlighters) {
            HighlighterModel highlighterModel = buildModel(highlighterConfiguration);
            environmentModel.getHighlighters().add(highlighterModel);
        }

        List<String> quickFilters = environmentConfiguration.getQuickFilters();
        for (String string : quickFilters) {
            environmentModel.getQuickFilterHistoryModel().getEntries().add(new QuickFilterHistoryEntryModel(string, false));
        }

        environmentModel.addFilters(environmentConfiguration);

        loadColumnNameMappings(environmentConfiguration, environmentModel);
        loadLevelNameMappings(environmentConfiguration, environmentModel);
        loadColumnSettings(environmentConfiguration, environmentModel);

        return environmentModel;
    }

    private HubConnectionModel buildModel(HubConfiguration hubConfiguration) {
        HubConnectionModel hubModel = new HubConnectionModel();

        int port = hubConfiguration.getPort();
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress(hubConfiguration.getHost(), port);

        hubModel.set(HubConnectionModel.Fields.Host, inetSocketAddress.getHostName());
        hubModel.set(HubConnectionModel.Fields.Name, hubConfiguration.getName());
        hubModel.set(HubConnectionModel.Fields.Channel, hubConfiguration.getChannel());
        hubModel.set(HubConnectionModel.Fields.Port, inetSocketAddress.getPort());
        hubModel.set(HubConnectionModel.Fields.ConnectionState, HubConnectionModel.ConnectionState.NotConnected);
        hubModel.setOverrideTime(hubConfiguration.getOverrideTime());
        return hubModel;
    }

    private HighlighterModel buildModel(HighlighterConfiguration highlighterConfiguration) {
        HighlighterModel model = new HighlighterModel();
        model.set(HighlighterModel.Fields.Phrase, highlighterConfiguration.getPhrase());
        model.set(HighlighterModel.Fields.ColourHex, highlighterConfiguration.getColourHex());
        return model;
    }

    private void loadColumnNameMappings(EnvironmentConfiguration environmentConfiguration, EnvironmentModel environmentModel) {
        Map<Integer, String> columnNameMappings = environmentModel.getEventTableColumnModel().getColumnNameMappings();
        List<NameMappingConfiguration> columnMappings = environmentConfiguration.getColumnMappings();
        processMappings(columnNameMappings, columnMappings);
    }

    private void loadLevelNameMappings(EnvironmentConfiguration environmentConfiguration, EnvironmentModel environmentModel) {
        Map<Integer, String> levelNameMappings = environmentModel.getLevelNamesModel().getLevelNames();
        List<NameMappingConfiguration> columnMappings = environmentConfiguration.getLevelMappings();
        processMappings(levelNameMappings, columnMappings);
    }

    private void loadColumnSettings(EnvironmentConfiguration environmentConfiguration, EnvironmentModel environmentModel) {

        List<ColumnConfiguration> columnSetting = environmentConfiguration.getColumnSetting();
        for (ColumnConfiguration columnConfiguration : columnSetting) {

            environmentModel.getColumnSettingsModel()
                            .getColumnSettings()
                            .put(columnConfiguration.getName(),
                                 new ColumnSettingModel(columnConfiguration.getName(),
                                                        columnConfiguration.getWidth(),
                                                        columnConfiguration.getOrder()));

        }

    }

    private void processMappings(Map<Integer, String> levelNameMappings, List<NameMappingConfiguration> columnMappings) {
        for (NameMappingConfiguration columnMapping : columnMappings) {

            String from = columnMapping.getFrom();
            String to = columnMapping.getTo();

            for (Entry<Integer, String> mappingEntry : levelNameMappings.entrySet()) {
                if (mappingEntry.getValue().equalsIgnoreCase(from)) {
                    int columnIndex = mappingEntry.getKey();
                    levelNameMappings.put(columnIndex, to);
                }
            }

        }
    }
}
