package com.logginghub.utils;

import java.util.Iterator;

public class CharacterIterator implements Iterator<Character> {

    private final String str;
    private int pos = 0;

    public CharacterIterator(String str) {
        this.str = str;
    }

    public boolean hasNext() {
        return pos < str.length();
    }

    public Character next() {
        return str.charAt(pos++);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean startsWith(String string) {
        String activeSubstring = str.substring(pos);
        boolean startsWith = activeSubstring.startsWith(string);
        return startsWith;         
    }
}