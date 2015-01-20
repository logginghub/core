package com.logginghub.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME) public @interface WebController {
    String staticFiles();
    String defaultUrl() default "index.html"; 
}
