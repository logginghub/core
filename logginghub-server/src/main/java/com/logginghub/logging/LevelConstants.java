package com.logginghub.logging;


/**
 * You can't use the Juli level.intValue in switch statements, so this class should make that easier.
 * @author James
 *
 */
public class LevelConstants {

    public static final int OFF = Integer.MAX_VALUE;
    public static final int SEVERE = 1000;
    public static final int WARNING = 900;
    public static final int INFO = 800;
    public static final int CONFIG = 700;
    public static final int FINE = 500;
    public static final int FINER = 400;
    public static final int FINEST = 300;
    public static final int ALL = Integer.MIN_VALUE;
}
