package com.logginghub.logging;

import com.logginghub.logging.api.levelsetting.LevelSettingsRequest;

public interface LevelSettingImplementation {
    boolean process(LevelSettingsRequest request);
}
