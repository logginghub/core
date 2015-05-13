package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofReader;
import com.logginghub.utils.sof.SofWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Convience class that adds some type specific accessors over a map to provide a basic metadata
 * bucket.
 *
 * @author James
 */
public class Metadata extends HashMap<Object, Object> implements SerialisableObject {

    private static final Logger logger = Logger.getLoggerFor(Metadata.class);
    private static final long serialVersionUID = 1L;

    public String getString(Object key) {
        String value;
        Object object = this.get(key);
        if (object == null) {
            value = null;
        } else {
            if (object instanceof String) {
                value = (String) object;
            } else {
                value = object.toString();
            }
        }
        return value;
    }

    public String getString(Object key, String valueIfMissing) {
        String value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof String) {
                value = (String) object;
            } else {
                value = object.toString();
            }
        }
        return value;
    }

    public int getInt(Object key) {
        int value;
        Object object = this.get(key);
        if (object == null) {
            throw new RuntimeException(String.format(
                    "You tried to get the value for key '%s' out of this metadata as an int, but nothing was stored against that key",
                    key));
        } else {
            if (object instanceof Integer) {
                value = (Integer) object;
            } else {
                try {
                    value = Integer.parseInt(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as an int, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public long getLong(Object key) {
        long value;
        Object object = this.get(key);
        if (object == null) {
            throw new RuntimeException(String.format(
                    "You tried to get the value for key '%s' out of this metadata as a long, but nothing was stored against that key",
                    key));
        } else {
            if (object instanceof Long) {
                value = (Long) object;
            } else {
                try {
                    value = Long.parseLong(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as a long, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public int getInt(Object key, int valueIfMissing) {
        int value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Integer) {
                value = (Integer) object;
            } else {
                try {
                    value = Integer.parseInt(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as an int, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public short getShort(Object key, Short valueIfMissing) {
        short value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Short) {
                value = (Short) object;
            } else {
                throw new RuntimeException(String.format(
                        "You tried to get the value for key '%s' out of this metadata as a short, but the object stored was actually a %s",
                        key,
                        object.getClass()));
            }
        }
        return value;
    }

    public long getLong(Object key, Long valueIfMissing) {
        long value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Long) {
                value = (Long) object;
            } else {
                try {
                    value = Long.parseLong(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as a long, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public float getFloat(Object key, Float valueIfMissing) {
        float value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Float) {
                value = (Float) object;
            } else {
                throw new RuntimeException(String.format(
                        "You tried to get the value for key '%s' out of this metadata as a float, but the object stored was actually a %s",
                        key,
                        object.getClass()));
            }
        }
        return value;
    }

    public double getDouble(Object key) {
        double value;
        Object object = this.get(key);
        if (object == null) {
            throw new RuntimeException(String.format(
                    "You tried to get the value for key '%s' out of this metadata as an int, but nothing was stored against that key",
                    key));
        } else {
            if (object instanceof Double) {
                value = (Double) object;
            } else {
                try {
                    value = Double.parseDouble(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as a double, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public double getDouble(Object key, double valueIfMissing) {
        double value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Double) {
                value = (Double) object;
            } else {
                try {
                    value = Double.parseDouble(object.toString());
                } catch (NumberFormatException nfe) {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as an double, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public boolean getBoolean(Object key, boolean valueIfMissing) {
        boolean value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            String toString = object.toString();
            if (toString.equalsIgnoreCase("true")) {
                value = true;
            } else if (toString.equalsIgnoreCase("false")) {
                value = false;
            } else {
                throw new RuntimeException(String.format(
                        "You tried to get the value for key '%s' out of this metadata as a boolean, but the object stored was actually a %s",
                        key,
                        object.getClass()));
            }
        }
        return value;
    }

    public boolean getBoolean(Object key) {
        boolean value;
        Object object = this.get(key);
        if (object == null) {
            throw new RuntimeException(String.format(
                    "You tried to get the value for key '%s' out of this metadata as a boolean, but nothing was stored against that key",
                    key));
        } else {
            if (object instanceof Boolean) {
                value = (Boolean) object;
            } else {
                String toString = object.toString();
                if (toString.equalsIgnoreCase("true")) {
                    value = true;
                } else if (toString.equalsIgnoreCase("false")) {
                    value = false;
                } else {
                    throw new RuntimeException(String.format(
                            "You tried to get the value for key '%s' out of this metadata as a boolean, but the object stored was actually a %s",
                            key,
                            object.getClass()));
                }
            }
        }
        return value;
    }

    public byte getByte(Object key, byte valueIfMissing) {
        byte value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Byte) {
                value = (Byte) object;
            } else {
                throw new RuntimeException(String.format(
                        "You tried to get the value for key '%s' out of this metadata as a byte, but the object stored was actually a %s",
                        key,
                        object.getClass()));
            }
        }
        return value;
    }

    public char getChar(Object key, char valueIfMissing) {
        char value;
        Object object = this.get(key);
        if (object == null) {
            value = valueIfMissing;
        } else {
            if (object instanceof Character) {
                value = (Character) object;
            } else {
                throw new RuntimeException(String.format(
                        "You tried to get the value for key '%s' out of this metadata as a char, but the object stored was actually a %s",
                        key,
                        object.getClass()));
            }
        }
        return value;
    }

    public synchronized void set(Object key, Object value) {
        put(key, value);
    }

    public static Metadata fromProperties(String properties) {
        Metadata metadata = new Metadata();
        metadata.parse(properties);
        return metadata;
    }

    /**
     * Parses string attributes from an xml style "name1='value' name2='value2'" string
     *
     * @param attributes
     */
    public void parse(String attributes) {

        StringUtilsTokeniser tokeniser = StringUtils.tokenise(attributes);

        while (tokeniser.hasMore()) {
            String key = tokeniser.upTo("=").trim();
            tokeniser.skip();
            tokeniser.skipNonCharacterElements();

            char c = tokeniser.peekChar();

            String data;

            if (c == '"') {
                Set<Character> set = new HashSet<Character>();
                set.add('"');
                data = tokeniser.upToOutsideQuotes(set, false);
            } else if (c == '\'') {
                Set<Character> set = new HashSet<Character>();
                set.add('\'');
                data = tokeniser.upToOutsideQuotes(set, false);
            } else {
                data = tokeniser.upToOutsideQuotes(' ');
            }

            tokeniser.skip();

            String value = StringUtils.stripQuotes(data);
            put(key, value);
        }
    }

    public void parseCSV(String properties) {

        if (properties != null && properties.trim().length() > 0) {
            String[] split = properties.split(",");
            for (String string : split) {
                String[] split2 = string.split("=");
                put(split2[0].trim(), split2[1].trim());
            }
        }
    }

    @Override
    public Object put(Object key, Object value) {
        return super.put(key, value);
    }

    public void read(SofReader reader) throws SofException {
        int count = reader.readInt(1);

        // TESTME

        int field = 2;
        for (int i = 0; i < count; i++) {
            Object key = reader.readObject(field++);
            Object value = reader.readObject(field++);
            put(key, value);
        }
    }

    public void write(SofWriter writer) throws SofException {

        Set<java.util.Map.Entry<Object, Object>> entries = entrySet();
        writer.write(1, entries.size());
        int field = 2;
        for (java.util.Map.Entry<Object, Object> entry : entries) {
            // FIXME : need a way of writing Object, and then on the other side it instanceOfs it
            // out
            // to see it can actually be encoded?
            // writer.write(field++, entry.getKey());
            // writer.write(field++, entry.getValue());
        }

    }

    public Metadata copy() {
        Metadata other = new Metadata();
        other.putAll(this);
        return other;

    }

    public static Metadata loadUserSettings(String application) {
        File potentialGlobalSettings = getUserSettingsFile(application);
        logger.info("Loading settings for application '{}' here : {}",
                    application,
                    potentialGlobalSettings.getAbsolutePath());
        return fromFile(potentialGlobalSettings);
    }

    public static File getUserSettingsFile(String application) {
        String userhome = System.getProperty("user.home");
        final File potentialGlobalSettings = new File(userhome, ".vertexlabs/" + application + ".properties");
        return potentialGlobalSettings;

    }

    public static Metadata fromFile(File file) {
        Metadata metadata = new Metadata();
        if (file.exists()) {
            Properties p = new Properties();
            try {
                p.load(new FileInputStream(file));
                metadata.putAll(p);
            } catch (IOException e) {
                throw new FormattedRuntimeException(e, "Failed to load properties from {}", file.getAbsolutePath());
            }
        }
        return metadata;
    }
}
