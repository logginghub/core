package com.logginghub.logging.transaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME) public @interface DestinationType {
    Class<?> type();
}
