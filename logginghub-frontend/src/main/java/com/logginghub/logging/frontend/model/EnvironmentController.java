package com.logginghub.logging.frontend.model;

import com.logginghub.logging.filters.TimeFieldFilter;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.json.JsonDecoder;
import com.logginghub.utils.observable.json.JsonEncoder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by james on 28/01/2016.
 */
public class EnvironmentController {

    private static final Logger logger = Logger.getLoggerFor(EnvironmentController.class);
    private final EnvironmentModel model;

    public EnvironmentController(EnvironmentModel model) {
        this.model = model;
    }

    public void deleteSearch(String searchName) {
        FilterBookmarkModel bookmarkByName = getBookmarkByName(searchName);
        if(bookmarkByName != null) {
            model.getFilterBookmarks().remove(bookmarkByName);
            persistSearches();
        }
    }

    public void loadSearches() {

        try {
            ObservableList<FilterBookmarkModel> filterBookmarks = model.getFilterBookmarks();
            filterBookmarks.clear();

            File file = new File(model.getConfigurationFolder().get(), model.getSavedSearchesFilename().get());
            if (file.exists()) {
                String content = FileUtils.read(file);

                JsonDecoder decoder = new JsonDecoder();

                try {
                    Object json = decoder.parse(content);

                    ArrayList<Object> list = (ArrayList<Object>) json;
                    for (Object o : list) {

                        HashMap<String, Object> filter = (HashMap<String, Object>) o;

                        String name = filter.get("name").toString();

                        FilterBookmarkModel filterBookmarkModel = new FilterBookmarkModel();
                        filterBookmarkModel.getName().set(name);

                        ArrayList<Object> values = (ArrayList<Object>) filter.get("values");
                        for (Object valueObject : values) {
                            HashMap<String, Object> valueMap = (HashMap<String, Object>) valueObject;

                            String label = valueMap.get("label").toString();
                            String value = valueMap.get("value").toString();

                            FilterBookmarkValueModel filterBookmarkValueModel = new FilterBookmarkValueModel(label, value);
                            filterBookmarkModel.getValues().add(filterBookmarkValueModel);
                        }

                        filterBookmarks.add(filterBookmarkModel);

                    }

                    logger.info("JSON '{}' decoded to '{}'", content, json);

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        } catch (RuntimeException e) {
            logger.warn(e, "Failed to load saved searches");
        }
    }

    public void saveSearch(String searchName) {

        FilterBookmarkModel bookmarkModel = new FilterBookmarkModel();

        // Scan through the filter models and capture the values
        ObservableList<CustomDateFilterModel> customDateFilters = model.getCustomDateFilters();
        for (CustomDateFilterModel customDateFilter : customDateFilters) {
            logger.trace("Custom date filter : {} = {}", customDateFilter.getLabel().get(), customDateFilter.getValue().get());
            bookmarkModel.addFilter(customDateFilter.getLabel().get(), Long.toString(customDateFilter.getValue().get()));
        }

        ObservableList<CustomQuickFilterModel> customFilters = model.getCustomFilters();
        for (CustomQuickFilterModel customFilter : customFilters) {
            logger.trace("Custom filter : {} = {}", customFilter.getLabel().get(), customFilter.getValue().get());
            bookmarkModel.addFilter(customFilter.getLabel().get(), customFilter.getValue().get());
        }

        ObservableList<QuickFilterModel> quickFilterModels = model.getQuickFilterModels();
        for (QuickFilterModel quickFilterModel : quickFilterModels) {
            logger.trace("Quick filter text : {}", quickFilterModel.getFilterText().get());
            bookmarkModel.addFilter("quickFilterText", quickFilterModel.getFilterText().get());

            if (quickFilterModel.getLevelFilter().get().getSelectedLevel().get() != null) {
                String level = quickFilterModel.getLevelFilter().get().getSelectedLevel().get().toString();
                logger.trace("Quick level : {}", level);
                bookmarkModel.addFilter("quickFilterLevel", level);
            }
        }

        bookmarkModel.getName().set(searchName);
        model.getFilterBookmarks().add(bookmarkModel);

        persistSearches();
    }

    public void persistSearches() {

        ObservableList<FilterBookmarkModel> filterBookmarks = model.getFilterBookmarks();

        String json = JsonEncoder.encode(filterBookmarks);

        File file = new File(model.getConfigurationFolder().get(), model.getSavedSearchesFilename().get());
        logger.debug("Saving saved searches json '{}'", json);
        FileUtils.write(json, file);

    }

    public void selectSearch(String name) {

        FilterBookmarkModel bookmarkByName = getBookmarkByName(name);
        if (bookmarkByName != null) {
            selectSearch(bookmarkByName);
        } else {
            logger.warn("Failed to find bookmark search with name '{}'", name);
        }
    }

    private FilterBookmarkModel getBookmarkByName(String name) {
        FilterBookmarkModel foundModel = null;
        ObservableList<FilterBookmarkModel> filterBookmarks = model.getFilterBookmarks();
        for (FilterBookmarkModel filterBookmark : filterBookmarks) {
            if (filterBookmark.getName().get().equalsIgnoreCase(name)) {
                foundModel = filterBookmark;
                break;
            }
        }

        return foundModel;
    }

    private void selectSearch(FilterBookmarkModel bookmark) {

        model.getActiveBookmark().set(bookmark);
        ObservableList<FilterBookmarkValueModel> values = bookmark.getValues();
        for (FilterBookmarkValueModel bookmarkValueModel : values) {

            String label = bookmarkValueModel.getLabel().get();
            String value = bookmarkValueModel.getValue().get();

            applyFilter(label, value);

        }

    }

    private void applyFilter(String label, String value) {

        if (label.equalsIgnoreCase("quickFilterText")) {
            ObservableList<QuickFilterModel> quickFilterModels = model.getQuickFilterModels();
            QuickFilterModel first = quickFilterModels.get(0);
            first.getFilterText().set(value);
        } else if (label.equalsIgnoreCase("quickFilterLevel")) {
            ObservableList<QuickFilterModel> quickFilterModels = model.getQuickFilterModels();
            QuickFilterModel first = quickFilterModels.get(0);
            first.getLevelFilter().get().getSelectedLevel().set(java.util.logging.Level.parse(value));
        } else {

            // Scan through the filter models and capture the values
            ObservableList<CustomDateFilterModel> customDateFilters = model.getCustomDateFilters();
            for (CustomDateFilterModel customDateFilter : customDateFilters) {
                if (customDateFilter.getLabel().get().equalsIgnoreCase(label)) {
                    if (value.isEmpty() || value.equals("-1")) {
                        customDateFilter.getValue().set(TimeFieldFilter.ACCEPT_ALL);
                    } else {
                        customDateFilter.getValue().set(Long.parseLong(value));
                    }
                }
            }

            ObservableList<CustomQuickFilterModel> customFilters = model.getCustomFilters();
            for (CustomQuickFilterModel customFilter : customFilters) {
                if (customFilter.getLabel().get().equalsIgnoreCase(label)) {
                    customFilter.getValue().set(value);
                }
            }
        }
    }
}
