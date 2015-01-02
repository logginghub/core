package com.logginghub.utils;

import java.text.NumberFormat;

import com.logginghub.utils.logging.Logger;

public class ByteUtils {

    private static final Logger logger = Logger.getLoggerFor(ByteUtils.class);

    public static long terabytes(int i) {
        return i * 1024L * 1024L * 1024L * 1024L;
    }

    public static long petabytes(int i) {
        return i * 1024L * 1024L * 1024L * 1024L * 1024L;
    }

    public static long gigabytes(int i) {
        return i * 1024L * 1024L * 1024L;
    }

    public static long megabytes(int i) {
        return i * 1024L * 1024L;
    }

    public static long kilobytes(int i) {
        return i * 1024L;
    }

    public static long bytes(int i) {
        return i;

    }

    public static String format(long dataFolderSizeWatermark) {
        return StringUtils.formatBytes(dataFolderSizeWatermark);
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
                        throw new IllegalArgumentException("You can't have a fractional number of bytes");
                    }
                    multiplier = 1;
                    break;
                case 'k':
                    multiplier = 1024L;
                    break;
                case 'm':
                    multiplier = 1024L * 1024L;
                    break;
                case 'g':
                    multiplier = 1024L * 1024L * 1024L;
                    break;
                case 't':
                    multiplier = 1024L * 1024L * 1024L * 1024L;
                    break;
                case 'p':
                    multiplier = 1024L * 1024L * 1024L * 1024L * 1024L;
                    break;
                default:
                    logger.warn("Unparsable data size '{}' - assuming bytes", size);
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

    public static String formatKB(Double value) {
        if (value != null) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
            return nf.format(value / 1024d);
        }
        else {
            return "?";
        }
    }

    public static String formatMB(Double value) {
        if (value != null) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setMinimumFractionDigits(1);
            return nf.format(value / 1024d / 1024d);
        }
        else {
            return "?";
        }

    }

}
