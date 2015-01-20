package com.logginghub.analytics.demo;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.logginghub.analytics.AggregatedDataKey;
import com.logginghub.analytics.AggregatedDataPlotter;
import com.logginghub.analytics.ChartBuilder;
import com.logginghub.analytics.Grouper;
import com.logginghub.analytics.Log;
import com.logginghub.analytics.TimeSeriesAggregator;
import com.logginghub.analytics.model.AggregatedData;
import com.logginghub.analytics.model.MultiSeriesAggreatedData;
import com.logginghub.analytics.model.TimeSeriesData;
import com.logginghub.analytics.utils.AggregatedDataSplitter;
import com.logginghub.utils.FactoryMap;
import com.logginghub.utils.swing.TestFrame;

public class DocDemo {
    private static Random random = new Random();
    private static Log log = Log.create(DocDemo.class);

    public static void main(String[] args) {

        Log.setGlobalLevel(Log.INFO);
        // Run the extract and pull out the save data into TimeSeriesData object
        // That'll capture the time|user,type,protocol|elapsed,compressed size,
        // uncompressed size
        TimeSeriesData fromFile = generateData();

        Grouper grouper = new Grouper();
        HashMap<String, TimeSeriesData> groups = grouper.group(fromFile, 0, 1, 2);

        // Save that file
        // Run the aggregator - against the compressed size

        TimeSeriesAggregator aggregator = new TimeSeriesAggregator();
        MultiSeriesAggreatedData aggregated = aggregator.aggregate(groups, 2000, 0);

        aggregated.sortAscending(AggregatedDataKey.Sum);
        List<AggregatedData> orderedData = aggregated.getOrderedData();
        for (AggregatedData aggregatedData : orderedData) {
            System.out.println(String.format("%s : %,.0f", aggregatedData, aggregatedData.getValue(AggregatedDataKey.Sum)));
        }
        
        aggregated.sortDescending(AggregatedDataKey.Sum);
        for (AggregatedData aggregatedData : orderedData) {
            System.out.println(String.format("%s : %,.0f", aggregatedData, aggregatedData.getValue(AggregatedDataKey.Sum)));
        }
        
        ChartBuilder.startXY().addSeries(aggregated, AggregatedDataKey.Sum).showInFrame();
        
        MultiSeriesAggreatedData top = aggregated.top(1, true);
        ChartBuilder.startXY().addSeries(top, AggregatedDataKey.Sum).showInFrame();
        
        
//        DataFunction dataFunction = new DataFunction("Rate", AggregatedDataKey.Sum) {
//            @Override public double function(double value) {
//                return value / 60;
//            }};
//        ChartBuilder.start().addSeries(top, dataFunction).showInFrame();
        
        
        AggregatedDataSplitter splitter = new AggregatedDataSplitter();
        List<MultiSeriesAggreatedData> splitData = splitter.split(aggregated, 1, TimeUnit.HOURS);
        log.info("Split complete");

        File folder = new File("output/");
        folder.mkdirs();
        log.info("Rendering");

        int count = 0;
        for (MultiSeriesAggreatedData multiSeriesAggreatedData : splitData) {

            if (count == 1) {
                ChartBuilder builder = ChartBuilder.startXY();
                builder.setTitleFromLegends(multiSeriesAggreatedData);

                Set<String> seriesNames = multiSeriesAggreatedData.getSeriesNames();
                for (String string : seriesNames) {
                    AggregatedData series = multiSeriesAggreatedData.getSeries(string);                    
                    builder.addSeries(string, series, AggregatedDataKey.Sum);
                }

                builder.showInFrame();
            }

            count++;
            if (count == 2) break;
        }
    }

    static class MinMax {
        public MinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        int min;
        int max;

        public int random() {
            return min + random.nextInt(max - min);
        }
    }

    private static TimeSeriesData generateData() {

        TimeSeriesData data = new TimeSeriesData();

        data.setKeysLegend(new String[] {"Type", "Protocol", "User" });
        data.setValuesLegend(new String[] { "Data size", "Elapsed"});
        
        Calendar c = new GregorianCalendar();

        c.add(Calendar.DAY_OF_MONTH, -1);

        long startTime = c.getTimeInMillis();
        long endTime = System.currentTimeMillis();

        String[][] keys = new String[][] { new String[] { "EquityPrice", "live", "mds" },
                                          new String[] { "FXBenchmark", "official", "cibm_fr_sud" },
                                          new String[] { "BondPrice", "official", "mds" } };

        Map<String, MinMax> typeSizes = new HashMap<String, MinMax>();
        typeSizes.put("EquityPrice", new MinMax(200, 400));
        typeSizes.put("FXBenchmark", new MinMax(2200, 100400));
        typeSizes.put("BondPrice", new MinMax(800, 1500));

        int keyIndex = 0;
        for (long time = startTime; time < endTime; time += 500) {

            String[] keysSelection = keys[keyIndex];
            int size = typeSizes.get(keysSelection[0]).random();
            int elapsedTime = 15 + random.nextInt(50);

            data.add(time, keysSelection, new double[] { size, elapsedTime });

            keyIndex++;
            if (keyIndex == keys.length) {
                keyIndex = 0;
            }
        }

        return data;

    }
}
