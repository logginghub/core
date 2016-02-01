package com.logginghub.utils.observable.json;

import com.logginghub.utils.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

/**
 * Created by james on 30/07/15.
 */
public class CharacterWalker {

    public static final char EOF = '\uFFFF';

    private final Reader inputStream;

    private char current;
    private int position;

    public CharacterWalker(Reader inputStream) {
        this.inputStream = inputStream;
        readNext();
    }

    public char readNext() {
        try {

            current = (char) inputStream.read();
            if (current != -1) {
                position++;
            }
            return current;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPosition() {
        return position;
    }

    public boolean isAlphabetic() {
        return Character.isAlphabetic(current);
    }

    public boolean isNumeric() {
        return Character.isDigit(current);
    }

    public String readRemaining() {
        String remaining;

        if (current == EOF) {
            remaining = "";
        } else {
            StringBuilder builder = new StringBuilder();

            builder.append(current);

            while (readNext() != EOF) {
                builder.append(current);
            }
            remaining = builder.toString();
        }

        return remaining;
    }

    public String readToEndOfQuotes(char quoteChar) {

        StringBuilder currentString = new StringBuilder();

        // Move passed the first quote
        readNext();

        boolean escaped = false;

        while (current != EOF && (!is(quoteChar) || escaped)) {
            currentString.append(current);
            readNext();

            if (is('\\')) {
                escaped = true;
                readNext();
            } else {
                escaped = false;
            }
        }

        // Read passed the end quote
        readNext();

        String value = currentString.toString();
        return value;
    }

    public boolean is(char c) {
        return current == c;
    }

    public String readUpTo(Set<Character> endChars) {
        StringBuilder currentString = new StringBuilder();
        currentString.append(current);

        while (!endChars.contains(readNext())) {
            char c = getCurrent();
            currentString.append(c);
        }

        String value = currentString.toString();
        return value;

    }

    public char getCurrent() {
        return current;
    }

    public void skipWhitespace() {
        while (Character.isWhitespace(current)) {
            readNext();
        }
    }

    @Override
    public String toString() {
        return StringUtils.format("{} - '{}'", position, current);
    }
}
