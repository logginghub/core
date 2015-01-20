package com.logginghub.logging.api.levelsetting;

import com.logginghub.utils.Result;

public interface MultipleResultListener<T> {
    void onResult(Result<T> result);
}
