package com.logginghub.logging.modules.web;

import com.logginghub.analytics.model.LongFrequencyCount;
import com.logginghub.logging.messaging.PatternisedLogEvent;
import com.logginghub.utils.MemorySnapshot;
import com.logginghub.utils.SinglePassStatisticsDoublePrecision;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by james on 12/05/2015.
 */
public class DataController {

    private Map<Integer, PatternData> patternDataByPatternId = new HashMap<Integer, PatternData>();

    // TODO : get the pattern metadata from somewhere
    enum VariableType {
        Ignore,
        Numeric,
        NonNumeric
    }

    private VariableType[] patternMetadata = new VariableType[]{VariableType.Ignore,
                                                                VariableType.Ignore,
                                                                VariableType.Ignore,
                                                                VariableType.Ignore,
                                                                VariableType.Ignore,
                                                                VariableType.Numeric};

    // TODO : potential leaking of calenders? Periodically clear it down?
    private ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return new GregorianCalendar();
        }
    };

    public List<YearlyData> getYears() {
        List<YearlyData> years = new ArrayList<YearlyData>();

        PatternData patternData = patternDataByPatternId.get(0);
        for (YearlyData yearlyData : patternData.yearlyData.values()) {
            years.add(yearlyData);
        }

        return years;
    }

    public YearlyData getYear(int year) {
        PatternData patternData = patternDataByPatternId.get(0);
        YearlyData yearlyData = patternData.yearlyData.get(PeriodKey.year(year));
        return yearlyData;
    }

    private List<SecondData> dirtySeconds = new ArrayList<SecondData>();

    static class PeriodKey implements Comparable<PeriodKey> {
        int year = -1;
        int month = -1;
        int dayOfMonth = -1;
        int dayOfWeek = -1;

        int hour = -1;
        int minute = -1;
        int second = -1;

        public static PeriodKey year(int year) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            return key;
        }

        public static PeriodKey month(int year, int month) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            return key;
        }

        public static PeriodKey dayOfWeek(int year, int month, int dayOfWeek) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            key.dayOfWeek = dayOfWeek;
            return key;
        }

        public static PeriodKey dayOfMonth(int year, int month, int dayOfMonth) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            key.dayOfMonth = dayOfMonth;
            return key;
        }

        public static PeriodKey hour(int year, int month, int dayOfMonth, int hour) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            key.dayOfMonth = dayOfMonth;
            key.hour = hour;
            return key;
        }

        public static PeriodKey minute(int year, int month, int dayOfMonth, int hour, int minute) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            key.dayOfMonth = dayOfMonth;
            key.hour = hour;
            key.minute = minute;
            return key;
        }

        public static PeriodKey second(int year, int month, int dayOfMonth, int hour, int minute, int second) {
            PeriodKey key = new PeriodKey();
            key.year = year;
            key.month = month;
            key.dayOfMonth = dayOfMonth;
            key.hour = hour;
            key.minute = minute;
            key.second = second;
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            PeriodKey periodKey = (PeriodKey) o;

            if (year != periodKey.year)
                return false;
            if (month != periodKey.month)
                return false;
            if (dayOfMonth != periodKey.dayOfMonth)
                return false;
            if (dayOfWeek != periodKey.dayOfWeek)
                return false;
            if (hour != periodKey.hour)
                return false;
            if (minute != periodKey.minute)
                return false;
            return second == periodKey.second;

        }

        @Override
        public int hashCode() {
            int result = year;
            result = 31 * result + month;
            result = 31 * result + dayOfMonth;
            result = 31 * result + dayOfWeek;
            result = 31 * result + hour;
            result = 31 * result + minute;
            result = 31 * result + second;
            return result;
        }


        @Override
        public int compareTo(PeriodKey that) {
            if (this.year < that.year) {
                return -1;
            } else if (this.year > that.year) {
                return 1;
            }

            if (this.month < that.month) {
                return -1;
            } else if (this.month > that.month) {
                return 1;
            }

            if (this.dayOfMonth < that.dayOfMonth) {
                return -1;
            } else if (this.dayOfMonth > that.dayOfMonth) {
                return 1;
            }

            if (this.dayOfWeek < that.dayOfWeek) {
                return -1;
            } else if (this.dayOfWeek > that.dayOfWeek) {
                return 1;
            }

            if (this.hour < that.hour) {
                return -1;
            } else if (this.hour > that.hour) {
                return 1;
            }

            if (this.minute < that.minute) {
                return -1;
            } else if (this.minute > that.minute) {
                return 1;
            }

            if (this.second < that.second) {
                return -1;
            } else if (this.second > that.second) {
                return 1;
            }
            return 0;
        }
    }

    abstract class AbstractPeriodData {
        private final PeriodKey key;
        private final AbstractPeriodData parent;
        private long count;
        public LongFrequencyCount[] frequencyCounts = new LongFrequencyCount[patternMetadata.length];
        public SinglePassStatisticsDoublePrecision[] numericStats = new SinglePassStatisticsDoublePrecision[patternMetadata.length];

        public AbstractPeriodData(PeriodKey yearKey, AbstractPeriodData parent) {
            key = yearKey;
            this.parent = parent;
        }

        public PeriodKey getKey() {
            return key;
        }

        public LongFrequencyCount[] getFrequencyCounts() {
            return frequencyCounts;
        }

        public SinglePassStatisticsDoublePrecision[] getNumericStats() {
            return numericStats;
        }

        public AbstractPeriodData getParent() {
            return parent;
        }

        public void onItemAdded(PatternisedLogEvent event) {
            count++;

            String[] variables = event.getVariables();
            for (int i = 0; i < variables.length; i++) {
                String value = variables[i];
                VariableType type = patternMetadata[i];

                if(type == VariableType.Ignore) {
                    // Easy
                }
                else if (type == VariableType.NonNumeric) {
                    // All we do with non-numeric is frequency counting
                    LongFrequencyCount frequencyCount = frequencyCounts[i];
                    if (frequencyCount == null) {
                        frequencyCount = new LongFrequencyCount();
                        frequencyCounts[i] = frequencyCount;
                    }

                    // We'll intern the strings in the hope there is relatively low cardinality - if we have a variable capturing a sequence, then we are buggered
                    frequencyCount.count(value.intern(), 1);
                } else {
                    // For numeric values we keep a full set of stats
                    SinglePassStatisticsDoublePrecision stats = numericStats[i];
                    if (stats == null) {
                        stats = new SinglePassStatisticsDoublePrecision();
                        numericStats[i] = stats;
                    }

                    stats.addValue(Double.parseDouble(value));
                }
            }

            if (parent != null) {
                parent.onItemAdded(event);
            }
        }

        public long getCount() {
            return count;
        }
    }

    class SecondData extends AbstractPeriodData {

        private List<PatternisedLogEvent> data = new ArrayList<PatternisedLogEvent>();
        private boolean dirty = false;

        public SecondData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public List<PatternisedLogEvent> getData() {
            return data;
        }


        public void add(PatternisedLogEvent event) {
            data.add(event);
            dirty = true;
            dirtySeconds.add(this);

            onItemAdded(event);
        }

    }

    class MinuteData extends AbstractPeriodData {
        private Map<PeriodKey, SecondData> subPeriodData = new HashMap<PeriodKey, SecondData>();

        public MinuteData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public Map<PeriodKey, SecondData> getSubPeriodData() {
            return subPeriodData;
        }

        public SecondData add(PeriodKey key) {
            SecondData subPeriod = subPeriodData.get(key);
            if (subPeriod == null) {
                subPeriod = new SecondData(key, this);
                subPeriodData.put(key, subPeriod);
            }
            return subPeriod;
        }
    }

    class HourlyData extends AbstractPeriodData {
        private Map<PeriodKey, MinuteData> subPeriodData = new HashMap<PeriodKey, MinuteData>();

        public HourlyData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public Map<PeriodKey, MinuteData> getSubPeriodData() {
            return subPeriodData;
        }

        public MinuteData add(PeriodKey key) {
            MinuteData subPeriod = subPeriodData.get(key);
            if (subPeriod == null) {
                subPeriod = new MinuteData(key, this);
                subPeriodData.put(key, subPeriod);
            }
            return subPeriod;
        }
    }

    class DailyData extends AbstractPeriodData {
        private Map<PeriodKey, HourlyData> subPeriodData = new HashMap<PeriodKey, HourlyData>();

        public DailyData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public Map<PeriodKey, HourlyData> getSubPeriodData() {
            return subPeriodData;
        }

        public HourlyData add(PeriodKey key) {
            HourlyData subPeriod = subPeriodData.get(key);
            if (subPeriod == null) {
                subPeriod = new HourlyData(key, this);
                subPeriodData.put(key, subPeriod);
            }
            return subPeriod;
        }
    }

    class MonthlyData extends AbstractPeriodData {

        private Map<PeriodKey, DailyData> subPeriodData = new HashMap<PeriodKey, DailyData>();

        public MonthlyData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public Map<PeriodKey, DailyData> getSubPeriodData() {
            return subPeriodData;
        }

        public DailyData add(PeriodKey key) {
            DailyData subPeriod = subPeriodData.get(key);
            if (subPeriod == null) {
                subPeriod = new DailyData(key, this);
                subPeriodData.put(key, subPeriod);
            }
            return subPeriod;
        }
    }

    class YearlyData extends AbstractPeriodData {
        private Map<PeriodKey, MonthlyData> subPeriodData = new HashMap<PeriodKey, MonthlyData>();

        public YearlyData(PeriodKey key, AbstractPeriodData parent) {
            super(key, parent);
        }

        public MonthlyData add(PeriodKey key) {
            MonthlyData subPeriod = subPeriodData.get(key);
            if (subPeriod == null) {
                subPeriod = new MonthlyData(key, this);
                subPeriodData.put(key, subPeriod);
            }
            return subPeriod;
        }

        public Map<PeriodKey, MonthlyData> getSubPeriodData() {
            return subPeriodData;
        }
    }

    class PatternData {
        int patternId;

        private Map<PeriodKey, YearlyData> yearlyData = new HashMap<PeriodKey, YearlyData>();

        public void add(PatternisedLogEvent event) {

            Calendar calendar = calendarThreadLocal.get();
            calendar.setTimeInMillis(event.getTime());

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            PeriodKey yearKey = PeriodKey.year(year);
            PeriodKey monthKey = PeriodKey.month(year, month);
            PeriodKey dayOfMonthKey = PeriodKey.dayOfMonth(year, month, dayOfMonth);
            PeriodKey dayOfWeekKey = PeriodKey.dayOfWeek(year, month, dayOfWeek);

            PeriodKey hourKey = PeriodKey.hour(year, month, dayOfMonth, hour);
            PeriodKey minuteKey = PeriodKey.minute(year, month, dayOfMonth, hour, minute);
            PeriodKey secondKey = PeriodKey.second(year, month, dayOfMonth, hour, minute, second);

            YearlyData yearly = yearlyData.get(yearKey);
            if (yearly == null) {
                yearly = new YearlyData(yearKey, null);
                yearlyData.put(yearKey, yearly);
            }

            yearly.add(monthKey).add(dayOfMonthKey).add(hourKey).add(minuteKey).add(secondKey).add(event);


        }
    }

    public void loadData() {
        try {

            WorkerThread workerThread = MemorySnapshot.runMonitor();

            int files = 11;
            for (int i = 0; i < files; i++) {

                String inputFilename = "production.log";
                String outputFilename = "production.log.txt";

                if (i > 0) {
                    inputFilename += "." + Integer.toString(i);
                    outputFilename += "." + Integer.toString(i);
                }

                File sourceFolder = new File("/Users/james/development/divmax/logs/");
                File inputFile = new File(sourceFolder, inputFilename);
                File outputFile = new File(sourceFolder, outputFilename);

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                RailsLogReader reader = new RailsLogReader();

                reader.read(inputFile, new Visitor<RailsLogReader.Entry>() {
                    @Override
                    public void visit(RailsLogReader.Entry entry) {
                        PatternisedLogEvent event = new PatternisedLogEvent(Logger.info, entry.time, 0, 0, "Rails");

                        event.setVariables(new String[]{entry.method, entry.codeNumber, entry.statusText, entry.ip, entry.url, "" + entry.duration});

                        add(event);
                    }
                });

                writer.close();
            }
            updateStats();

            workerThread.stop();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void updateStats() {


    }

    private void add(PatternisedLogEvent event) {

        PatternData patternData = patternDataByPatternId.get(event.getPatternID());
        if (patternData == null) {
            patternData = new PatternData();
            patternData.patternId = event.getPatternID();
            patternDataByPatternId.put(event.getPatternID(), patternData);
        }

        patternData.add(event);

    }
}
