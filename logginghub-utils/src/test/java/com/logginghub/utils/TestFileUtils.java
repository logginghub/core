package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.StringUtils;

public class TestFileUtils {

    @Test public void test_generateMD5() {

        String testData1 = "This is our test data";
        String testData2 = "This is our";

        File randomFile1 = FileUtils.createRandomTestFileForClass(TestFileUtils.class);
        File randomFile2 = FileUtils.createRandomTestFileForClass(TestFileUtils.class);

        FileUtils.write(testData1, randomFile1);
        FileUtils.write(testData2, randomFile2);

        assertThat(randomFile1.length(), is(21L));
        assertThat(randomFile2.length(), is(11L));

        byte[] fullMD1 = FileUtils.generateMD5(randomFile1);
        byte[] fullMD2 = FileUtils.generateMD5(randomFile2);

        assertThat(fullMD1.length, is(16));
        assertThat(fullMD2.length, is(16));

        byte[] partialMD1 = FileUtils.generateMD5(randomFile1, 11);
        assertThat(partialMD1, is(not(fullMD1)));
        assertThat(partialMD1, is(fullMD2));

        // Build a really big file so the stream readers have to loop a bit
        String commonStart = StringUtils.randomString(500000);
        
        FileUtils.write(commonStart + StringUtils.randomString(1000000), randomFile1);
        FileUtils.write(commonStart, randomFile2);
        
        partialMD1 = FileUtils.generateMD5(randomFile1, 500000);
        fullMD2 = FileUtils.generateMD5(randomFile2);
        
        assertThat(partialMD1, is(not(fullMD1)));
        assertThat(partialMD1, is(fullMD2));

    }

    @Test public void testGetFileExtension() {

        assertThat(FileUtils.getFileExtension(new File("a/b.c")), is("c"));
        assertThat(FileUtils.getFileExtension(new File("a/b/c")), is(""));

        assertThat(FileUtils.getFileExtension(new File("a/b/c.jpg")), is("jpg"));
        assertThat(FileUtils.getFileExtension(new File("a/b/c.jpg.jpg")), is("jpg"));

        assertThat(FileUtils.getFileExtension(new File("a.b/c")), is(""));
        assertThat(FileUtils.getFileExtension(new File("a.b/c.jpg")), is("jpg"));
        assertThat(FileUtils.getFileExtension(new File("a.b/c.jpg.jpg")), is("jpg"));

        assertThat(FileUtils.getFileExtension(new File("c")), is(""));
        assertThat(FileUtils.getFileExtension(new File("c.jpg")), is("jpg"));
        assertThat(FileUtils.getFileExtension(new File("c.jpg.jpg")), is("jpg"));
    }

    @Test public void testGetFileNameWithoutExtension() {
        assertThat(FileUtils.getNameWithoutExtension(new File("a/b.c")), is("b"));
        assertThat(FileUtils.getNameWithoutExtension(new File("a/b/c")), is("c"));

        assertThat(FileUtils.getNameWithoutExtension(new File("a/b/c.jpg")), is("c"));
        assertThat(FileUtils.getNameWithoutExtension(new File("a/b/c.jpg.jpg")), is("c.jpg"));

        assertThat(FileUtils.getNameWithoutExtension(new File("a.b/c")), is("c"));
        assertThat(FileUtils.getNameWithoutExtension(new File("a.b/c.jpg")), is("c"));
        assertThat(FileUtils.getNameWithoutExtension(new File("a.b/c.jpg.jpg")), is("c.jpg"));

        assertThat(FileUtils.getNameWithoutExtension(new File("c")), is("c"));
        assertThat(FileUtils.getNameWithoutExtension(new File("c.jpg")), is("c"));
        assertThat(FileUtils.getNameWithoutExtension(new File("c.jpg.jpg")), is("c.jpg"));
    }

    @Test public void testGetRandomFile() {

        File randomFile = FileUtils.getRandomFile("target/testGetRandomFile", "prefix-", "-postfix");
        File randomFile2 = FileUtils.getRandomFile("target/testGetRandomFile", "prefix-", "-postfix");

        assertThat(randomFile.exists(), is(false));
        assertThat(randomFile.getAbsoluteFile().getParent(), endsWith("target" + File.separatorChar + "testGetRandomFile"));
        assertThat(randomFile.getName(), startsWith("prefix-"));
        assertThat(randomFile.getName(), endsWith("-postfix"));

        assertThat(randomFile2.exists(), is(false));
        assertThat(randomFile2.getAbsoluteFile().getParent(), endsWith("target" + File.separatorChar + "testGetRandomFile"));
        assertThat(randomFile2.getName(), startsWith("prefix-"));
        assertThat(randomFile2.getName(), endsWith("-postfix"));

        assertThat(randomFile.getName(), is(not(randomFile2.getName())));

    }

    @Test public void testExtractFirstXLines() throws Exception {

    }

    @Test public void test_get_chunk_iterator_uneven() throws IOException {

        File random = FileUtils.getRandomFile("target/test/random", "random", ".txt");
        FileUtils.write("abcdefghijklmnopqrstuvwxyz", random);

        Iterator<byte[]> chunkIterator = FileUtils.getChunkIterator(random, 5);

        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("abcde"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("fghij"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("klmno"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("pqrst"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("uvwxy"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("z"));
        assertThat(chunkIterator.hasNext(), is(false));

    }

    @Test public void test_get_chunk_iterator_even() throws IOException {

        File random = FileUtils.getRandomFile("target/test/random", "random", ".txt");
        FileUtils.write("abcdefghijklmnopqrstuvwxyz", random);

        Iterator<byte[]> chunkIterator = FileUtils.getChunkIterator(random, 13);

        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("abcdefghijklm"));
        assertThat(chunkIterator.hasNext(), is(true));
        assertThat(new String(chunkIterator.next()), is("nopqrstuvwxyz"));
        assertThat(chunkIterator.hasNext(), is(false));

    }

}
