package com.logginghub.utils;

import java.text.SimpleDateFormat;

public class StandardDateFormat extends SimpleDateFormat {
    private static final long serialVersionUID = 1L;
    public StandardDateFormat() {
        super("HH:mm:ss dd/MM/yyyy");
    }
}
