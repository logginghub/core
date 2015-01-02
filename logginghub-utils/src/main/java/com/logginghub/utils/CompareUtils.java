package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class CompareUtils {
    public static int compareInts(int a, int b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

    public static int compareBytes(byte a, byte b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

    public static int compareShorts(short a, short b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

    public static int compareLongs(long a, long b) {
        return (a < b ? -1 : (a == b ? 0 : 1));
    }

    public static int compareFloats(float a, float b) {
        return Float.compare(a, b);
    }

    public static int compareDoubles(double a, double b) {
        return Double.compare(a, b);
    }

    public static int compareStrings(String a, String b) {
        return a.compareTo(b);
    }

    public static int compareDates(Date a, Date b) {
        return a.compareTo(b);
    }

    @SuppressWarnings("unchecked") public static <T> int compare(Comparable<T> a, Comparable<T> b) {
        return a.compareTo((T) b);
    }

    public static int compare(Object a, Object b) {
        if (a instanceof String && b instanceof String) {
            return compareStrings((String) a, (String) b);
        }
        else if (a instanceof Long && b instanceof Long) {
            return compareLongs(((Long) a).longValue(), ((Long) b).longValue());
        }
        else if (a instanceof Integer && b instanceof Integer) {
            return compareInts(((Integer) a).intValue(), ((Integer) b).intValue());
        }
        else if (a instanceof Short && b instanceof Short) {
            return compareShorts(((Short) a).shortValue(), ((Short) b).shortValue());
        }
        else if (a instanceof Byte && b instanceof Byte) {
            return compareBytes(((Byte) a).byteValue(), ((Byte) b).byteValue());
        }
        else if (a instanceof Character && b instanceof Character) {
            return compareInts(((Character) a).charValue(), ((Character) b).charValue());
        }
        else if (a instanceof Date && b instanceof Date) {
            return compareDates((Date) a, (Date) b);
        }
        else if (a instanceof Float && b instanceof Float) {
            return compareFloats(((Float) a).floatValue(), ((Float) b).floatValue());
        }
        else if (a instanceof Enum<?> && b instanceof Enum<?>) {
            return compareEnums((Enum<?>) a, (Enum<?>) b);
        }
        else if (a instanceof Class<?> && b instanceof Class<?>) {
            return compareClasses((Class<?>) a, (Class<?>) b);
        }
        else if (a instanceof Double && b instanceof Double) {
            return compareDoubles(((Double) a).doubleValue(), ((Double) b).doubleValue());
        }
        else if (a == null && b != null) {
            return 1;
        }
        else if (a != null && b == null) {
            return -1;
        }
        else if (a == null || b == null) {
            return 0;
        }
        else {
            throw new RuntimeException(String.format("Couldn't find any common class to use to compare '%s' and '%s'", a, b));
        }
    }

    private static int compareClasses(Class<?> a, Class<?> b) {
        return compareStrings(a.getName(), b.getName());
    }

    private static int compareEnums(Enum<?> a, Enum<?> b) {
        return CompareUtils.start().add(a.getClass(), b.getClass()).add(a.ordinal(), b.ordinal()).compare();
    }

    public static class CompareBuilder {

        private List<Triple<Object, Object, Comparator>> triples = new ArrayList<Triple<Object, Object, Comparator>>();

        public CompareBuilder add(Object a, Object b) {
            triples.add(new Triple<Object, Object, Comparator>(a, b, null));
            return this;
        }

        public CompareBuilder add(Object a, Object b, Comparator comp) {
            triples.add(new Triple<Object, Object, Comparator>(a, b, comp));
            return this;
        }
        
        public int compare() {

            int result = 0;

            for (Triple<Object, Object, Comparator> triple : triples) {

                if (triple.getC() == null) {
                    result = CompareUtils.compare(triple.getA(), triple.getB());
                }
                else {
                    result = triple.getC().compare(triple.getA(), triple.getB());
                }

                if (result != 0) {
                    break;
                }
            }

            return result;

        }
    }

    public static CompareBuilder add(Object a, Object b) {
        CompareBuilder compareBuilder = new CompareBuilder();
        compareBuilder.add(a, b);
        return compareBuilder;
    }

    public static CompareBuilder start() {
        return new CompareBuilder();

    }
}
