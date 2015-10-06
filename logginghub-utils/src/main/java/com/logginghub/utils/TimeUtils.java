package com.logginghub.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static long seconds = 1000;
    public static long minutes = seconds * 60;
    public static long tenMinutes = minutes * 10;
    public static long twentyMinutes = minutes * 20;
    public static long halfHours = minutes * 30;
    public static long hours = minutes * 60;
    public static long twoHours = hours * 2;
    public static long threeHours = hours * 3;
    public static long sixHours = hours * 6;
    public static long twelveHours = hours * 12;
    public static long days = hours * 24;
    public static long weeks = days * 7;
    public static long months = weeks * 4;
    private static TimeProvider timeProvider = new SystemTimeProvider();
    public static long milliseconds = 1;

    public static long after(long base, String delta) {
        long parseInterval = TimeUtils.parseInterval(delta);
        return base + parseInterval;
    }

    public static long before(long base, String delta) {
        long parseInterval = TimeUtils.parseInterval(delta);
        return base - parseInterval;
    }

    public static long parseInterval(String newValue) {

        Is.notNullOrEmpty(newValue, "Value cannot be null or empty");

        String clean = newValue.replace(",", "");
        clean = clean.replace(" and", "");
        clean = clean.replace("and", "");

        String[] split = clean.split(" ");

        long total = 0;
        if (split.length == 1) {

            // Check for "1ms" with no space
            StringUtilsTokeniser st = new StringUtilsTokeniser(clean);
            String first = st.nextUpToCharacterTypeChange();
            String second = st.restOfString();

            if (StringUtils.isNotNullOrEmpty(second)) {
                double amountValue = Double.parseDouble(first);
                long millis = toMilliseconds(amountValue, second);
                total += millis;
            } else {

                try {
                    total = Long.parseLong(clean);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("We couldn't understand the interval value '" + newValue + "', please double check it");
                }
            }
        } else {

            if (split.length % 2 != 0) {
                throw new IllegalArgumentException("We couldn't understand the interval value '" +
                                                   newValue +
                                                   "', please double check it - it needs to have an even number of items, for example '10 minutes 4 seconds'");
            }

            for (int i = 0; i < split.length; i += 2) {
                String amount = split[i];
                String units = split[i + 1];

                double amountValue = Double.parseDouble(amount);

                long millis = toMilliseconds(amountValue, units);
                total += millis;
            }
        }

        return total;

    }

    public static long toMilliseconds(double amount, String units) {

        char charAt = units.charAt(0);
        charAt = Character.toLowerCase(charAt);

        double factor;

        switch (charAt) {
            case 's': {
                factor = 1000;
                break;
            }
            case 'n': {
                factor = 1e-6;
                break;
            }
            case 'm': {
                if (units.length() == 1) {
                    factor = TimeUtils.minutes;
                } else {
                    char secondChar = Character.toLowerCase(units.charAt(1));
                    switch (secondChar) {
                        case 's': {
                            factor = 1;
                            break;
                        }
                        case 'o': {
                            factor = TimeUtils.months;
                            break;
                        }
                        case 'i': {
                            char thirdChar = Character.toLowerCase(units.charAt(2));
                            switch (thirdChar) {
                                case 'n': {
                                    factor = TimeUtils.minutes;
                                    break;
                                }
                                case 'l': {
                                    factor = 1;
                                    break;
                                }
                                case 'c': {
                                    factor = 1e-3;
                                    break;
                                }
                                default: {
                                    throw new IllegalArgumentException("Couldn't work out how to parse '" +
                                                                       units +
                                                                       "', it doesn't look like a regular time unit (sec/min/hour etc)");
                                }
                            }

                            break;
                        }
                        case 'u': {
                            factor = 1e-3;
                            break;
                        }
                        default: {
                            throw new IllegalArgumentException("Couldn't work out how to parse '" +
                                                               units +
                                                               "', it doesn't look like a regular time unit (sec/min/hour etc)");
                        }
                    }
                }
                break;
            }
            case 'h': {
                factor = TimeUtils.hours;
                break;
            }
            case 'd': {
                factor = TimeUtils.days;
                break;
            }

            default:
                throw new IllegalArgumentException("Couldn't work out how to parse '" +
                                                   units +
                                                   "', it doesn't look like a regular time unit (sec/min/hour etc)");
        }

        return (long) (amount * factor);

    }

    public static Calendar buildCalendarForTime(long baseTime, String input) {
        String[] split = input.split("[:|.]");

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        if (split.length > 0) {
            hours = Integer.parseInt(split[0]);
        }

        if (split.length > 1) {
            minutes = Integer.parseInt(split[1]);
        }

        if (split.length > 2) {
            seconds = Integer.parseInt(split[2]);
        }

        if (split.length > 3) {
            milliseconds = Integer.parseInt(split[3]);
        }

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(baseTime);
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        return calendar;
    }

    public static Calendar calendar(int hour, int minute, int seconds, int millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar;
    }

    public static Calendar calendarAtStartOfDay(int year, int month, int day) {
        return calendar(year, month, day, 0, 0, 0, 0);
    }

    public static Calendar calendarAtStartOfMonth(int year, int month) {
        return calendar(year, month, 1, 0, 0, 0, 0);
    }

    public static Calendar calendarAtStartOfToday() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar calendarAtStartOfYear(int year) {
        return calendar(year, 0, 1, 0, 0, 0, 0);
    }

    public static Calendar calendar(int year, int zeroIndexMonth, int day, int hour, int minute, int seconds, int millis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, zeroIndexMonth);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, millis);
        return calendar;
    }

    public static long days(int days, int minutes, int seconds) {
        return days(days) + minutes(minutes) + seconds(seconds);
    }

    public static long days(int days, int hours, int minutes, int seconds) {
        return days(days) + hours(hours) + minutes(minutes) + seconds(seconds);
    }

    public static long daysFromNow(int days) {
        return timeProvider.getTime() + days(days);
    }

    public static long days(int i) {
        return days * i;

    }

    public static String format(long newValue, String timezone) {
        DateFormat dateThenTimeWithMillis = DateFormatFactory.getDateThenTimeWithMillis(TimeZone.getTimeZone(timezone));
        return dateThenTimeWithMillis.format(new Date(newValue));
    }

    public static String formatDate(long date) {
        return DateFormatFactory.getJustDate(DateFormatFactory.utc).format(new Date(date));
    }

    public static String formatIntervalMilliseconds(double intervalMilliseconds) {

        StringBuilder builder = new StringBuilder();

        if (intervalMilliseconds >= TimeUtils.minutes) {
            long asLong = (long) intervalMilliseconds;
            long minutes = asLong / TimeUtils.minutes;

            long remainingMillis = asLong % TimeUtils.minutes;
            double remainingSeconds = remainingMillis / (1000d);

            if (minutes == 1) {
                builder.append(minutes).append(" minute");
            } else {
                builder.append(minutes).append(" minutes");
            }
            if (remainingMillis > 0) {
                long remainingSecondsLong = (long) remainingSeconds;
                builder.append(" ").append(remainingSecondsLong);
                if (remainingSecondsLong == 1) {
                    builder.append(" second");
                } else {
                    builder.append(" seconds");
                }
            }
        } else if (intervalMilliseconds >= TimeUtils.seconds) {
            double seconds = intervalMilliseconds / 1000d;
            if (intervalMilliseconds >= TimeUtils.seconds) {

                if (seconds % 1 == 0) {
                    builder.append(StringUtils.format0dp(seconds));
                } else {
                    builder.append(StringUtils.format2dp(seconds));
                }

                if (seconds == 1) {
                    builder.append(" second");
                } else {
                    builder.append(" seconds");
                }
            } else {
                builder.append(StringUtils.format2dp(seconds)).append(" ms");
            }
        } else {
            builder.append((long) intervalMilliseconds).append(" ms");
        }

        return builder.toString();
    }

    public static String formatIntervalMillisecondsCompact(long intervalMilliseconds) {

        StringBuilder builder = new StringBuilder();

        if (intervalMilliseconds >= TimeUtils.minutes) {
            long asLong = (long) intervalMilliseconds;
            long minutes = asLong / TimeUtils.minutes;

            long remainingMillis = asLong % TimeUtils.minutes;
            long remainingSeconds = remainingMillis / (1000);

            if (minutes == 1) {
                builder.append(minutes).append("minute");
            } else {
                builder.append(minutes).append("minutes");
            }
            if (remainingMillis > 0) {
                long remainingSecondsLong = (long) remainingSeconds;
                builder.append(" ").append(remainingSecondsLong);
                if (remainingSecondsLong == 1) {
                    builder.append("second");
                } else {
                    builder.append("seconds");
                }
            }
        } else if (intervalMilliseconds >= TimeUtils.seconds) {
            long seconds = intervalMilliseconds / 1000;
            if (intervalMilliseconds >= TimeUtils.seconds) {
                builder.append(seconds);
                if (seconds == 1) {
                    builder.append("second");
                } else {
                    builder.append("seconds");
                }
            } else {
                builder.append(seconds).append("ms");
            }
        } else {
            builder.append((long) intervalMilliseconds).append("ms");
        }

        return builder.toString();

    }

    public static String formatIntervalNanoseconds(double interval) {
        TimeDetails makeNice = makeNice(interval);
        return makeNice.toString();
    }

    public static TimeDetails makeNice(double nanoSecondValue) {
        TimeUnit units = TimeUnit.NANOSECONDS;

        if (nanoSecondValue > 1000) {
            nanoSecondValue /= 1000;
            units = TimeUnit.MICROSECONDS;
        }

        if (nanoSecondValue > 1000) {
            nanoSecondValue /= 1000;
            units = TimeUnit.MILLISECONDS;
        }

        if (nanoSecondValue > 1000) {
            nanoSecondValue /= 1000;
            units = TimeUnit.SECONDS;
        }

        return new TimeDetails(nanoSecondValue, units);
    }

    public static String formatJustTime(long date) {
        return DateFormatFactory.getJustTime(DateFormatFactory.utc).format(new Date(date));
    }

    public static String formatJustTimeNoSeconds(long date) {
        return DateFormatFactory.getJustTimeNoSeconds(DateFormatFactory.utc).format(new Date(date));
    }

    public static String formatJustTimeNoSeconds(long date, TimeZone timezone) {
        return DateFormatFactory.getJustTimeNoSeconds(timezone).format(new Date(date));
    }

    public static Object formatUSDateThenTime(long time, String timezone) {
        SimpleDateFormat usDateThenTime = DateFormatFactory.getUSDateThenTime(TimeZone.getTimeZone(timezone));
        return usDateThenTime.format(new Date(time));

    }

    public static String formatUTC(long newValue) {
        DateFormat dateThenTimeWithMillis = DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.utc);
        dateThenTimeWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateThenTimeWithMillis.format(new Date(newValue));
    }

    public static Calendar getUTCCalendarForTime(String string) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(parseTime(string, DateFormatFactory.utc));
        return calendar;
    }

    public static long hours(int hours, int minutes) {
        return hours(hours) + minutes(minutes);
    }

    public static long hours(int hours, int minutes, int seconds) {
        return hours(hours) + minutes(minutes) + seconds(seconds);
    }

    public static long hoursFromNow(int hours) {
        return timeProvider.getTime() + hours(hours);
    }

    public static long hours(int i) {
        return hours * i;

    }

    public static boolean isInLast24Hours(long lastStateChangeTime) {
        long delta = Math.abs(timeProvider.getTime() - lastStateChangeTime);
        return delta < TimeUtils.days(1);
    }

    public static boolean isToday(long lastStateChangeTime) {
        long startOfDay = chunk(lastStateChangeTime, TimeUtils.days(1));
        long startOfToday = chunk(timeProvider.getTime(), TimeUtils.days(1));
        return startOfDay == startOfToday;
    }

    public static long chunk(long time, long chunkInterval) {
        return time - (time % chunkInterval);
    }

    public static long minutesFromNow(int minutes) {
        return timeProvider.getTime() + minutes(minutes);
    }

    public static long minutes(int i) {
        return minutes * i;
    }

    public static long months(int i) {
        return months * i;
    }

    public static long now() {
        return timeProvider.getTime();
    }

    public static boolean overlaps(long startA, long endA, long startB, long endB) {
        return startA < endB && startB <= endA;
    }

    public static boolean overlapsExclusive(long startA, long endA, long startB, long endB) {
        return startA < endB && startB < endA;
    }

    public static long parseTime(String string) {
        return parseTime(string, DateFormatFactory.utc);
    }

    public static long parseTime(String string, TimeZone timeZone) {

        boolean success = false;
        long parsed = 0;

        List<DateFormat> allFormats = DateFormatFactory.getAllFormats(timeZone);
        for (DateFormat dateFormat : allFormats) {
            try {
                parsed = dateFormat.parse(string).getTime();
                success = true;
                break;
            } catch (ParseException e) {
                // No luck
            }

        }

        if (!success) {
            throw new RuntimeException(StringUtils.format("Failed to parse '{}' using any of our date formats", string));
        }

        return parsed;
    }

    public static long parseTimePart(String newValue) {

        String[] split = newValue.split("[:|.]");

        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        int milliseconds = 0;

        if (split.length > 0) {
            hours = Integer.parseInt(split[0]);
        }

        if (split.length > 1) {
            minutes = Integer.parseInt(split[1]);
        }

        if (split.length > 2) {
            seconds = Integer.parseInt(split[2]);
        }

        if (split.length > 3) {
            milliseconds = Integer.parseInt(split[3]);
        }

        long result = TimeUtils.hours(hours) + TimeUtils.minutes(minutes) + TimeUtils.seconds(seconds) + milliseconds;
        return result;
    }

    public static long parseTimeUTC(String string) {

        boolean success = false;
        long parsed = 0;

        List<DateFormat> allFormats = DateFormatFactory.getAllFormats(DateFormatFactory.utc);
        for (DateFormat dateFormat : allFormats) {
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                parsed = dateFormat.parse(string).getTime();
                success = true;
                break;
            } catch (ParseException e) {
                // No luck
            }

        }

        if (!success) {
            throw new RuntimeException(StringUtils.format("Failed to parse '{}' using any of our date formats", string));
        }

        return parsed;
    }

    public static long secondsFromNow(int seconds) {
        return timeProvider.getTime() + seconds(seconds);
    }

    public static long seconds(int i) {
        return seconds * i;

    }

    public static void setTimeProvider(TimeProvider timeProvider) {
        TimeUtils.timeProvider = timeProvider;
    }

    public static long stripTimePart(long now) {
        return now - (now % (24 * 60 * 60 * 1000));
    }

    public static String toDailyFolderSplit(long time) {
        return DateFormatFactory.getDailyFolderSplit(DateFormatFactory.utc).format(new Date(time));
    }

    public static String toDateTimeString(long value) {
        return DateFormatFactory.getDateThenTime(DateFormatFactory.utc).format(new Date(value));
    }

    public static String toFileSafeOrderedNoMillis(long time) {
        return DateFormatFactory.getFileSafeOrdered(DateFormatFactory.utc).format(new Date(time));
    }

    public static long weeks(int i) {
        return weeks * i;

    }

    public static long whatsLeftAfterChunk(long time, long chunkInterval) {
        return time % chunkInterval;
    }

    public static Calendar calendar(TimeKey from) {
        return calendar(from.year, from.month, from.day, from.hour, from.minute, from.second, from.millisecond);
    }

    public static class TimeDetails {
        private TimeUnit timeUnit;
        private double value;
        private String abbriviatedUnits;

        public TimeDetails(double value, TimeUnit unit) {
            this.timeUnit = unit;
            this.value = value;

            if (unit == TimeUnit.NANOSECONDS) {
                abbriviatedUnits = "ns";
            } else if (unit == TimeUnit.MICROSECONDS) {
                abbriviatedUnits = "mus";
            } else if (unit == TimeUnit.MILLISECONDS) {
                abbriviatedUnits = "ms";
            } else if (unit == TimeUnit.SECONDS) {
                abbriviatedUnits = "s";
            }
        }

        public String getAbbriviatedUnits() {
            return abbriviatedUnits;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%.2f %s", value, abbriviatedUnits);

        }

    }

}
