package com.logginghub.logging.frontend.model;

import com.logginghub.logging.frontend.components.QuickFilterHistoryEntryModel;
import com.logginghub.logging.frontend.configuration.ActionConfiguration;
import com.logginghub.logging.frontend.configuration.ArgumentConfiguration;
import com.logginghub.logging.frontend.configuration.ColumnConfiguration;
import com.logginghub.logging.frontend.configuration.CustomDateFilterConfiguration;
import com.logginghub.logging.frontend.configuration.CustomFilterConfiguration;
import com.logginghub.logging.frontend.configuration.EnvironmentConfiguration;
import com.logginghub.logging.frontend.configuration.HighlighterConfiguration;
import com.logginghub.logging.frontend.configuration.HubConfiguration;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import com.logginghub.logging.frontend.configuration.NameMappingConfiguration;
import com.logginghub.logging.frontend.model.ActionModel.ArgumentModel;
import com.logginghub.logging.frontend.model.ColumnSettingsModel.ColumnSettingModel;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ModelBuilder {

    public LoggingFrontendModel buildModel(LoggingFrontendConfiguration configuration) {

        LoggingFrontendModel model = new LoggingFrontendModel();
        model.getShowDashboard().set(configuration.isShowDashboard());
        model.getPopoutCharting().set(configuration.isPopoutCharting());
        model.getSelectedRowFormat().set(RowFormatModel.fromConfiguration(configuration.getSelectedRowFormat()));
        model.getShowHubClearEvents().set(configuration.isShowHubClearEvents());
        model.getLocalRPCPort().set(configuration.getLocalRPCPort());
        model.getStartDemoSource().set(configuration.getStartDemoSource());

        List<EnvironmentConfiguration> environments = configuration.getEnvironments();
        for (EnvironmentConfiguration environmentConfiguration : environments) {
            EnvironmentModel environmentModel = buildModel(environmentConfiguration);
            model.getEnvironments().add(environmentModel);
        }


        return model;
    }

    private EnvironmentModel buildModel(EnvironmentConfiguration environmentConfiguration) {

        EnvironmentModel environmentModel = new EnvironmentModel();
        environmentModel.getName().set(environmentConfiguration.getName());
        environmentModel.getChannel().set(environmentConfiguration.getChannel());
        environmentModel.getOpenOnStartup().set(environmentConfiguration.isOpenOnStartup());
        environmentModel.getAutoLocking().set(environmentConfiguration.isAutoLocking());
        environmentModel.getRepoEnabled().set(environmentConfiguration.isRepoEnabled());
        environmentModel.getShowRegexOptionOnQuickFilters().set(environmentConfiguration.isShowRegexOptionOnQuickFilters());
        environmentModel.getRepoConnectionPoints().set(environmentConfiguration.getRepoConnectionPoints());
        environmentModel.getShowHistoryTab().set(environmentConfiguration.isShowHistoryTab());
        environmentModel.getShowHTMLEventDetails().set(environmentConfiguration.isShowHTMLEventDetails());
        environmentModel.getShowTimeSelectionView().set(environmentConfiguration.isShowTimeSelectionView());
        environmentModel.getShowFullStatusDetails().set(environmentConfiguration.isShowFullStatusDetails());

        environmentModel.getPanelBackgroundColour().set(environmentConfiguration.getPanelBackgroundColour());
        environmentModel.getTableBackgroundColour().set(environmentConfiguration.getTableBackgroundColour());
        environmentModel.getDetailViewBackgroundColour().set(environmentConfiguration.getDetailViewBackgroundColour());
        environmentModel.getSummaryBarBackgroundColour().set(environmentConfiguration.getSummaryBarBackgroundColour());

        environmentModel.getShowClearEvents().set(environmentConfiguration.isShowClearEvents());
        environmentModel.getShowTimeControl().set(environmentConfiguration.isShowTimeControl());
        environmentModel.getShowAddFilter().set(environmentConfiguration.isShowAddFilter());
        environmentModel.getShowEventDetailSummary().set(environmentConfiguration.isShowEventDetailSummary());

        environmentModel.getEventDetailsSeparatorHorizontalOrientiation().set(environmentConfiguration.isEventDetailsSeparatorHorizontalOrientiation());
        environmentModel.getEventDetailsSeparatorLocation().set(environmentConfiguration.getEventDetailsSeparatorLocation());

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

            model.getHost().set("Clustered - N/A");
            model.getName().set("Hub cluster");
            model.getChannel().set(null);
            model.getPort().set(-1);
            model.getConnectionState().set(HubConnectionModel.ConnectionState.NotConnected);

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
        loadCustomFilters(environmentConfiguration, environmentModel);
        loadActions(environmentConfiguration, environmentModel);

        return environmentModel;
    }

    private void loadActions(EnvironmentConfiguration environmentConfiguration, EnvironmentModel environmentModel) {
        List<ActionConfiguration> actions = environmentConfiguration.getActions();
        for (ActionConfiguration action : actions) {

            ActionModel actionModel = new ActionModel();
            actionModel.setCommand(action.getCommand());
            actionModel.setName(action.getName());
            actionModel.setPath(action.getPath());

            List<ArgumentConfiguration> arguments = action.getArguments();
            for (ArgumentConfiguration argument : arguments) {
                ArgumentModel argumentModel = actionModel. new ArgumentModel();
                argumentModel.setValue(argument.getValue());
                actionModel.getArguments().add(argumentModel);
            }

            environmentModel.getActions().add(actionModel);

        }
    }

    private void loadCustomFilters(EnvironmentConfiguration environmentConfiguration, EnvironmentModel environmentModel) {

        List<CustomFilterConfiguration> customerFilters = environmentConfiguration.getCustomFilters();
        for (CustomFilterConfiguration customerFilter : customerFilters) {

            CustomQuickFilterModel customQuickFilterModel = new CustomQuickFilterModel();

            customQuickFilterModel.getValue().set(customerFilter.getDefaultValue());
            customQuickFilterModel.getLabel().set(customerFilter.getLabel());
            customQuickFilterModel.getField().set(customerFilter.getField());
            customQuickFilterModel.getType().set(customerFilter.getType());
            customQuickFilterModel.getWidth().set(customerFilter.getWidth());

            String choices = customerFilter.getChoices();
            if(StringUtils.isNotNullOrEmpty(choices)) {
                String[] split = choices.split(",");
                for (String choice : split) {
                    String trimmed = choice.trim();
                    customQuickFilterModel.getChoices().add(trimmed);
                }
            }

            environmentModel.getCustomFilters().add(customQuickFilterModel);
        }

        List<CustomDateFilterConfiguration> customDateFilters =
                environmentConfiguration.getCustomDateFilters();

        for (CustomDateFilterConfiguration customDateFilter : customDateFilters) {

            CustomDateFilterModel customDateFilterModel = new CustomDateFilterModel();

            customDateFilterModel.getValue().set(customDateFilter.getDefaultValue());
            customDateFilterModel.getLabel().set(customDateFilter.getLabel());
            customDateFilterModel.getField().set(customDateFilter.getField());
            customDateFilterModel.getType().set(customDateFilter.getType());
            customDateFilterModel.getWidth().set(customDateFilter.getWidth());

            environmentModel.getCustomDateFilters().add(customDateFilterModel);
        }
    }

    private HubConnectionModel buildModel(HubConfiguration hubConfiguration) {
        HubConnectionModel hubModel = new HubConnectionModel();

        int port = hubConfiguration.getPort();
        InetSocketAddress inetSocketAddress = NetUtils.toInetSocketAddress(hubConfiguration.getHost(), port);

        hubModel.getHost().set(inetSocketAddress.getHostName());
        hubModel.getName().set(hubConfiguration.getName());
        hubModel.getChannel().set(hubConfiguration.getChannel());
        hubModel.getPort().set(inetSocketAddress.getPort());
        hubModel.getConnectionState().set(HubConnectionModel.ConnectionState.NotConnected);
        hubModel.getOverrideTime().set(hubConfiguration.getOverrideTime());
        return hubModel;
    }

    private HighlighterModel buildModel(HighlighterConfiguration highlighterConfiguration) {
        HighlighterModel model = new HighlighterModel();
        model.getPhrase().set(highlighterConfiguration.getPhrase());
        model.getColourHex().set(highlighterConfiguration.getColourHex());
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
                                                        columnConfiguration.getOrder(),
                                                        columnConfiguration.getAlignment(),
                                                        columnConfiguration.getMetadata(),
                                                        columnConfiguration.getAction(),
                                                        columnConfiguration.getRenderer()
                                                        ));

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
