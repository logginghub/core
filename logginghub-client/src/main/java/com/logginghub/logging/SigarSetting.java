package com.logginghub.logging;

public class SigarSetting {
    private static boolean noSigar = Boolean.getBoolean("com.logginghub.logging.nosigar");
    public static boolean noSigar() {
        return noSigar;
    }
}
