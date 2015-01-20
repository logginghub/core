package com.logginghub.logging.commandline;

import java.awt.event.KeyEvent;

public class ConsoleKeyEvent {

    private int keycode;
    private char key;
    private boolean alt = false;
    private boolean control = false;
    private boolean shift = false;

    public ConsoleKeyEvent(int vkCode, char c, boolean alt, boolean control, boolean shift) {
        this.key = c;
        this.keycode = vkCode;
        this.alt = alt;
        this.control = control;
        this.shift = shift;
    }

    public char getChar() {
        return key;
    }

    public int getKeycode() {
        return keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public boolean isAlt() {
        return alt;
    }

    public void setAlt(boolean alt) {
        this.alt = alt;
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public boolean isShift() {
        return shift;
    }

    public void setShift(boolean shift) {
        this.shift = shift;
    }

    @Override public String toString() {
        return "ConsoleKeyEvent [keycode=" + KeyEvent.getKeyText(keycode) + ", key=" + key + ", alt=" + alt + ", control=" + control + ", shift=" + shift + "]";
    }

    
    
}
