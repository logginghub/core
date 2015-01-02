package com.logginghub.utils.persistence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtilsTokeniser;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;

public class TextPersistence {

    private String keyValueSepartor = ":";
    private boolean quotedKeys = true;

    public void setKeyValueSepartor(String keyValueSepartor) {
        this.keyValueSepartor = keyValueSepartor;
    }

    public String getKeyValueSepartor() {
        return keyValueSepartor;
    }

    public void setQuotedKeys(boolean quotedKeys) {
        this.quotedKeys = quotedKeys;
    }

    public boolean isQuotedKeys() {
        return quotedKeys;
    }

    public Bag fromString(String string) {
        Bag bag = new Bag();

        if (string.startsWith("[")) {
            Object decodeArray = decodeArray(string);
            bag.setValue(decodeArray);
        }
        else if (string.startsWith("{")) {
            decodeMap(string, bag);
        }
        else {
            String unquote = unquote(string);

            Object converted = convertToType(unquote);
            bag.setValue(converted);
        }

        return bag;
    }

    private void decodeMap(String string, Bag bag) {
        String mapContents = StringUtils.pinch(string, 1);

        StringUtilsTokeniser st = new StringUtilsTokeniser(mapContents);
        while (st.hasMore()) {

            if (st.peekChar() == ',') {
                st.skip();
            }

            String key = st.nextQuotedWordWithQuotesRemoved(':');            
            st.skip();

            char next = st.peekChar();

            if (next == '\"') {
                String value = st.nextQuotedWordWithQuotesRemoved(',');
                Object converted = convertToType(value);
                bag.put(key, converted);
            }
            else if (next == '{') {
                throw new FormattedRuntimeException("Nested maps aren't supported yet");
            }
            else if (next == '[') {
                String array = st.upToAndIncluding("]");
                Object decoded = decodeArray(array);
                bag.put(key, decoded);
            }
            else {
                // Must be numeric value
                String value = st.nextQuotedWordWithQuotesRemoved(',');
                bag.put(key, value);
            }

        }
    }

    private Object decodeArray(String string) {
        Object decoded;

        String arrayContents = StringUtils.pinch(string, 1);

        Set<Class<?>> types = new HashSet<Class<?>>();
        List<Object> elements = new ArrayList<Object>();
        StringUtilsTokeniser st = new StringUtilsTokeniser(arrayContents);

        boolean stringValues;

        while (st.hasMore()) {
            char peekChar = st.peekChar();
            if (peekChar == '\"') {
                stringValues = true;
            }
            else {
                stringValues = false;
            }

            String element = st.nextQuotedWord(',');
            st.skip();

            Object object;
            if (stringValues) {
                object = StringUtils.pinch(element, 1);
            }
            else {
                object = convertToType(element);
            }

            elements.add(object);
            types.add(object.getClass());
        }

        if (types.size() == 1) {

            Class<?> type = types.iterator().next();
            if (type == Integer.TYPE || type == Integer.class) {
                int[] array = new int[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (Integer) elements.get(i);
                }
                decoded = array;
            }
            else if (type == Double.TYPE || type == Double.class) {
                double[] array = new double[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (Double) elements.get(i);
                }
                decoded = array;
            }
            else if (type == Float.TYPE || type == Float.class) {
                float[] array = new float[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (Float) elements.get(i);
                }
                decoded = array;
            }
            else if (type == Short.TYPE || type == Short.class) {
                short[] array = new short[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (Short) elements.get(i);
                }
                decoded = array;
            }
            else if (type == Boolean.TYPE || type == Boolean.class) {
                boolean[] array = new boolean[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (Boolean) elements.get(i);
                }
                decoded = array;
            }
            else if (type == String.class) {
                String[] array = new String[elements.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = (String) elements.get(i);
                }

                decoded = array;
            }
            else {
                throw new FormattedRuntimeException("Unsupported array type");
            }

        }
        else {
            throw new FormattedRuntimeException("Failed to convert the array because we couldn't identify a common type");
        }

        return decoded;
    }

