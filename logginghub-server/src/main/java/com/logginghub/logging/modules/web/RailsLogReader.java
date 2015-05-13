package com.logginghub.logging.modules.web;

import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.model.TimeSeriesDataContainer;
import com.logginghub.analytics.utils.TimeBucketCounter;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Out;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.logging.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by james on 12/05/2015.
 */
public class RailsLogReader {

    public static void main(String[] args) throws IOException, ParseException {

       final TimeBucketCounter histogram = new TimeBucketCounter(60000);

        final long start = parseTime("2015-05-06 00:00:00 +0100");
        final long end = parseTime("2015-05-06 12:00:00 +0100");

        final BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/james/development/divmax/logs/production.log.txt"));
        RailsLogReader reader = new RailsLogReader();
        reader.read(new File("/Users/james/development/divmax/logs/production.log"), new Visitor<Entry>() {
            @Override
            public void visit(Entry i) {
                if (i.time > start && i.time < end) {
                    histogram.count(i.time, "Request", 1);
                }
                try {
                    writer.write(i.toString());
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        writer.close();

        TimeSeriesDataContainer timeSeriesDataContainer = histogram.extractAllSeries();
        ChartBuilder.start(ChartBuilder.Type.XY)
                    .addSeries(timeSeriesDataContainer, 42)
                    .setTitle("Frequency of messages")
                    .toPng(new File("chart.png"));
    }

    class Entry {
        String method;
        String url;
        String ip;
        long time;
        double duration;
        String codeNumber;
        String statusText;

        @Override
        public String toString() {
            return String.format("%20s %20s %20s %10s %20s %10.2f ms %150s",
                                 Logger.toLocalDateString(time),
                                 method,
                                 ip,
                                 codeNumber,
                                 statusText,
                                 duration,
                                 url);
        }
    }

    public void read(File file, Visitor<Entry> visitor) throws IOException, ParseException {

        Out.out("Reading file '{}'", file.getAbsolutePath());

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int index = 0;

        Entry entry = null;

        while ((line = reader.readLine()) != null) {
            //            System.out.println(line);

            if (line.startsWith("Started ")) {
                String[] split = line.split(" ");

                if (entry != null) {
                    System.err.println("Rejecting unfulfilled request");
                }

                entry = new Entry();
                entry.method = split[1];
                entry.url = StringUtils.unquote(split[2]);
                entry.ip = split[4];

                String timeString = split[6];
                timeString += " " + split[7];
                timeString += " " + split[8];

                long parsed = parseTime(timeString);
                String formatted = Logger.toLocalDateString(parsed).toString();

                entry.time = parsed;

                //                System.out.println(">>>> " + line);

            } else if (line.startsWith("Completed ")) {

                if (entry == null) {
                    System.err.println("Entry is currently null - did we start the log halfway through an entry?");
                } else {
                    String[] split = line.split(" ");
                    entry.codeNumber = split[1];
                    entry.statusText = split[2];

                    // Need to do a search for the time
                    int searchIndex = 3;
                    for (; searchIndex < split.length; searchIndex++) {
                        if (split[searchIndex].equals("in")) {
                            break;
                        }
                    }

                    entry.duration = Double.parseDouble(StringUtils.before(split[searchIndex + 1], "ms"));

                    visitor.visit(entry);
                    entry = null;
                }

                //                System.out.println("<<<< " + line);


            } else {
                //System.out.println(line);
            }

            index++;

        }

        FileUtils.closeQuietly(reader);


    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private static long parseTime(String timeString) throws ParseException {
        return dateFormat.parse(timeString).getTime();
    }

}
