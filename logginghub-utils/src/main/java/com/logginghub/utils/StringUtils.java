package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String alphaNumericChars = new String("abcdefghijklmnopqrstuvwxyz0123456789");
    private static Random random = new Random();
    public static String newline = System.getProperty("line.separator");

    /**
     * Converts 'foo' to 'Foo'
     *
     * @param string
     * @return
     */
    public static String leadingUppercase(String string) {
        String method = string;

        if (string.startsWith("m_")) {
            method = string.substring(2);
        }

        return Character.toUpperCase(method.charAt(0)) + method.substring(1);
    }

    public static String[] toArray(List<String> command) {
        return CollectionUtils.toArray(command);
    }

    public static String reflectionToString(Object object) {
        StringBuilder builder = new StringBuilder();

        Class<?> c = object.getClass();
        builder.append("[").append(c.getSimpleName()).append("] ");

        Field[] declaredFields = c.getDeclaredFields();
        for (Field field : declaredFields) {
            // Second bit hides the this pointer for inner classes
            if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                Object fieldValue = ReflectionUtils.getFieldValue(field, object);
                builder.append(ReflectionUtils.stripFieldPrefixes(field.getName()));
                builder.append(" [");
                builder.append(fieldValue);
                builder.append("] ");
            }
        }

        return builder.toString();
    }

    public static String reflectionToString(Object object, String... fields) {
        StringBuilder builder = new StringBuilder();

        Class<?> c = object.getClass();
        builder.append("[").append(c.getSimpleName()).append("] ");

        Set<String> fieldsToInclude = ArrayUtils.toSet(fields);

        Field[] declaredFields = c.getDeclaredFields();
        for (Field field : declaredFields) {
            String name = field.getName();
            String stripFieldPrefixes = ReflectionUtils.stripFieldPrefixes(name);

            if (fieldsToInclude.contains(name) || fieldsToInclude.contains(stripFieldPrefixes)) {
                if (!Modifier.isStatic(field.getModifiers()) && !field.getName().startsWith("this$")) {
                    Object fieldValue = ReflectionUtils.getFieldValue(field, object);

                    builder.append(stripFieldPrefixes);
                    builder.append(" [");
                    builder.append(fieldValue);
                    builder.append("] ");
                }
            }
        }

        return builder.toString();
    }

    public static <T> String table(Collection<T> collection, String... fieldsToInclude) {
        List<String> fields;

        if (fieldsToInclude == null || (fieldsToInclude.length == 0 && collection.size() > 0)) {
            fields = ReflectionUtils.getFieldNames(collection.iterator().next().getClass());
        } else {
            fields = ArrayUtils.toList(fieldsToInclude);
        }

        int columns = fields.size();
        int rows = collection.size();

        // Create new lists in an array of columns
        List<String>[] columnValues = new ArrayList[columns];
        for (int i = 0; i < columns; i++) {
            columnValues[i] = new ArrayList<String>();
        }

        // Go through each item and populate the column lists
        for (T t : collection) {
            Field[] declaredFields = t.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                String name = field.getName();
                String stripFieldPrefixes = ReflectionUtils.stripFieldPrefixes(name);

                int index = fields.indexOf(name);
                if (index == -1) {
                    index = fields.indexOf(stripFieldPrefixes);
                }

                if (index != -1) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        Object fieldValue = ReflectionUtils.getFieldValue(field, t);

                        // columnValues[index].add(fieldValue.toString());
                        columnValues[index].add(toString(fieldValue));
                    }
                }
            }
        }

        // Go through each column and work out the maximum width
        int totalWidth = 0;
        int[] columnWidths = new int[columns];
        for (int i = 0; i < columns; i++) {
            List<String> list = columnValues[i];
            int width = 0;
            for (String string : list) {
                width = Math.max(width, string.length());
            }

            // Dont forget to include the column name too
            String columnName = fields.get(i);
            width = Math.max(width, columnName.length());

            columnWidths[i] = width;
            totalWidth += width;
        }

        int totalWidthIncludingFormatting = totalWidth + (columns * 3);

        // Finally render the bloody table
        String newline = "\n";
        StringBuilder builder = new StringBuilder();
        builder.append(paddingString("", totalWidthIncludingFormatting, '-', false)).append(newline);
        for (int i = 0; i < columns; i++) {
            String columnName = fields.get(i);
            int width = columnWidths[i];

            builder.append(" ").append(padRight(columnName, width)).append(" |");
        }

        builder.append(newline);
        builder.append(paddingString("", totalWidthIncludingFormatting, '-', false)).append(newline);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                List<String> list = columnValues[j];
                String value = list.get(i);
                int width = columnWidths[j];

                builder.append(" ").append(padRight(value, width)).append(" |");
            }

            builder.append(newline);
        }
        builder.append(paddingString("", totalWidthIncludingFormatting, '-', false)).append(newline);

        return builder.toString();
    }

    static final String HEXES = "0123456789abcdef";

    public static String toHex(byte[] raw) {
        if (raw == null) {
            return null;
        }
        int index = 0;
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));

            if (index < raw.length - 1) {
                hex.append(" ");
            }

            index++;
        }
        return hex.toString();
    }

    /**
     * Try and provide some nice formatting for this object
     */
    private static String toString(Object fieldValue) {
        String toString;

        if (fieldValue instanceof byte[]) {
            toString = toHex((byte[]) fieldValue);
        } else {
            toString = fieldValue.toString();
        }

        return toString;
    }

    public static String padRight(String s, int n) {
        return paddingString(s, n, ' ', false);
    }

    public static String padLeft(String s, int n) {
        return paddingString(s, n, ' ', true);
    }

    public static String padCenter(String s, int n, char c) {
        if (s == null) {
            return s;
        }

        int add = n - s.length();
        if (add <= 0) {
            return s;
        }

        int addLeft = add / 2;
        int addRight = add - addLeft;

        StringBuffer str = new StringBuffer(s);

        char[] ch = new char[addLeft];
        Arrays.fill(ch, c);
        str.insert(0, ch);

        ch = new char[addRight];
        Arrays.fill(ch, c);
        str.append(ch);

        return str.toString();
    }

    /**
     * Pads a String <code>s</code> to take up <code>n</code> characters, padding with char <code>c</code> on the left (<code>true</code>) or on the
     * right (<code>false</code>). Returns <code>null</code> if passed a <code>null</code> String.
     **/
    public static String paddingString(String s, int n, char c, boolean paddingLeft) {
        if (s == null) {
            return s;
        }
        int add = n - s.length(); // may overflow int size... should not be a
        // problem in real life
        if (add <= 0) {
            return s;
        }
        StringBuffer str = new StringBuffer(s);
        char[] ch = new char[add];
        Arrays.fill(ch, c);
        if (paddingLeft) {
            str.insert(0, ch);
        } else {
            str.append(ch);
        }
        return str.toString();
    }

    public static String classnameToFilename(String classname) {
        String filename = classname.replace('.', File.separatorChar) + ".class";
        return filename;
    }

    public static String filenameToClassname(String filename) {
        String classname = filename.replace(File.separatorChar, '.').substring(0, filename.length() - ".class".length());
        return classname;
    }

    public static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm')
                c += 13;
            else if (c >= 'n' && c <= 'z')
                c -= 13;
            else if (c >= 'A' && c <= 'M')
                c += 13;
            else if (c >= 'A' && c <= 'Z')
                c -= 13;
            sb.append(c);
        }

        return sb.toString();
    }

    public static String formatBytes(double bytes) {
        String formatted;
        NumberFormat instance = NumberFormat.getInstance();

        float kb = 1024;
        float mb = kb * kb;
        float gb = mb * kb;
        float tb = gb * kb;
        float pb = tb * kb;

        if (bytes > pb) {
            formatted = String.format("%s PB", instance.format(bytes / pb));
        } else if (bytes > tb) {
            formatted = String.format("%s TB", instance.format(bytes / tb));
        } else if (bytes > gb) {
            formatted = String.format("%s GB", instance.format(bytes / gb));
        } else if (bytes > mb) {
            formatted = String.format("%s MB", instance.format(bytes / mb));
        } else if (bytes > kb) {
            formatted = String.format("%s KB", instance.format(bytes / kb));
        } else {
            formatted = String.format("%s bytes", instance.format(bytes));
        }

        return formatted;
    }

    public static String before(String string, String sub) {
        int index = string.indexOf(sub);
        if (index == -1) {
            return string;
        } else {
            return string.substring(0, index);
        }
    }

    public static String beforeLast(String string, String sub) {
        int index = string.lastIndexOf(sub);
        if (index == -1) {
            return "";
        } else {
            return string.substring(0, index);
        }
    }

    public static String between(String string, String start, String end) {
        int startIndex = string.indexOf(start);
        int searchFrom = startIndex + start.length();

        String between;
        // Check to see if we've gone off the end of the string
        if (searchFrom == string.length()) {
            // Oops
            between = "";
        } else {
            int endIndex = string.lastIndexOf(end);
            if (endIndex != -1) {
                int startPoint = startIndex + start.length();
                int endPoint = endIndex;

                if (startPoint > endPoint) {
                    // We've kind of overlapped, there cant be anything in between
                    between = null;
                } else {
                    between = string.substring(startIndex + start.length(), endIndex);
                }
            } else {
                between = null;
            }
        }
        return between;
    }

    public static String betweenNonGreedy(String string, String start, String end) {
        int startIndex = string.indexOf(start);
        int searchFrom = startIndex + start.length();
        String between;
        if (searchFrom == string.length()) {
            between = "";
        } else {
            int endIndex = string.indexOf(end, searchFrom);
            if (endIndex != -1) {
                int startPoint = searchFrom;
                if (startPoint > endIndex) {
                    between = null;
                } else {
                    between = string.substring(searchFrom, endIndex);
                }
            } else {
                between = null;
            }
        }

        return between;
    }

    public static String stripQuotes(String string) {
        int length = string.length();
        if (length == 0) {
            return string;
        } else if ((string.charAt(0) == '\'' && string.charAt(length - 1) == '\'') || (string.charAt(0) == '\"' && string.charAt(length - 1) ==
                                                                                                                   '\"')) {
            return string.substring(1, length - 1);
        } else {
            return string;
        }

    }

    public static String afterLast(String string, String partial) {
        int index = string.lastIndexOf(partial);
        if (index == -1) {
            return "";
        }
        return string.substring(index + partial.length(), string.length());
    }

    public static String after(String string, String partial) {
        int index = string.indexOf(partial);
        if (index == -1) {
            return "";
        }
        return string.substring(index + partial.length(), string.length());
    }

    public static void out() {
        System.out.println();
    }

    public static void out(String message, Object... objects) {
        System.out.println(format(message, objects));
    }

    public static void out(char c) {
        System.out.println(c);
    }

    public static void outSameLine(String message, Object... objects) {
        System.out.print(format(message, objects));
        System.out.flush();
    }

    public static void err(String message, Object... objects) {
        System.err.println(format(message, objects));
    }

    public static void errSameLine(String message, Object... objects) {
        System.err.print(format(message, objects));
    }

    public static String format(String message, Object... objects) {
        int objectIndex = 0;

        StringBuilder formatted = new StringBuilder();

        StringBuilder params = new StringBuilder();
        boolean insideCurlyBraces = false;
        boolean isEscaped = false;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);
            if (c == '{') {
                if (isEscaped) {
                    isEscaped = false;
                    formatted.append(c);
                } else {
                    insideCurlyBraces = true;
                }
            } else if (c == '}') {
                if (insideCurlyBraces) {
                    insideCurlyBraces = false;

                    String param = params.toString().trim();

                    boolean isNumeric = false;
                    boolean isDate = false;
                    int width = -1;
                    boolean leftJustified = false;

                    if (param.trim().length() > 0) {

                        if (param.startsWith("-")) {
                            leftJustified = true;
                            param = param.substring(1).trim();
                        }

                        if (param.endsWith("T")) {
                            isDate = true;
                            param = param.substring(0, param.length() - 1).trim();
                        } else if (param.endsWith("N")) {
                            isNumeric = true;
                            param = param.substring(0, param.length() - 1).trim();
                        }

                        if (param.length() > 0) {
                            width = Integer.parseInt(param.trim());
                        }
                    }

                    if (objectIndex < objects.length) {
                        Object object = objects[objectIndex];
                        String objectString = object.toString();

                        if (isDate) {
                            objectString = Logger.toDateString(Long.parseLong(objectString)).toString();
                        } else if (isNumeric) {
                            if (objectString.contains(".")) {
                                objectString = NumberFormat.getInstance().format(Double.parseDouble(objectString));
                            } else {
                                objectString = NumberFormat.getInstance().format(Long.parseLong(objectString));
                            }
                        }

                        if (width != -1) {
                            if (objectString.length() < width) {

                                int padding = width - objectString.length();
                                if (leftJustified) {
                                    formatted.append(objectString);
                                    for (int j = 0; j < padding; j++) {
                                        formatted.append(" ");
                                    }
                                } else {
                                    for (int j = 0; j < padding; j++) {
                                        formatted.append(" ");
                                    }

                                    formatted.append(objectString);
                                }
                            } else {
                                formatted.append(objectString.substring(0, width));
                            }
                        } else {
                            formatted.append(objectString);
                        }

                        objectIndex++;
                    } else {
                        // No object in this position, its just a pair of braces
                        formatted.append("{").append(params).append("}");
                    }

                    params = new StringBuilder();
                } else {
                    // Just a closing curly brace
                    formatted.append(c);
                }
            } else if (c == '%' && i + 1 < message.length() && message.charAt(i + 1) == 'n') {
                formatted.append(newline);
                i++;
            } else if (c == '\\') {
                if(isEscaped) {
                    // Escaped escape
                    formatted.append(c);
                    isEscaped = false;
                }else {
                    isEscaped = true;
                }
            } else {
                if (insideCurlyBraces) {
                    params.append(c);
                } else {

                    if(isEscaped) {
                        // This wasn't a curly brace escape, it must have been a slash
                        formatted.append("\\");
                        isEscaped = false;
                    }

                    formatted.append(c);
                }
            }
        }

        // Edge case for unterminated curly brace
        if (insideCurlyBraces) {
            formatted.append("{").append(params);
        }

        return formatted.toString();

    }

    public static StringUtilsTokeniser tokenise(String attributes) {
        return new StringUtilsTokeniser(attributes);
    }

    public static String repeat(String string, int times) {
        Is.greaterThanOrZero(times, "You can't repeat a string a negative number of times ({})", times);
        StringBuilder builder = new StringBuilder(string.length() * times);
        for (int i = 0; i < times; i++) {
            builder.append(string);
        }
        return builder.toString();

    }

    // From :
    // http://stackoverflow.com/questions/1102891/how-to-check-a-string-is-a-numeric-type-in-java
    public static boolean isStringNumeric(String str) {
        DecimalFormatSymbols currentLocaleSymbols = new DecimalFormatSymbols();
        char localeMinusSign = currentLocaleSymbols.getMinusSign();

        if (!Character.isDigit(str.charAt(0)) && str.charAt(0) != localeMinusSign)
            return false;

        boolean isDecimalSeparatorFound = false;
        char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

        for (char c : str.substring(1).toCharArray()) {
            if (!Character.isDigit(c)) {
                if (c == localeDecimalSeparator && !isDecimalSeparatorFound) {
                    isDecimalSeparatorFound = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    public static class StringUtilsBuilder {

        private StringBuilder builder = new StringBuilder();

        public StringUtilsBuilder add(Object... items) {
            for (Object object : items) {
                builder.append(object);
            }
            return this;
        }

        public StringUtilsBuilder addIfNonZero(int value, Object... items) {
            if (value != 0) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder addIfZero(int value, Object... items) {
            if (value == 0) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder addIfNotNegative(int value, Object... items) {
            if (value >= 0) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder addIfNotNull(Object value, Object... items) {
            if (value != null) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder addNull(Object value, Object... items) {
            if (value == null) {
                add(items);
            }
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

        public StringUtilsBuilder addIfFalse(boolean value, Object... items) {
            if (!value) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder addIfTrue(boolean value, Object... items) {
            if (value) {
                add(items);
            }
            return this;
        }

        public StringUtilsBuilder format(String message, Object... objects) {
            add(StringUtils.format(message, objects));
            return this;
        }

        public StringUtilsBuilder addRandom(int characters) {
            add(StringUtils.randomString(characters));
            return this;
        }

        public StringUtilsBuilder append(String format, Object... params) {
            builder.append(StringUtils.format(format, params));
            return this;
        }

        public StringUtilsBuilder append(Object object) {
            if (object != null) {
                builder.append(object.toString());
            } else {
                builder.append("null");
            }
            return this;
        }

        public StringUtilsBuilder appendLine(Object object) {
            append(object);
            builder.append(newline);
            return this;
        }

        public StringUtilsBuilder appendLine(String format, Object... params) {
            builder.append(StringUtils.format(format, params));
            builder.append(newline);
            return this;
        }

        public StringUtilsBuilder quote(String string) {
            builder.append("\"").append(string).append("\"");
            return this;
        }

        public void toFile(File file) {
            FileUtils.write(toString(), file);
        }

        public StringUtilsBuilder newline() {
            builder.append(newline);
            return this;
        }

        public void appendLine() {
            newline();
        }

        public void reset() {
            this.builder = new StringBuilder();
        }

        public void clear() {
            reset();
        }
    }

    public static StringUtilsBuilder build(Object... items) {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.add(items);
        return builder;
    }

    public static String randomString(int characters) {
        char[] string = new char[characters];
        for (int i = 0; i < string.length; i++) {
            string[i] = alphaNumericChars.charAt(random.nextInt(alphaNumericChars.length()));
        }

        return new String(string);
    }

    public static String randomString(int characters, int seed) {
        Random random = new Random(seed);
        char[] string = new char[characters];
        for (int i = 0; i < string.length; i++) {
            string[i] = alphaNumericChars.charAt(random.nextInt(alphaNumericChars.length()));
        }

        return new String(string);

    }

    public static StringUtilsBuilder builder() {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        return builder;
    }

    /**
     * Parses a size string (12, 34M, 34Gb) etc into the number of bytes
     *
     * @param size
     * @return
     */
    public static long parseSize(String size) {

        String numberString = StringUtils.leadingNumber(size);
        String units = StringUtils.after(size, numberString).trim().toLowerCase();

        double base = Double.parseDouble(numberString);
        if (units.length() > 0) {
            switch (units.charAt(0)) {
                case 'k': {
                    base *= 1024L;
                    break;
                }
                case 'm': {
                    base *= 1024L * 1024L;
                    break;
                }
                case 'g': {
                    base *= 1024L * 1024L * 1024L;
                    break;
                }
                case 't': {
                    base *= 1024L * 1024L * 1024L * 1024L;
                    break;
                }
                case 'p': {
                    base *= 1024L * 1024L * 1024L * 1024L * 1024L;
                    break;
                }
                case 'e': {
                    base *= 1024L * 1024L * 1024L * 1024L * 1024L * 1024L;
                    break;
                }
                case 'z': {
                    base *= 1024L * 1024L * 1024L * 1024L * 1024L * 1024L * 1024L;
                    break;
                }
                case 'y': {
                    base *= 1024L * 1024L * 1024L * 1024L * 1024L * 1024L * 1024L;
                    break;
                }
            }
        }

        return (long) base;

    }

    public static String leadingNumber(String input) {

        DecimalFormatSymbols currentLocaleSymbols = new DecimalFormatSymbols();
        char decimalSeparator = currentLocaleSymbols.getDecimalSeparator();
        char groupingSeparator = currentLocaleSymbols.getGroupingSeparator();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isDigit(c) || c == decimalSeparator || c == groupingSeparator) {
                builder.append(c);
            } else {
                break;
            }
        }
        return builder.toString();
    }

    public static String trailingNumber(String input) {

        DecimalFormatSymbols currentLocaleSymbols = new DecimalFormatSymbols();
        char decimalSeparator = currentLocaleSymbols.getDecimalSeparator();
        char groupingSeparator = currentLocaleSymbols.getGroupingSeparator();

        StringBuilder builder = new StringBuilder();
        for (int i = input.length() - 1; i > 0; i--) {
            char c = input.charAt(i);
            if (Character.isDigit(c) || c == decimalSeparator || c == groupingSeparator) {
                builder.append(c);
            } else {
                break;
            }
        }
        return builder.reverse().toString();
    }

    public static String trailingInteger(String input) {

        StringBuilder builder = new StringBuilder();
        for (int i = input.length() - 1; i > 0; i--) {
            char c = input.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            } else {
                break;
            }
        }
        return builder.reverse().toString();
    }

    public static String trimFromEnd(String string, int i) {
        return string.substring(0, string.length() - i);

    }

    public static String trimFromStart(String string, int i) {
        return string.substring(i, string.length());

    }

    public static String removeFromStart(String string, String substring) {
        if (string.startsWith(substring)) {
            return string.substring(substring.length(), string.length());
        } else {
            return string;
        }
    }

    public static String removeAllFromStart(String string, String substring) {
        String removed = string;
        while (removed.startsWith(substring)) {
            removed = removeFromStart(removed, substring);
        }
        return removed;
    }

    public static List<String> splitIntoWords(String trim) {
        return CollectionUtils.toList(trim.split("\\s+"));
    }

    public static String[] splitIntoLines(String contents) {
        return contents.split("\\r?\\n");
    }

    public static List<String> splitIntoLineList(String string) {
        String[] splitIntoLines = splitIntoLines(string);
        List<String> list = new ArrayList<String>();
        for (String item : splitIntoLines) {
            list.add(item);
        }

        return list;
    }

    public static String replace(String string, int startIndex, int endIndex, String newContent) {
        StringBuilder builder = new StringBuilder();
        builder.append(string.substring(0, startIndex));
        builder.append(newContent);
        builder.append(string.substring(endIndex, string.length()));
        String replaced = builder.toString();
        return replaced;
    }

    public static String capitalise(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static String[] readAsStringArray(InputStream is) {
        return FileUtils.readAsStringArray(is);
    }

    public static String readAsString(InputStream is) {
        return FileUtils.readAsString(is);

    }

    public static String appendLines(List<String> splitIntoLineList, int start, int end) {
        StringBuilder builder = new StringBuilder();
        String split = "";
        for (int i = start; i < end; i++) {
            builder.append(splitIntoLineList.get(i));
            builder.append(split);
            split = StringUtils.newline;
        }
        return builder.toString();

    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    public static List<Pair<String, String>> splitIntoKeyValuePairs(String line) {
        List<Pair<String, String>> pairs = new ArrayList<Pair<String, String>>();
        List<String> splitIntoWords = splitIntoWords(line);
        for (String string : splitIntoWords) {
            if (string.contains("=")) {
                String key = before(string, "=");
                String value = after(string, "=");
                Pair<String, String> pair = new Pair<String, String>(key, value);
                pairs.add(pair);
            }
        }
        return pairs;

    }

    public static Number parseNumeric(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException nfe2) {
                try {
                    return NumberFormat.getInstance().parse(value);
                } catch (ParseException e) {
                    return null;
                }
            }
        }
    }

    public static String format(int value) {
        return NumberFormat.getInstance().format(value);

    }

    /**
     * Take a number of chars from the start and end and return the middle string
     *
     * @param string
     * @param i
     * @return
     */
    public static String pinch(String string, int chars) {
        return string.substring(chars, string.length() - chars);
    }

    public static Pair<String, String> splitAroundLast(String string, String substring) {
        int index = string.lastIndexOf(substring);
        if (index == -1) {
            return null;
        } else {
            return new Pair<String, String>(string.substring(0, index), string.substring(index + 1, string.length()));
        }
    }

    public static boolean isNumeric(String value) {
        boolean isNumeric;
        try {
            Double.parseDouble(value);
            isNumeric = true;
        } catch (NumberFormatException e) {
            isNumeric = false;
        }

        return isNumeric;
    }

    public static String unquote(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return pinch(value, 1);
        } else if (value.startsWith("'") && value.endsWith("'")) {
            return pinch(value, 1);
        } else {
            return value;
        }

    }

    public static String templateFromResource(String resourcePath, Metadata variables) {

        String template = ResourceUtils.read(resourcePath);

        Set<Entry<Object, Object>> entrySet = variables.entrySet();
        for (Entry<Object, Object> entry : entrySet) {
            String key = "${" + entry.getKey().toString() + "}";
            String replacement = entry.getValue().toString();

            // TODO : this is really slow, should work out a way to do it in one pass, like the
            // string utils formatter
            template = template.replace(key, replacement);
        }

        return template;
    }

    public static String templateFromResource(String resourcePath) {
        return ResourceUtils.read(resourcePath);
    }

    public static List<String> toList(String line) {
        StringUtilsTokeniser tokeniser = new StringUtilsTokeniser(line);
        return tokeniser.toList();
    }

    public static String format2dp(double value) {
        NumberFormat instance = NumberFormat.getInstance();
        instance.setMaximumFractionDigits(2);
        instance.setMinimumFractionDigits(2);
        return instance.format(value);
    }

    public static String format0dp(double value) {
        NumberFormat instance = NumberFormat.getInstance();
        instance.setMaximumFractionDigits(0);
        instance.setMinimumFractionDigits(0);
        return instance.format(value);
    }


    public static String environmentReplacement(String sourceApplication) {

        int index = 0;
        int lastIndex = index;

        boolean done = false;

        StringBuilder replacementLine = new StringBuilder();

        String startString = "${";
        String endString = "}";
        while (!done) {
            index = sourceApplication.indexOf(startString, index);
            if (index == -1) {
                done = true;
            } else {
                int endIndex = sourceApplication.indexOf(endString, index);
                String variable = sourceApplication.substring(index + startString.length(), endIndex);

                String replacement = EnvironmentProperties.getString(variable);

                replacementLine.append(sourceApplication.substring(lastIndex, index));
                if (replacement == null) {
                    replacementLine.append("${").append(variable).append("}");
                } else {
                    replacementLine.append(replacement);
                }
                index = endIndex + 1;
                lastIndex = endIndex + 1;
            }
        }

        if (lastIndex < sourceApplication.length()) {
            replacementLine.append(sourceApplication.substring(lastIndex, sourceApplication.length()));
        }

        return replacementLine.toString();
    }

    public static int countOccurances(String toSearch, String toFind) {
        int count = 0;

        int index = 0;
        while ((index = toSearch.indexOf(toFind, index)) != -1) {
            count++;
            index += toFind.length();
        }

        return count;
    }

    public static boolean isNotNullOrEmpty(String string) {
        return string != null && string.length() > 0;
    }

    public static int countLeading(String string, char c) {
        int count = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == c) {
                count++;
            } else {
                break;
            }
        }

        return count;
    }

    public static String camelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String patternReplace(String source, String search, String replace) {

        StringBuffer sb = new StringBuffer();
        Pattern p = Pattern.compile(search);
        Matcher m = p.matcher(source);

        while (m.find()) {
            m.appendReplacement(sb, replace);
        }
        m.appendTail(sb);

        return sb.toString();

    }

    public static List<String> matchGroups(String input, String regexFormat, Object... params) {
        String regex = StringUtils.format(regexFormat, params);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        List<String> groups = null;
        if (matcher.matches()) {
            groups = new ArrayList<String>();
            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount; i++) {
                String group = matcher.group(i + 1);
                groups.add(group);
            }
        }

        return groups;
    }

    public static String[] matchGroupsArray(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        String[] groups = null;
        if (matcher.matches()) {
            groups = new String[matcher.groupCount()];
            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount; i++) {
                String group = matcher.group(i + 1);
                groups[i] = group;
            }
        }

        return groups;
    }


    public static boolean matches(String input, String regexFormat, Object... params) {
        String regex = StringUtils.format(regexFormat, params);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static String first(String string, int chars) {
        return string.substring(0, Math.min(chars, string.length()));
    }

    public static String firstLine(String string) {
        return string.split("\\r\\n|\\n")[0];
    }

    public static String toHTML(String string) {

        if (string != null) {
            string = string.replace("&", "&amp;");
            string = string.replace("\"", "&quot;");
            string = string.replace("'", "&apos;");
            string = string.replace("<", "&lt;");
            string = string.replace(">", "&gt;");
        }

        return string;
    }

    public static String toXML(String string) {

        if (string != null) {
            string = string.replace("&", "&amp;");
            string = string.replace("\"", "&quot;");
            string = string.replace("'", "&apos;");
            string = string.replace("<", "&lt;");
            string = string.replace(">", "&gt;");
        }

        return string;
    }

    public static String fromXML(String string) {
        if (string != null) {
            string = string.replace("&quot;", "\"");
            string = string.replace("&apos;", "'");
            string = string.replace("&lt;", "<");
            string = string.replace("&gt;", ">");
            string = string.replace("&amp;", "&");
        }
        return string;
    }

    public static String toJSON(String string) {
        if (string != null) {
            string = string.replace("\"", "\\\"");
        }
        return string;

    }

    public static String reverse(String content) {
        return new StringBuilder(content).reverse().toString();
    }

    public static String removeLast(String pattern, int i) {
        int min = Math.min(i, pattern.length());
        return pattern.substring(0, pattern.length() - min);
    }


}
