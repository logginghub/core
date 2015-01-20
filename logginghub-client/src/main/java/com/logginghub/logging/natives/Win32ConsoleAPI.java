package com.logginghub.logging.natives;

public class Win32ConsoleAPI
{
    public static final short FOREGROUND_BLACK = 0;
    public static final short FOREGROUND_BLUE = 1;
    public static final short FOREGROUND_GREEN = 2;
    public static final short FOREGROUND_AQUA = 3;
    public static final short FOREGROUND_RED = 4;
    public static final short FOREGROUND_PURPLE = 5;
    public static final short FOREGROUND_YELLOW = 6;
    public static final short FOREGROUND_WHITE = 7;
    public static final short FOREGROUND_GREY = 8;
    public static final short FOREGROUND_LIGHT_BLUE = 9;
    public static final short FOREGROUND_LIGHT_GREEN = 10;
    public static final short FOREGROUND_LIGHT_AQUA = 11;
    public static final short FOREGROUND_LIGHT_RED = 12;
    public static final short FOREGROUND_LIGHT_PURPLE = 13;
    public static final short FOREGROUND_LIGHT_YELLOW = 14;
    public static final short FOREGROUND_LIGHT_WHITE = 15;
    
    public native void cls();
    public native void setCursorPosition(short x, short y);
    public native void keepColors();
    public native void restoreColors();
    public native void setColor(short foreground, short background);
    public native int getch();

    static
    {
        System.loadLibrary("Win32ConsoleAPI");
    }
}