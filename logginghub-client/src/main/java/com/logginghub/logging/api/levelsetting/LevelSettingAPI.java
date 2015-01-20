package com.logginghub.logging.api.levelsetting;


public interface LevelSettingAPI {
    void setLevels(InstanceFilter filter, LevelSettingsGroup settings, MultipleResultListener<LevelSettingsConfirmation> listener);
}