    private Object convertToType(String string) {

        Object converted = string;

        try {
            int parseInt = Integer.parseInt(string);
            converted = parseInt;
        }
        catch (NumberFormatException e) {

            try {
                double parseDouble = Double.parseDouble(string);
                converted = parseDouble;
            }
            catch (NumberFormatException e2) {

            }

        }
        return converted;
    }

    private String unquote(String string) {
        return StringUtils.between(string, "\"", "\"");
    }

    public String toString(Bag bag) {
        StringUtilsBuilder builder = StringUtils.builder();

        if (bag.hasValue()) {

            Object value = bag.getValue();
            format(builder, value);
        }
        else {
            builder.append("{");
            List<String> keySet = bag.getSortedKeyList();
            String div = "";
            for (String string : keySet) {
                builder.append(div);
                appendKey(builder, string);
                builder.append(keyValueSepartor);
                format(builder, bag.get(string));
                div = ",";
            }

            builder.append("}");
        }
        return builder.toString();
    }

    private void format(StringUtilsBuilder builder, Object value) {

        if (value instanceof String[]) {
            String[] strings = (String[]) value;

            String div = "";
            builder.append("[");
            for (String string : strings) {
                builder.append(div).quote(string);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof byte[]) {
            byte[] bs = (byte[]) value;
            String div = "";
            builder.append("[");
            for (byte byteValue : bs) {
                builder.append(div).append(byteValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof int[]) {
            int[] bs = (int[]) value;
            String div = "";
            builder.append("[");
            for (int intValue : bs) {
                builder.append(div).append(intValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof short[]) {
            short[] bs = (short[]) value;
            String div = "";
            builder.append("[");
            for (short intValue : bs) {
                builder.append(div).append(intValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof double[]) {
            double[] bs = (double[]) value;
            String div = "";
            builder.append("[");
            for (double intValue : bs) {
                builder.append(div).append(intValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof float[]) {
            float[] bs = (float[]) value;
            String div = "";
            builder.append("[");
            for (float intValue : bs) {
                builder.append(div).append(intValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof boolean[]) {
            boolean[] bs = (boolean[]) value;
            String div = "";
            builder.append("[");
            for (boolean intValue : bs) {
                builder.append(div).append(intValue);
                div = ",";
            }
            builder.append("]");
        }
        else if (value.getClass().isArray()) {
            Object[] objects = (Object[]) value;

            String div = "";
            builder.append("[");
            for (Object object : objects) {
                if (object instanceof Number) {
                    builder.append(div).quote(object.toString());
                }
                else {
                    builder.append(div).quote(object.toString());
                }
                div = ",";
            }
            builder.append("]");
        }
        else if (value instanceof ObservableLong) {
            builder.append(((ObservableLong) value).longValue());
        }
        else if (value instanceof ObservableInteger) {
            builder.append(((ObservableInteger) value).intValue());
        }
        else if (value instanceof ObservableDouble) {
            builder.append(((ObservableDouble) value).doubleValue());
        }
        else {
            builder.quote(value.toString());
        }

    }

    private void appendKey(StringUtilsBuilder builder, String string) {
        if (quotedKeys) {
            builder.append("\"").append(string).append("\"");
        }
        else {
            builder.append(string);
        }
    }

    public static String toString(Object object) {
        ReflectionBinder binder = new ReflectionBinder();
        try {
            Bag bag = binder.fromObject(object);
            TextPersistence persistence = new TextPersistence();
            return persistence.toString(bag);
        }
        catch (Exception e) {
            throw new FormattedRuntimeException(e, "Failed to convert object '{}'", object);
        }
    }

    public static <T> T fromString(String string, Class<T> clazz) {
        TextPersistence persistence = new TextPersistence();
        Bag fromString = persistence.fromString(string);
        ReflectionBinder binder = new ReflectionBinder();
        try {
            return binder.toObject(fromString, clazz);
        }
        catch (Exception e) {
            throw new FormattedRuntimeException(e, "Failed to convert object '{}'", string);
        }
    }

}
