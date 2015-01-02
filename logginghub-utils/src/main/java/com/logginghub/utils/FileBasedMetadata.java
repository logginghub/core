package com.logginghub.utils;

import java.io.File;
import java.util.Set;

import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.logging.Logger;

public class FileBasedMetadata extends Metadata {

    private static final Logger logger = Logger.getLoggerFor(FileBasedMetadata.class);
    private static final long serialVersionUID = 1L;
    private File source;

    public FileBasedMetadata(File source) {
        this.source = source;
    }

    public static FileBasedMetadata load(File source) {
        FileBasedMetadata fileBasedMetadata = new FileBasedMetadata(source);
        fileBasedMetadata.load();
        return fileBasedMetadata;
    }

    public synchronized void load() {
        clear();

        if (source.exists()) {
            String[] lines = FileUtils.readAsStringArray(source);
            for (String string : lines) {
                if (string.trim().length() > 0) {
                    Pair<String, String> key = StringUtils.splitAroundLast(string, "=");
                    logger.debug("Loaded entry : '{}' = '{}'", key.getA(), key.getB());
                    put(key.getA(), key.getB());
                }
            }
        }
    }

    public synchronized void save() {
        StringUtilsBuilder builder = StringUtils.builder();
        Set<Object> keySet = keySet();
        for (Object object : keySet) {
            String key = object.toString();
            String value = get(key).toString();
            builder.format("{}={}\r\n", key, value);
        }
        logger.debug("Saved dynamic properties to '{}'", source.getAbsolutePath());
        FileUtils.write(builder.toString(), source);
    }
}
