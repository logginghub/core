package com.logginghub.utils.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) public @interface Inject {
    enum Direction {
        Parent,
        SiblingsThenParent,        
    }
    
    Direction direction() default Direction.SiblingsThenParent;
}
