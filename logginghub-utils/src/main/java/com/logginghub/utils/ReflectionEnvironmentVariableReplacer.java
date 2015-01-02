package com.logginghub.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logginghub.utils.logging.Logger;

public class ReflectionEnvironmentVariableReplacer {

    private static final Logger logger = Logger.getLoggerFor(ReflectionEnvironmentVariableReplacer.class);

    public static void doReplacements(Object object) throws IllegalArgumentException, IllegalAccessException {
        Set<Object> visitedInstances = new HashSet<Object>();
        doReplacementsInternal(object, visitedInstances);
    }

    private static void doReplacementsInternal(Object object, Set<Object> visitedInstances) throws IllegalArgumentException, IllegalAccessException {

        logger.debug("Replacing environment strings in object '{}' of class '{}'", object, object.getClass().getName());
        if (visitedInstances.contains(object)) {
            // Already done this object
            logger.debug("Skipping this object, we've already visited it");
        }
        else {
            visitedInstances.add(object);

            Class<?> clazz = object.getClass();

            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                Class<?> type = field.getType();
                if (type == String.class) {
                    field.setAccessible(true);
                    String fieldValue = (String) field.get(object);
                    if (fieldValue != null) {
                        String replaced = replace(fieldValue);
                        if (!fieldValue.equals(replaced)) {
                            logger.debug("Replaced string '{}' with '{}' for field '{}' of object class '{}'", fieldValue, replaced, field.getName(), clazz.getName());
                            field.set(object, replaced);
                        }
                    }
                }
                else if (ReflectionUtils.isPrimativeType(type)) {
                    // Dont bother checking these
                }
                else if (type.isAssignableFrom(Collection.class)) {
                    // TODO : process collection items
                }
                else if (type.isAssignableFrom(Map.class)) {
                    // TODO : process map items
                }
                else {
                    field.setAccessible(true);
                    Object fieldValue = field.get(object);
                    if (fieldValue != null) {
                        logger.debug("Recursing into field '{}' class '{}'", field.getName(), type.getName());
                        doReplacements(fieldValue);
                    }
                }
            }
        }

    }

    private static String replace(String text) {

        Properties properties = System.getProperties();
        Map<String, String> envMap = System.getenv();

        String pattern = "\\$\\{([A-Za-z0-9_-]+)\\}";
        Pattern expr = Pattern.compile(pattern);

        Matcher matcher = expr.matcher(text);
        while (matcher.find()) {
            String property = matcher.group(1);

            String value = properties.getProperty(property);
            logger.debug("Looking up property '{}' from system properties environment result was '{}'", property, value);
            if (value == null) {
                value = envMap.get(property);
                logger.debug("Looking up property '{}' from the environment and system properties, result was '{}'", property, value);
            }

            if (value == null) {
                value = "";
            }
            else {
                value = value.replace("\\", "\\\\");
            }
            Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)));
            text = subexpr.matcher(text).replaceAll(value);
        }

        return text;
    }

}
