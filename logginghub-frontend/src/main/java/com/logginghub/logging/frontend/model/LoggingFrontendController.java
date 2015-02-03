package com.logginghub.logging.frontend.model;

import java.io.File;

import com.logginghub.logging.frontend.PathHelper;
import com.logginghub.logging.frontend.components.QuickFilterHistoryEntryModel;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableListListener;
import com.logginghub.utils.persistence.Bag;
import com.logginghub.utils.persistence.ReflectionBinder;
import com.logginghub.utils.persistence.TextPersistence;

public class LoggingFrontendController {

    private static final Logger logger = Logger.getLoggerFor(LoggingFrontendController.class);
    private LoggingFrontendModel model;
    private File logViewerSettingsPath = PathHelper.getLogViewerSettingsPath();
    private static final String divider = "\t";

    public LoggingFrontendController(LoggingFrontendModel model) {
        this.model = model;
    }

    public void loadPersistedQuickFilters() {
        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (final EnvironmentModel environmentModel : environments) {
            File quickFiltersFile = getQuickFiltersFileForEnvironment(logViewerSettingsPath, environmentModel);
            if (quickFiltersFile.exists()) {
                String[] lines = FileUtils.readAsStringArray(quickFiltersFile);

                ReflectionBinder binder = new ReflectionBinder();
                TextPersistence persistence = new TextPersistence();

                for (String string : lines) {
                    try {
                        Bag fromString = persistence.fromString(string);
                        QuickFilterHistoryEntryModel model = binder.toObject(fromString, QuickFilterHistoryEntryModel.class);
                        environmentModel.getQuickFilterHistoryModel().getEntries().add(model);
                    }
                    catch (Exception e) {
                        logger.warning("Failed to parse quick filter history line '{}' from file '{}'", string, quickFiltersFile.getAbsolutePath());
                    }

                    // String[] split = string.split(divider);
                    // QuickFilterHistoryEntryModel entryModel = new
                    // QuickFilterHistoryEntryModel(split[0], true);
                    // entryModel.getCount().set(Long.parseLong(split[1]));
                    // entryModel.getLastUsed().set(Long.parseLong(split[2]));
                }
            }
        }
    }

    private File getQuickFiltersFileForEnvironment(File logViewerSettingsPath, final EnvironmentModel environmentModel) {
        File quickFiltersFile = new File(logViewerSettingsPath, environmentModel.getName() + ".quickfilters");
        return quickFiltersFile;
    }

    public void startQuickFilterPersistence() {

        ObservableList<EnvironmentModel> environments = model.getEnvironments();
        for (final EnvironmentModel environmentModel : environments) {
            environmentModel.getQuickFilterHistoryModel().getEntries().addListener(new ObservableListListener<QuickFilterHistoryEntryModel>() {
                @Override public void onRemoved(QuickFilterHistoryEntryModel t, int index) {
                    if (t.getUserDefined().get()) {
                        writeQuickFilters(environmentModel);
                    }
                }

                @Override public void onCleared() {}

                @Override public void onAdded(QuickFilterHistoryEntryModel t) {
                    if (t.getUserDefined().get()) {
                        writeQuickFilters(environmentModel);
                    }
                }
            });

        }

    }

    protected void writeQuickFilters(EnvironmentModel environmentModel) {
        File quickFiltersFileForEnvironment = getQuickFiltersFileForEnvironment(logViewerSettingsPath, environmentModel);

        ReflectionBinder binder = new ReflectionBinder();
        TextPersistence persistence = new TextPersistence();

        StringUtilsBuilder builder = StringUtils.builder();
        for (QuickFilterHistoryEntryModel entry : environmentModel.getQuickFilterHistoryModel().getEntries()) {
            if (entry.getUserDefined().asBoolean()) {

                try {
                    Bag fromObject = binder.fromObject(entry);
                    String string = persistence.toString(fromObject);
                    builder.appendLine(string);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                //
                // builder.append(entry.getCommand().asString());
                // builder.append(divider);
                // builder.append(entry.getCount().longValue());
                // builder.append(divider);
                // builder.appendLine(entry.getLastUsed().longValue());
            }
        }

        FileUtils.write(builder.toString(), quickFiltersFileForEnvironment);

    }

}
