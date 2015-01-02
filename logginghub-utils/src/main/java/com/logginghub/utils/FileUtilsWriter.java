package com.logginghub.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtilsWriter {

    private BufferedWriter writer;
    private File file;

    public FileUtilsWriter(File properties) {
        this.file = properties;
        try {
            writer = new BufferedWriter(new FileWriter(properties));
        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to open file '{}' for writing", properties.getAbsolutePath());
        }
    }

    public File getFile() {
        return file;
    }

    public void appendLine(String format, Object... objects) {
        try {
            writer.write(StringUtils.format(format, objects));
            writer.newLine();
        }
        catch (IOException e) {
            throw new FormattedRuntimeException(e, "Failed to write to file '{}' for writing", file.getAbsolutePath());
        }
    }

    public void close() {
        FileUtils.closeQuietly(writer);
    }

    public static FileUtilsWriter createTestFile(Class<?> clazz) {
        return new FileUtilsWriter(FileUtils.createRandomTestFileForClass(clazz));
    }

}
