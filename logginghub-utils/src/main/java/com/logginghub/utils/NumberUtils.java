package com.logginghub.utils;

import com.logginghub.utils.logging.Logger;

public class NumberUtils {

    private static final Logger logger = Logger.getLoggerFor(NumberUtils.class);

    public static long tera(int i) {
        return i * 1000L * 1000L * 1000L * 1000L;
    }

    public static long peta(int i) {
        return i * 1000L * 1000L * 1000L * 1000L * 1000L;
    }

    public static long giga(int i) {
        return i * 1000L * 1000L * 1000L;
    }

    public static long mega(int i) {
        return i * 1000L * 1000L;
    }

    public static long kilo(int i) {
        return i * 1000L;
    }

    public static long parse(String size) {

        long bytes;
        boolean negative = false;

        String trimmed = size.trim();
        if (trimmed.length() == 0) {
            bytes = 0;
        }
        else {

            String numeric;
            String units;

            String[] split = trimmed.split(" ");
            if (split.length == 1) {

                if (trimmed.charAt(0) == '-') {
                    negative = true;
                    trimmed = StringUtils.after(trimmed, "-");
                }

                StringUtilsTokeniser st = new StringUtilsTokeniser(trimmed);

                numeric = st.nextUpToCharacterTypeChange();
                if (st.hasMore()) {
                    units = st.nextUpToCharacterTypeChange();
                }
                else {
                    units = "b";
                }

                // bytes = Long.parseLong(split[0]);
            }
            else {
                numeric = split[0];
                units = split[1];

            }

            double value = Double.parseDouble(numeric);

            long multiplier;

            char charAt = Character.toLowerCase(units.charAt(0));
            switch (charAt) {
                case 'b':
                    if (Math.floor(value) != value) {
                        throw new IllegalArgumentException("You can't have a fractional number");
                    }
                    multiplier = 1;
                    break;
                case 'k':
                    multiplier = 1000L;
                    break;
                case 'm':
                    multiplier = 1000L * 1000L;
                    break;
                case 'g':
                    multiplier = 1000L * 1000L * 1000L;
                    break;
                case 't':
                    multiplier = 1000L * 1000L * 1000L * 1000L;
                    break;
                case 'p':
                    multiplier = 1000L * 1000L * 1000L * 1000L * 1000L;
                    break;
                default:
                    logger.warn("Unparsable data size '{}' - assuming non-multiplier", size);
                    multiplier = 1;
                    break;
            }

            bytes = (long) (value * multiplier);

        }

        if (negative) {
            bytes *= -1;
        }

        return bytes;

    }
    
    public static int parseInt(String size) {
        return (int)parse(size);
    }

}
