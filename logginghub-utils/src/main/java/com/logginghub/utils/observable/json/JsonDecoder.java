package com.logginghub.utils.observable.json;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by james on 30/07/15.
 */
public class JsonDecoder {

    private static Set<Character> endCharsIncludingSpace = new HashSet<Character>();
    private static Set<Character> endCharsNotIncludingSpace = new HashSet<Character>();

    private static final Logger logger = Logger.getLoggerFor(JsonDecoder.class);

    static {
        endCharsIncludingSpace.add(',');
        endCharsIncludingSpace.add('}');
        endCharsIncludingSpace.add(']');
        endCharsIncludingSpace.add(' ');
        endCharsIncludingSpace.add('\n');
        endCharsIncludingSpace.add('\r');
        endCharsIncludingSpace.add(CharacterWalker.EOF);

        endCharsNotIncludingSpace.add(',');
        endCharsNotIncludingSpace.add('}');
        endCharsNotIncludingSpace.add(']');
        endCharsNotIncludingSpace.add('\n');
        endCharsNotIncludingSpace.add('\r');
        endCharsNotIncludingSpace.add(CharacterWalker.EOF);
    }

    private String input;

    public Object parse(String body) throws Throwable {
        try {
            this.input = body;
            return parse(new StringReader(body));
        } catch (Throwable t) {
            logger.warn(t, "Failed to parse : '{}'", body);
            throw t;
        }
    }

    private Object parse(Reader inputStream) {

        CharacterWalker characterWalker = new CharacterWalker(inputStream);
        characterWalker.skipWhitespace();

        Object element = readElement(characterWalker);
        return element;
    }

    private Object readElement(CharacterWalker characterWalker) {
        Object element;

        if (characterWalker.isNumeric() || characterWalker.is('+') || characterWalker.is('-')) {
            Object value;

            boolean hasExponent = false;
            boolean hasDecimalPlace = false;

            StringBuilder currentString = new StringBuilder();
            currentString.append(characterWalker.getCurrent());

            while (!endCharsNotIncludingSpace.contains(characterWalker.readNext())) {
                char c = characterWalker.getCurrent();

                if (c == 'E' || c == 'e') {
                    hasExponent = true;
                }

                if (c == '.') {
                    hasDecimalPlace = true;
                }

                currentString.append(c);
            }

            String parsed = currentString.toString();

            try {
                if (hasExponent || hasDecimalPlace) {
                    // TODO : float, double and big decimal representations?
                    value = Double.parseDouble(parsed);
                } else {
                    // TODO : this is pretty slow and nasty, we can probably short cut this for various lengths?
                    String trimmed = parsed.trim();
                    try {
                        value = Integer.parseInt(trimmed);
                    } catch (NumberFormatException e) {
                        try {
                            value = Long.parseLong(trimmed);
                        } catch (NumberFormatException e2) {
                            BigInteger bigIntegerValue = new BigInteger(trimmed);
                            value = bigIntegerValue;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Must be a string, maybe an unquoted string with a number at the start?
                value = parsed;
            }

            element = value;
        } else if (characterWalker.is('"')) {
            String value = characterWalker.readToEndOfQuotes('"');
            element = value;
        } else if (characterWalker.is('\'')) {
            String value = characterWalker.readToEndOfQuotes('\'');
            element = value;
        } else if (characterWalker.is('{')) {
            Map<String, Object> object = new HashMap<String, Object>();
            readObject(characterWalker, object);
            element = object;
        } else if (characterWalker.is('[')) {
            List<Object> array = new ArrayList<Object>();
            readArray(characterWalker, array);
            element = array;
        } else if (characterWalker.is('n') || characterWalker.is('N')) {
            String mightBeNull = characterWalker.readUpTo(endCharsIncludingSpace);
            if (mightBeNull.equalsIgnoreCase("null")) {
                element = null;
            } else {
                // Must be an unquoted string then?
                element = mightBeNull + characterWalker.readRemaining();
            }
        } else if (characterWalker.is('t') || characterWalker.is('T')) {
            String mightBeTrue = characterWalker.readUpTo(endCharsIncludingSpace);
            if (mightBeTrue.equalsIgnoreCase("true")) {
                element = true;
            } else {
                // Must be an unquoted string then?
                element = mightBeTrue + characterWalker.readRemaining();
            }
        } else if (characterWalker.is('f') || characterWalker.is('F')) {
            String mightBeFalse = characterWalker.readUpTo(endCharsIncludingSpace);
            if (mightBeFalse.equalsIgnoreCase("false")) {
                element = false;
            } else {
                // Must be an unquoted string then?
                element = mightBeFalse + characterWalker.readRemaining();
            }
        } else {
            // Not sure what the hell this is, treat it as a string literal
            element = characterWalker.readRemaining();
        }


        return element;
    }

    private IllegalArgumentException throwFormattedException(CharacterWalker characterWalker, String message) {

        int position = characterWalker.getPosition();

        int before = 100;
        int after = 100;

        int start = position - before;
        int end = position + after;

        if (start < 0) {
            start = 0;
            before = position;
        }

        if (end > input.length()) {
            end = input.length();
            after = input.length() - position;
            if(after < 0) {
                // We've read passed the end of the input
                after = 0;
            }
        }

        String substring = input.substring(start, end);

        StringBuilder pointer = new StringBuilder();
        pointer.append(StringUtils.repeat("-", before - 1));
        pointer.append("^");
        pointer.append(StringUtils.repeat("-", after));

        String fullMessage = StringUtils.format("Parsing failed at position {} - {}:\r\n{}\r\n{}\r\n", position, message, substring, pointer);

        return new IllegalArgumentException(fullMessage);
    }

    private void readArray(CharacterWalker characterWalker, List<Object> array) {

        characterWalker.readNext();
        characterWalker.skipWhitespace();

        while (!characterWalker.is(']')) {

            Object element = readElement(characterWalker);
            array.add(element);
            characterWalker.skipWhitespace();

            if (characterWalker.is(',')) {
                characterWalker.readNext();
                characterWalker.skipWhitespace();
            }
        }

        characterWalker.readNext();
    }

    private void readObject(CharacterWalker characterWalker, Map<String, Object> object) {

        characterWalker.readNext();
        characterWalker.skipWhitespace();

        while (!characterWalker.is('}')) {

            String key = readKey(characterWalker);
            characterWalker.skipWhitespace();
            if (!characterWalker.is(':')) {
                throw new IllegalArgumentException("Parsing failed at position " + characterWalker.getPosition() + " - was expecting a :");
            }
            characterWalker.readNext();
            characterWalker.skipWhitespace();
            Object element = readElement(characterWalker);
            object.put(key, element);
            characterWalker.skipWhitespace();

            // jshaw - useful debugging
            logger.trace("{} = {}", key, element);

            if (characterWalker.is(',')) {
                characterWalker.readNext();
                characterWalker.skipWhitespace();
            }
        }

        characterWalker.readNext();
    }

    private String readKey(CharacterWalker characterWalker) {
        characterWalker.skipWhitespace();

        String key;
        if (characterWalker.is('"')) {
            key = characterWalker.readToEndOfQuotes('"');
        } else if (characterWalker.is('\'')) {
            key = characterWalker.readToEndOfQuotes('\'');
        } else {
            throw throwFormattedException(characterWalker, "was expecting a quoted key");
        }

        return key;

    }


}
