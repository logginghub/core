package com.logginghub.integrationtests.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.modules.BinaryProcessorModule;
import com.logginghub.logging.modules.configuration.BinaryProcessorConfiguration;
import com.logginghub.logging.modules.configuration.RegexExtractingProcessorConfiguration;
import com.logginghub.logging.repository.BinaryLogFileWriter;
import com.logginghub.logging.repository.DataFileNameFactory;
import com.logginghub.utils.FileDateFormat;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;

public class TestDataFileProcessor {

    @Test public void testOneFileAtATimeRegexWithBetterGenerator() throws IOException {

        Random random = new Random();

        String[] resources = new String[] { "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "rarely/accessed/page.html" };

        int elapsed = 50 + random.nextInt(50);
        int size = 512 + random.nextInt(4096);

        int seconds = 60 * 60;

        File inputFolder = new File("target/test/TestDataFileProcessor/logdata/input");
        File outputFolder = new File("target/test/TestDataFileProcessor/logdata/output");
        File resultsFolder = new File("target/test/TestDataFileProcessor/logdata/results");

        FileUtils.deleteFolderAndContents(inputFolder);
        FileUtils.deleteFolderAndContents(outputFolder);
        FileUtils.deleteFolderAndContents(resultsFolder);

        inputFolder.mkdirs();
        outputFolder.mkdirs();
        resultsFolder.mkdirs();

        File dataFile1 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20120101.120000")));
        File dataFile2 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20120101.100000")));
        File dataFile3 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20000101.140000")));

        BinaryLogFileWriter writer1 = new BinaryLogFileWriter(dataFile1);
        BinaryLogFileWriter writer2 = new BinaryLogFileWriter(dataFile2);
        BinaryLogFileWriter writer3 = new BinaryLogFileWriter(dataFile3);

        long startTime = FileDateFormat.parseHelper("20120101.120000");
        for (int i = 0; i < seconds; i++) {
            int eventsPerSecond = (int) (10 + (10 * Math.sin(i / (double) 60)));

            long time = startTime + (1000 * i);

            long timeInc = (long) (1000 / (double) eventsPerSecond);

            for (int j = 0; j < eventsPerSecond; j++) {
                String logLine = String.format("Operation 'get' complete in %d ms, resource request was '%s' and returned data size was %d bytes",
                                               elapsed,
                                               resources[random.nextInt(resources.length)],
                                               size);

                DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
                event.setLocalCreationTimeMillis(time);
                event.setMessage(logLine);
                writer1.write(event);
                writer2.write(event);
                writer3.write(event);

                time += timeInc;
            }
        }

        writer1.close();
        writer2.close();
        writer3.close();

        BinaryProcessorConfiguration configuration = new BinaryProcessorConfiguration();
        configuration.setInputPath(inputFolder.getAbsolutePath());
        configuration.setOutputPath(outputFolder.getAbsolutePath());
        configuration.setResultsPath(resultsFolder.getAbsolutePath());

        List<RegexExtractingProcessorConfiguration> processors = new ArrayList<RegexExtractingProcessorConfiguration>();
        RegexExtractingProcessorConfiguration config = new RegexExtractingProcessorConfiguration();
        config.setName("regex");
        config.setAggregationPeriod(1000);
        config.setExpression("Operation 'get' complete in {elapsed} ms, resource request was '[request]' and returned data size was {size} bytes");
        processors.add(config);
        configuration.setRegexProcessorsConfiguration(processors);

        BinaryProcessorModule processor = new BinaryProcessorModule();
        processor.configure(configuration, null);
        

        List<File> orderedFileList = processor.getOrderedFileList();
        assertThat(orderedFileList.size(), is(3));
        assertThat(orderedFileList.get(0).getName(), is("20120101.120000.logdata"));
        assertThat(orderedFileList.get(1).getName(), is("20120101.100000.logdata"));
        assertThat(orderedFileList.get(2).getName(), is("20000101.140000.logdata"));

        File first = processor.processNextFile();
        assertThat(first.getAbsolutePath(), is(dataFile1.getAbsolutePath()));

        File second = processor.processNextFile();
        assertThat(second.getAbsolutePath(), is(dataFile2.getAbsolutePath()));

        File third = processor.processNextFile();
        assertThat(third.getAbsolutePath(), is(dataFile3.getAbsolutePath()));

        assertThat(processor.processNextFile(), is(nullValue()));

        // Make sure the regex extractor is doing stuff broadly correct for counts
        File elapsedCountFile = new File(resultsFolder, "20120101.120000/regex.elapsed.count.csv");
        assertThat(elapsedCountFile.exists(), is(true));

        SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();
        CSVReader reader = new CSVReader(new FileReader(elapsedCountFile), ',');
        List<String[]> readAll = reader.readAll();
        for (String[] strings : readAll) {
            double value = Double.parseDouble(strings[1]);
            stats.addValue(value);
        }
        reader.close();

        stats.doCalculations();

        assertThat(readAll.size(), is(seconds));
        assertThat(stats.getCount(), is(seconds));
        assertThat(stats.getMean(), is(closeTo(10d, 0.5d)));

        // And for the frequency counters
        File requestFrequencyFile = new File(resultsFolder, "20120101.120000/regex.request.frequency.csv");
        assertThat(requestFrequencyFile.exists(), is(true));

        reader = new CSVReader(new FileReader(requestFrequencyFile), ',');
        List<String[]> lines = reader.readAll();
        reader.close();

        assertThat(lines.size(), is(3));
        assertThat(lines.get(0)[0], is("request"));
        assertThat(lines.get(0)[1], is("pages/index.html"));
        assertThat(Double.parseDouble(lines.get(0)[2]), is(greaterThan(0d)));
        assertThat(lines.get(1)[0], is("request"));
        assertThat(lines.get(1)[1], is("images/charts/chart.jpg"));
        assertThat(Double.parseDouble(lines.get(1)[2]), is(greaterThan(0d)));
        assertThat(lines.get(2)[0], is("request"));
        assertThat(lines.get(2)[1], is("rarely/accessed/page.html"));
        assertThat(Double.parseDouble(lines.get(2)[2]), is(greaterThan(0d)));

        assertThat(Double.parseDouble(lines.get(0)[2]), is(greaterThan(Double.parseDouble(lines.get(1)[2]))));
        assertThat(Double.parseDouble(lines.get(1)[2]), is(greaterThan(Double.parseDouble(lines.get(2)[2]))));

        processor.stop();
    }

    @Test public void testAllFilesInOneGoRegexWithBetterGenerator() throws IOException {

        Random random = new Random();

        String[] resources = new String[] { "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "images/charts/chart.jpg",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "pages/index.html",
                                           "rarely/accessed/page.html" };

        int elapsed = 50 + random.nextInt(50);
        int size = 512 + random.nextInt(4096);

        int seconds = 60 * 60;

        File inputFolder = new File("target/test/TestDataFileProcessor/logdata/input");
        File outputFolder = new File("target/test/TestDataFileProcessor/logdata/output");
        File resultsFolder = new File("target/test/TestDataFileProcessor/logdata/results");

        FileUtils.deleteFolderAndContents(inputFolder);
        FileUtils.deleteFolderAndContents(outputFolder);
        FileUtils.deleteFolderAndContents(resultsFolder);

        inputFolder.mkdirs();
        outputFolder.mkdirs();
        resultsFolder.mkdirs();

        File dataFile1 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20120101.120000")));
        File dataFile2 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20120101.100000")));
        File dataFile3 = new File(inputFolder, DataFileNameFactory.getFinishedFilename("", FileDateFormat.parseHelper("20000101.140000")));

        BinaryLogFileWriter writer1 = new BinaryLogFileWriter(dataFile1);
        BinaryLogFileWriter writer2 = new BinaryLogFileWriter(dataFile2);
        BinaryLogFileWriter writer3 = new BinaryLogFileWriter(dataFile3);

        long startTime = FileDateFormat.parseHelper("20120101.120000");
        for (int i = 0; i < seconds; i++) {
            int eventsPerSecond = (int) (10 + (10 * Math.sin(i / (double) 60)));

            long time = startTime + (1000 * i);

            long timeInc = (long) (1000 / (double) eventsPerSecond);

            for (int j = 0; j < eventsPerSecond; j++) {
                String logLine = String.format("Operation 'get' complete in %d ms, resource request was '%s' and returned data size was %d bytes",
                                               elapsed,
                                               resources[random.nextInt(resources.length)],
                                               size);

                DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
                event.setLocalCreationTimeMillis(time);
                event.setMessage(logLine);
                writer1.write(event);
                writer2.write(event);
                writer3.write(event);

                time += timeInc;
            }
        }

        writer1.close();
        writer2.close();
        writer3.close();

        BinaryProcessorConfiguration configuration = new BinaryProcessorConfiguration();
        configuration.setInputPath(inputFolder.getAbsolutePath());
        configuration.setOutputPath(outputFolder.getAbsolutePath());
        configuration.setResultsPath(resultsFolder.getAbsolutePath());

        List<RegexExtractingProcessorConfiguration> processors = new ArrayList();
        RegexExtractingProcessorConfiguration config = new RegexExtractingProcessorConfiguration();
        config.setName("regex");
        config.setAggregationPeriod(1000);
        config.setExpression("Operation 'get' complete in {elapsed} ms, resource request was '[request]' and returned data size was {size} bytes");
        processors.add(config);
        configuration.setRegexProcessorsConfiguration(processors);

        BinaryProcessorModule processor = new BinaryProcessorModule();
        processor.configure(configuration, null);

        List<File> orderedFileList = processor.getOrderedFileList();
        assertThat(orderedFileList.size(), is(3));
        assertThat(orderedFileList.get(0).getName(), is("20120101.120000.logdata"));
        assertThat(orderedFileList.get(1).getName(), is("20120101.100000.logdata"));
        assertThat(orderedFileList.get(2).getName(), is("20000101.140000.logdata"));

        processor.processAllFiles();

        assertThat(processor.processNextFile(), is(nullValue()));

        // Make sure the regex extractor is doing stuff broadly correct for counts
        File elapsedCountFile = new File(resultsFolder, "20120101.120000/regex.elapsed.count.csv");
        assertThat(elapsedCountFile.exists(), is(true));

        SinglePassStatisticsDoublePrecision stats = new SinglePassStatisticsDoublePrecision();
        CSVReader reader = new CSVReader(new FileReader(elapsedCountFile), ',');
        List<String[]> readAll = reader.readAll();
        for (String[] strings : readAll) {
            double value = Double.parseDouble(strings[1]);
            stats.addValue(value);
        }

        stats.doCalculations();

        assertThat(readAll.size(), is(seconds));
        assertThat(stats.getCount(), is(seconds));
        assertThat(stats.getMean(), is(closeTo(10d, 0.5d)));

        // And for the frequency counters
        File requestFrequencyFile = new File(resultsFolder, "20120101.120000/regex.request.frequency.csv");
        assertThat(requestFrequencyFile.exists(), is(true));

        reader = new CSVReader(new FileReader(requestFrequencyFile), ',');
        List<String[]> lines = reader.readAll();

        assertThat(lines.size(), is(3));
        assertThat(lines.get(0)[0], is("request"));
        assertThat(lines.get(0)[1], is("pages/index.html"));
        assertThat(Double.parseDouble(lines.get(0)[2]), is(greaterThan(0d)));
        assertThat(lines.get(1)[0], is("request"));
        assertThat(lines.get(1)[1], is("images/charts/chart.jpg"));
        assertThat(Double.parseDouble(lines.get(1)[2]), is(greaterThan(0d)));
        assertThat(lines.get(2)[0], is("request"));
        assertThat(lines.get(2)[1], is("rarely/accessed/page.html"));
        assertThat(Double.parseDouble(lines.get(2)[2]), is(greaterThan(0d)));

        assertThat(Double.parseDouble(lines.get(0)[2]), is(greaterThan(Double.parseDouble(lines.get(1)[2]))));
        assertThat(Double.parseDouble(lines.get(1)[2]), is(greaterThan(Double.parseDouble(lines.get(2)[2]))));

        processor.stop();
    }
}
