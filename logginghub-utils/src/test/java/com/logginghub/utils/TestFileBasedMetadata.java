package com.logginghub.utils;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.logginghub.utils.FileBasedMetadata;
import com.logginghub.utils.FileUtils;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TestFileBasedMetadata {

    @Test public void testSave() {

        File source = new File("target/temp/testFileBasedMetadata/testSave.metadata");
        source.getParentFile().mkdirs();
        if (source.exists()) {
            FileUtils.deleteLoudly(source);
        }
        
        assertThat(source.exists(), is(false));
        FileBasedMetadata metadata = new FileBasedMetadata(source);
        metadata.load();
        assertThat(source.exists(), is(false));
        assertThat(metadata.isEmpty(), is(true));

        metadata.set("intKey", 1);
        metadata.set("doubleKey", 1.1d);
        metadata.set("booleanKey", true);

        assertThat(metadata.getInt("intKey"), is(1));
        assertThat(metadata.getDouble("doubleKey"), is(1.1d));
        assertThat(metadata.getBoolean("booleanKey"), is(true));

        metadata.save();

        assertThat(source.exists(), is(true));

        List<String> lines = FileUtils.readAsStringList(source);
        assertThat(lines.size(), is(3));
        assertThat(lines.contains("intKey=1"), is(true));
        assertThat(lines.contains("doubleKey=1.1"), is(true));
        assertThat(lines.contains("booleanKey=true"), is(true));
        
        FileBasedMetadata loadedData = new FileBasedMetadata(source);
        loadedData.load();
        assertThat(loadedData.getInt("intKey"), is(1));
        assertThat(loadedData.getDouble("doubleKey"), is(1.1d));
        assertThat(loadedData.getBoolean("booleanKey"), is(true));
    }

}
