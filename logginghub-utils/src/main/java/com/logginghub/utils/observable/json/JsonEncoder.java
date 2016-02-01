package com.logginghub.utils.observable.json;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableItem;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JsonEncoder {

    private final static String NEWLINE = String.format("%n");

    public static String encode(Object object) {
        Metadata configuration = new Metadata();
        return encode(object, configuration);
    }

    public static String encode(Object object, Metadata configuration) {
        StringBuilder builder = new StringBuilder();
        encodeObject(object, builder, configuration);

        String json = builder.toString();
        return json;
    }

    public static void encodeBasicObject(Object object, StringBuilder builder, Metadata configuration) {
        builder.append("{");
        if (configuration.getBoolean("prettyPrint", false)) {
            builder.append(NEWLINE);
        }

        String div = "";

        List<Field> fields = ReflectionUtils.getAllFields(object.getClass());
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                String fieldName = field.getName();
                Object value = ReflectionUtils.getFieldValue(field, object);

                if (value != null) {
                    builder.append(div).append("\"").append(fieldName).append("\":");
                    encodeObject(value, builder, configuration);
                    div = ",";
                    if (configuration.getBoolean("prettyPrint", false)) {
                        div = "," + NEWLINE;
                    }
                } else if (configuration.getBoolean("encodeNulls", false)) {
                    builder.append(div).append("\"").append(fieldName).append("\":null");
                    div = ",";

                    if (configuration.getBoolean("prettyPrint", false)) {
                        div = "," + NEWLINE;
                    }
                }

            }
        }

        if (configuration.getBoolean("prettyPrint", false)) {
            builder.append(NEWLINE);
        }

        builder.append("}");

        if (configuration.getBoolean("prettyPrint", false)) {
            builder.append(NEWLINE);
        }
    }

    private static void encodeCollection(Collection collection, StringBuilder builder, Metadata configuration) {
        builder.append("[");

        String div = "";

        for (Object o : collection) {
            builder.append(div);
            encodeObject(o, builder, configuration);
            div = ",";
        }

        builder.append("]");
    }

    private static void encodeMap(Map<String, Object> map, StringBuilder builder, Metadata configuration) {
        Set<Entry<String, Object>> entries = map.entrySet();

        builder.append("{");
        String div = "";

        for (Map.Entry<String, Object> entry : entries) {

            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value != null) {
                builder.append(div).append("\"").append(fieldName).append("\":");
                encodeObject(value, builder, configuration);
                div = ",";
            } else if (configuration.getBoolean("encodeNulls", false)) {
                builder.append(div).append("\"").append(fieldName).append("\":null");
                div = ",";
            }

        }

        builder.append("}");
    }

    //    private static void encodeJsonObject(JsonObject object, StringBuilder builder, Metadata configuration) {
    //        builder.append("{");
    //
    //        String div = "";
    //
    //        Set<Entry<String, JsonElement>> entries = object.entrySet();
    //        for (Map.Entry<String, JsonElement> entry : entries) {
    //
    //            String fieldName = entry.getKey();
    //            JsonElement value = entry.getValue();
    //
    //            if (value != null) {
    //                builder.append(div).append("\"").append(fieldName).append("\":");
    //                encodeObject(value, builder, configuration);
    //                div = ",";
    //            } else if (configuration.getBoolean("encodeNulls", false)) {
    //                builder.append(div).append("\"").append(fieldName).append("\":null");
    //                div = ",";
    //            }
    //        }
    //
    //
    //        builder.append("}");
    //    }

    public static void encodeObject(Object object, StringBuilder builder, Metadata configuration) {

        // TODO : finish off the other base types
        if (object instanceof ObservableList) {
            encodeObservableList((ObservableList) object, builder, configuration);
        } else if (object instanceof Observable) {
            encodeObservable((Observable) object, builder, configuration);
        }
        else if (object instanceof ObservableInteger) {
            builder.append(((ObservableInteger) object).intValue());
        }
        else if (object instanceof ObservableLong) {
            builder.append(((ObservableLong) object).longValue());
        }
        else if (object instanceof ObservableProperty) {
            encodeString(object.toString(), builder, configuration);
        }else {
            throw new FormattedRuntimeException("Unsupported type '{}'", object.getClass());
        }

    }

    private static void encodeObservable(Observable object, StringBuilder builder, Metadata configuration) {

        String div = "";
        builder.append("{");
        List<ObservableItem> childProperties = object.getChildProperties();
        for (ObservableItem childProperty : childProperties) {

            String name;

            if (childProperty instanceof ObservableProperty) {
                ObservableProperty property = (ObservableProperty) childProperty;
                name = property.getName();
            } else if (childProperty instanceof ObservableList) {
                ObservableList property = (ObservableList) childProperty;
                name = property.getName();
            } else {
                throw new FormattedRuntimeException("Unsupported type '{}'", childProperty.getClass().getName());
            }

            builder.append(div).append("\"").append(name).append("\":");

            encodeObject(childProperty, builder, configuration);

            div = ",";
        }

        builder.append("}");
    }

    private static void encodeObservableList(ObservableList object, StringBuilder builder, Metadata configuration) {
        builder.append("[");

        String div = "";

        for (Object entry : object) {
            builder.append(div);
            encodeObject(entry, builder, configuration);
            div = ",";
        }

        builder.append("]");
    }

    private static void encodeString(String object, StringBuilder builder, Metadata configuration) {
        String escaped = escape(object);
        builder.append(escaped);
    }

    public static String escape(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
