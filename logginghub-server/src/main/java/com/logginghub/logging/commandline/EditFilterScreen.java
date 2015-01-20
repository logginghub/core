package com.logginghub.logging.commandline;

import com.logginghub.logging.commandline.editscreen.EditorGetter;
import com.logginghub.logging.commandline.editscreen.EditorScreen;
import com.logginghub.logging.commandline.editscreen.EditorSetter;

public class EditFilterScreen extends EditorScreen<FilterValues> {

    private ScreenController screenController;

    public EditFilterScreen(CommandLineController commandLineController, ScreenController screenController) {

        this.screenController = screenController;
        
        addEditableItem("Level", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getLevel();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setLevel(newValue);
            }
        });

        addEditableItem("Message", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getMessage();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setMessage(newValue);
            }
        });

        addEditableItem("Source application", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getSourceApplication();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setSourceApplication(newValue);
            }
        });

        addEditableItem("Source host", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getSourceHost();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setSourceHost(newValue);
            }
        });

        addEditableItem("Source address", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getSourceAddress();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setSourceAddress(newValue);
            }
        });

        addDivider();

        addEditableItem("Earliest time", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getStartTime();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setStartTime(newValue);
            }
        });

        addEditableItem("Latest time", new EditorGetter<FilterValues>() {
            public String get(FilterValues value) {
                return value.getEndTime();
            }
        }, new EditorSetter<FilterValues>() {
            public void set(String newValue, FilterValues value) {
                value.setEndTime(newValue);
            }
        });

        setTitle("Edit filter details");
        setItem(commandLineController.getFilterValues());
    }

    @Override protected void onEscape() {
        screenController.back();
    }

    @Override protected void onFinished(FilterValues item) {
        screenController.back();
    }

}
