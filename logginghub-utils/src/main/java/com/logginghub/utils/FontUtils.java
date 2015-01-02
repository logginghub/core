package com.logginghub.utils;

import java.awt.Font;

public class FontUtils {

    public static Font parseFont(String fontString) {
        return Font.decode(fontString);
    }

}
