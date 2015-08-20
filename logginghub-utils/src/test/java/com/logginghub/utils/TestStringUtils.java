package com.logginghub.utils;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TestStringUtils {

    private static final long OneKB = 1024L;

    @Test
    public void testStripQuotes() {
        assertThat(StringUtils.stripQuotes(""), is(""));

        assertThat(StringUtils.stripQuotes("foo"), is("foo"));
        assertThat(StringUtils.stripQuotes("\"foo\""), is("foo"));
        assertThat(StringUtils.stripQuotes("'foo'"), is("foo"));

        assertThat(StringUtils.stripQuotes("\"foo"), is("\"foo"));
        assertThat(StringUtils.stripQuotes("'foo"), is("'foo"));
        assertThat(StringUtils.stripQuotes("foo\""), is("foo\""));
        assertThat(StringUtils.stripQuotes("foo'"), is("foo'"));
    }

    @Test
    public void testAfter() {
        assertThat(StringUtils.after("this is my string", "my"), is(" string"));
        assertThat(StringUtils.after("", ""), is(""));
        assertThat(StringUtils.after("this is my string", ""), is("this is my string"));
        assertThat(StringUtils.after("this is my string", "t"), is("his is my string"));
        assertThat(StringUtils.after("this is my string", "g"), is(""));
        assertThat(StringUtils.after("this is my string", "foo"), is(""));
    }

    @Test
    public void testIsStringNumeric() {
        assertThat(StringUtils.isStringNumeric("1231232"), is(true));
        assertThat(StringUtils.isStringNumeric("-1231232"), is(true));
        assertThat(StringUtils.isStringNumeric("-0.00123"), is(true));
        assertThat(StringUtils.isStringNumeric("1230.00123"), is(true));

        assertThat(StringUtils.isStringNumeric("a1231232"), is(false));
        assertThat(StringUtils.isStringNumeric("-1231!232"), is(false));
        assertThat(StringUtils.isStringNumeric("-0.00123z"), is(false));
        assertThat(StringUtils.isStringNumeric("1230,,,,00123"), is(false));

    }

    @Test
    public void testBetween() {
        assertThat(StringUtils.between("", "", ""), is(""));
        assertThat(StringUtils.between("abba", "a", "a"), is("bb"));
        assertThat(StringUtils.between("abba", "b", "b"), is(""));
        assertThat(StringUtils.between("abba", "abba", "b"), is(""));

        assertThat(StringUtils.between("This: grab this: not this", "This: ", ":"), is("grab this"));
        assertThat(StringUtils.between("This : grab this: not this", "This : ", ":"), is("grab this"));

        assertThat(StringUtils.between("log.txt", "log.", ".txt"), is(nullValue()));

    }

    @Test
    public void testParseSize() {

        // assertThat(StringUtils.parseSize("123"), is(123L));

        assertThat(StringUtils.parseSize("123K"), is(123 * OneKB));
        assertThat(StringUtils.parseSize("123M"), is(123 * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123G"), is(123 * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123T"), is(123 * OneKB * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123P"), is(123 * OneKB * OneKB * OneKB * OneKB * OneKB));

        assertThat(StringUtils.parseSize("123Kb"), is(123 * OneKB));
        assertThat(StringUtils.parseSize("123Mb"), is(123 * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123Gb"), is(123 * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123Tb"), is(123 * OneKB * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123Pb"), is(123 * OneKB * OneKB * OneKB * OneKB * OneKB));

        assertThat(StringUtils.parseSize("123KB"), is(123 * OneKB));
        assertThat(StringUtils.parseSize("123MB"), is(123 * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123GB"), is(123 * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123TB"), is(123 * OneKB * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123PB"), is(123 * OneKB * OneKB * OneKB * OneKB * OneKB));

        assertThat(StringUtils.parseSize("123 Kb"), is(123 * OneKB));
        assertThat(StringUtils.parseSize("123 Mb"), is(123 * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 Gb"), is(123 * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 Tb"), is(123 * OneKB * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 Pb"), is(123 * OneKB * OneKB * OneKB * OneKB * OneKB));

        assertThat(StringUtils.parseSize("123 KB"), is(123 * OneKB));
        assertThat(StringUtils.parseSize("123 MB"), is(123 * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 GB"), is(123 * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 TB"), is(123 * OneKB * OneKB * OneKB * OneKB));
        assertThat(StringUtils.parseSize("123 PB"), is(123 * OneKB * OneKB * OneKB * OneKB * OneKB));

    }

    @Test
    public void testLeadingNumber() {
        assertThat(StringUtils.leadingNumber("123asf"), is("123"));
        assertThat(StringUtils.leadingNumber("1asf"), is("1"));
        assertThat(StringUtils.leadingNumber("00asf11"), is("00"));

        assertThat(StringUtils.leadingNumber("1.23asf"), is("1.23"));
        assertThat(StringUtils.leadingNumber("1,345asf"), is("1,345"));
        assertThat(StringUtils.leadingNumber("23,340.01asf11"), is("23,340.01"));
    }

    @Test
    public void testTrailingNumber() {
        assertThat(StringUtils.trailingNumber("asf123"), is("123"));
        assertThat(StringUtils.trailingNumber("asf1"), is("1"));
        assertThat(StringUtils.trailingNumber("11asf00"), is("00"));

        assertThat(StringUtils.trailingNumber("asf1.23"), is("1.23"));
        assertThat(StringUtils.trailingNumber("asf1,345"), is("1,345"));
        assertThat(StringUtils.trailingNumber("11asf23,340.01"), is("23,340.01"));
    }

    @Test
    public void testBeforeLast() {
        assertThat(StringUtils.beforeLast("abc/def/ghi", "/"), is("abc/def"));
        assertThat(StringUtils.beforeLast("abc/def/ghi", "b"), is("a"));
        assertThat(StringUtils.beforeLast("abc/def/ghi", "/ghi"), is("abc/def"));
        assertThat(StringUtils.beforeLast("", ""), is(""));
    }

    @Test
    public void testRemoveAllFromStart() {
        assertThat(StringUtils.removeAllFromStart("abcabcdef", "a"), is("bcabcdef"));
        assertThat(StringUtils.removeAllFromStart("abcabcdef", "ab"), is("cabcdef"));
        assertThat(StringUtils.removeAllFromStart("abcabcdef", "abc"), is("def"));
    }

    @Test
    public void testAfterLast() {
        assertThat(StringUtils.afterLast("", ""), is(""));
        assertThat(StringUtils.afterLast("a/b/c", "/"), is("c"));
        assertThat(StringUtils.afterLast("a/b/c", "a"), is("/b/c"));
    }

    @Test
    public void testFormatPadding() {
        assertThat(StringUtils.format("\\{5}\\d{}", 1), is("{5}\\d1"));
        assertThat(StringUtils.format("\\{5}{}", 1), is("{5}1"));
        assertThat(StringUtils.format("{}\\d\\{4}_\\d\\{2}_\\d\\{2}_\\d\\{6}{}", "hub.", ".log"), is("hub.\\d{4}_\\d{2}_\\d{2}_\\d{6}.log"));

        assertThat(StringUtils.format("{5}"), is("{5}"));

        assertThat(StringUtils.format("_{ 5 }_{ 5 }_", "a", "b"), is("_    a_    b_"));
        assertThat(StringUtils.format("{5}_{5}", "a", "b"), is("    a_    b"));

        assertThat(StringUtils.format("{5}", ""), is("     "));
        assertThat(StringUtils.format("{5}", "a"), is("    a"));
        assertThat(StringUtils.format("{5}", "ab"), is("   ab"));
        assertThat(StringUtils.format("{5}", "abc"), is("  abc"));
        assertThat(StringUtils.format("{5}", "abcd"), is(" abcd"));
        assertThat(StringUtils.format("{5}", "abcde"), is("abcde"));
        assertThat(StringUtils.format("{5}", "abcdef"), is("abcde"));

        assertThat(StringUtils.format("{-5}", ""), is("     "));
        assertThat(StringUtils.format("{-5}", "a"), is("a    "));
        assertThat(StringUtils.format("{-5}", "ab"), is("ab   "));
        assertThat(StringUtils.format("{-5}", "abc"), is("abc  "));
        assertThat(StringUtils.format("{-5}", "abcd"), is("abcd "));
        assertThat(StringUtils.format("{-5}", "abcde"), is("abcde"));
        assertThat(StringUtils.format("{-5}", "abcdef"), is("abcde"));

        assertThat(StringUtils.format("{-5 }", "a"), is("a    "));
        assertThat(StringUtils.format("{ -5 }", "a"), is("a    "));
        assertThat(StringUtils.format("{ - 5 }", "a"), is("a    "));

        assertThat(StringUtils.format("{T}", 0), is("01/01/1970 00:00:00.000"));
        assertThat(StringUtils.format("{10T}", 0), is("01/01/1970"));

        assertThat(StringUtils.format("{N}", 1234), is("1,234"));
        assertThat(StringUtils.format("{N}", 1234.56), is("1,234.56"));

        assertThat(StringUtils.format("{10N}", 1234), is("     1,234"));
        assertThat(StringUtils.format("{10N}", 1234.56), is("  1,234.56"));
        assertThat(StringUtils.format("{-10N}", 1234), is("1,234     "));
        assertThat(StringUtils.format("{-10N}", 1234.56), is("1,234.56  "));
    }

    @Test
    public void testFormat() {
        assertThat(StringUtils.format("a{}c", "b"), is("abc"));
        assertThat(StringUtils.format("{}", 10), is("10"));
        assertThat(StringUtils.format("{"), is("{"));
        assertThat(StringUtils.format("}"), is("}"));
        assertThat(StringUtils.format("{}"), is("{}"));

        String newline = System.getProperty("line.separator");
        assertThat(StringUtils.format("a%n{}%nc", "b"), is("a" + newline + "b" + newline + "c"));
    }

    @Test
    public void testRandomString() {

        assertThat(StringUtils.randomString(1).length(), is(1));
        assertThat(StringUtils.randomString(100).length(), is(100));
        assertThat(StringUtils.randomString(0).length(), is(0));
        assertThat(StringUtils.randomString(5), is(not(StringUtils.randomString(5))));

    }

    @Test
    public void testPadLeft() {
        assertThat(StringUtils.padLeft("abc", 0), is("abc"));
        assertThat(StringUtils.padLeft("abc", 1), is("abc"));
        assertThat(StringUtils.padLeft("abc", 2), is("abc"));
        assertThat(StringUtils.padLeft("abc", 3), is("abc"));
        assertThat(StringUtils.padLeft("abc", 4), is(" abc"));
        assertThat(StringUtils.padLeft("abc", 5), is("  abc"));
    }

    @Test
    public void testPadRight() {
        assertThat(StringUtils.padRight("abc", 0), is("abc"));
        assertThat(StringUtils.padRight("abc", 1), is("abc"));
        assertThat(StringUtils.padRight("abc", 2), is("abc"));
        assertThat(StringUtils.padRight("abc", 3), is("abc"));
        assertThat(StringUtils.padRight("abc", 4), is("abc "));
        assertThat(StringUtils.padRight("abc", 5), is("abc  "));

    }

    @Test
    public void testPaddingString() {

        assertThat(StringUtils.paddingString("abc", 0, ' ', true), is("abc"));
        assertThat(StringUtils.paddingString("abc", 1, ' ', true), is("abc"));
        assertThat(StringUtils.paddingString("abc", 2, ' ', true), is("abc"));
        assertThat(StringUtils.paddingString("abc", 3, ' ', true), is("abc"));
        assertThat(StringUtils.paddingString("abc", 4, ' ', true), is(" abc"));
        assertThat(StringUtils.paddingString("abc", 5, ' ', true), is("  abc"));

        assertThat(StringUtils.paddingString("abc", 0, ' ', false), is("abc"));
        assertThat(StringUtils.paddingString("abc", 1, ' ', false), is("abc"));
        assertThat(StringUtils.paddingString("abc", 2, ' ', false), is("abc"));
        assertThat(StringUtils.paddingString("abc", 3, ' ', false), is("abc"));
        assertThat(StringUtils.paddingString("abc", 4, ' ', false), is("abc "));
        assertThat(StringUtils.paddingString("abc", 5, ' ', false), is("abc  "));

        assertThat(StringUtils.paddingString("abc", 5, '-', false), is("abc--"));

    }

    @Test
    public void testSplitAroundLast() throws Exception {
        assertThat(StringUtils.splitAroundLast("abc=def", "=").getA(), is("abc"));
        assertThat(StringUtils.splitAroundLast("abc=def", "=").getB(), is("def"));
        assertThat(StringUtils.splitAroundLast("abc=def", "!"), is(nullValue()));

        assertThat(StringUtils.splitAroundLast("=def", "=").getA(), is(""));
        assertThat(StringUtils.splitAroundLast("=def", "=").getB(), is("def"));

        assertThat(StringUtils.splitAroundLast("abc=", "=").getA(), is("abc"));
        assertThat(StringUtils.splitAroundLast("abc=", "=").getB(), is(""));
    }

    @Test
    public void testIsNumeric() throws Exception {
        assertThat(StringUtils.isNumeric(""), is(false));
        assertThat(StringUtils.isNumeric("a"), is(false));
        assertThat(StringUtils.isNumeric("a1"), is(false));
        assertThat(StringUtils.isNumeric("-23.43e10b"), is(false));

        assertThat(StringUtils.isNumeric("1"), is(true));
        assertThat(StringUtils.isNumeric("2.4"), is(true));
        assertThat(StringUtils.isNumeric("-123"), is(true));
        assertThat(StringUtils.isNumeric("-1233.2123"), is(true));
        assertThat(StringUtils.isNumeric("1e-6"), is(true));
        assertThat(StringUtils.isNumeric("-23.43e10"), is(true));
        assertThat(StringUtils.isNumeric("0.00001"), is(true));
        assertThat(StringUtils.isNumeric("666"), is(true));
    }

    @Test
    public void testToList() throws Exception {
        List<String> list = StringUtils.toList(" r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa\r\n");
        assertThat(list.get(0), is("r"));
        assertThat(list.get(1), is("b"));
        assertThat(list.get(2), is("swpd"));
        assertThat(list.get(3), is("free"));
        assertThat(list.get(4), is("buff"));
        assertThat(list.get(5), is("cache"));
        assertThat(list.get(6), is("si"));
        assertThat(list.get(7), is("so"));
        assertThat(list.get(8), is("bi"));
        assertThat(list.get(9), is("bo"));
        assertThat(list.get(10), is("in"));
        assertThat(list.get(11), is("cs"));
        assertThat(list.get(12), is("us"));
        assertThat(list.get(13), is("sy"));
        assertThat(list.get(14), is("id"));
        assertThat(list.get(15), is("wa"));

        list = StringUtils.toList(" r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa");
        assertThat(list.get(0), is("r"));
        assertThat(list.get(1), is("b"));
        assertThat(list.get(2), is("swpd"));
        assertThat(list.get(3), is("free"));
        assertThat(list.get(4), is("buff"));
        assertThat(list.get(5), is("cache"));
        assertThat(list.get(6), is("si"));
        assertThat(list.get(7), is("so"));
        assertThat(list.get(8), is("bi"));
        assertThat(list.get(9), is("bo"));
        assertThat(list.get(10), is("in"));
        assertThat(list.get(11), is("cs"));
        assertThat(list.get(12), is("us"));
        assertThat(list.get(13), is("sy"));
        assertThat(list.get(14), is("id"));
        assertThat(list.get(15), is("wa"));

    }

    @Test
    public void testEnvironmentReplacement() throws Exception {

        System.setProperty("variable1", "value_1");
        System.setProperty("variable2", "value_2");
        System.setProperty("variable3", "value_3");

        if (OSUtils.isWindows()) {
            // See if it picks up env virables too
            assertThat(StringUtils.environmentReplacement("${path}"), is(not("${path}")));
        } else if (OSUtils.isNixVariant()) {
            assertThat(StringUtils.environmentReplacement("${SHELL}"), is(not("${SHELL}")));
        }

        assertThat(StringUtils.environmentReplacement("${variable1}"), is("value_1"));
        assertThat(StringUtils.environmentReplacement("before ${variable1}"), is("before value_1"));
        assertThat(StringUtils.environmentReplacement("${variable1} after"), is("value_1 after"));
        assertThat(StringUtils.environmentReplacement("before ${variable1} after"), is("before value_1 after"));
        assertThat(StringUtils.environmentReplacement("${variable1}${variable2}${variable3}"), is("value_1value_2value_3"));
        assertThat(StringUtils.environmentReplacement(" a ${variable1} b ${variable2} c ${variable3} "), is(" a value_1 b value_2 c value_3 "));

        assertThat(StringUtils.environmentReplacement("${not1}"), is("${not1}"));
        assertThat(StringUtils.environmentReplacement(" a ${not1} b ${not2} c ${not3} "), is(" a ${not1} b ${not2} c ${not3} "));

    }

    @Test
    public void testCountOccurances() throws Exception {
        assertThat(StringUtils.countOccurances("aaaa", "aa"), is(2));
        assertThat(StringUtils.countOccurances("aahelloaa", "hello"), is(1));
        assertThat(StringUtils.countOccurances("hello", "hello"), is(1));
    }

    @Test
    public void testCountLeading() throws Exception {
        assertThat(StringUtils.countLeading("    foo", ' '), is(4));
        assertThat(StringUtils.countLeading("foo", ' '), is(0));
        assertThat(StringUtils.countLeading(" ", ' '), is(1));
        assertThat(StringUtils.countLeading("", ' '), is(0));
    }

    @Test
    public void testMatches() throws Exception {
        assertThat(StringUtils.matches("hub.1970_01_01_000000.log", "{}\\d\\{4}_\\d\\{2}_\\d\\{2}_\\d\\{6}{}", "hub.", ".log"), is(true));
        assertThat(StringUtils.matches("hub.1970_01_01_000000.0.log", "{}\\d\\{4}_\\d\\{2}_\\d\\{2}_\\d\\{6}{}", "hub.", ".log"), is(false));

        assertThat(StringUtils.matches("aa bb cc", "aa bb cc"), is(true));

        assertThat(StringUtils.matches("aa bb cc", "aa bb [cc]{2}"), is(true));
        assertThat(StringUtils.matches("aa bb cc", "aa bb [c]{2}"), is(true));

        assertThat(StringUtils.matches("aa bb dd", "aa bb (cc)?dd"), is(true));
        assertThat(StringUtils.matches("aa bb cc dd", "aa bb (cc )?dd"), is(true));

        assertThat(StringUtils.matches("hub.1970_01_01_000000.log", "{}\\.\\d\\{4}_\\d\\{2}_\\d\\{2}_\\d\\{6}{}", "hub", ".log"), is(true));

        String fullTimeRegex = "{}\\.\\d\\{4}_\\d\\{2}_\\d\\{2}_\\d\\{6}(\\.\\d+)?{}";
        assertThat(StringUtils.matches("hub.1970_01_01_000000.log", fullTimeRegex, "hub", ".log"), is(true));
        assertThat(StringUtils.matches("hub.1970_01_01_000000.0.log", fullTimeRegex, "hub", ".log"), is(true));
        assertThat(StringUtils.matches("hub.1970_01_01_000000.10.log", fullTimeRegex, "hub", ".log"), is(true));

        String logdataRegex = ".*\\.logdata(\\.\\d+)?";
        assertThat(StringUtils.matches("file.logdata", logdataRegex), is(true));
        assertThat(StringUtils.matches("file.logdata.", logdataRegex), is(false));
        assertThat(StringUtils.matches("file.logdata.writing", logdataRegex), is(false));
        assertThat(StringUtils.matches("file.logdata.1", logdataRegex), is(true));
        assertThat(StringUtils.matches("file.logdata.1123", logdataRegex), is(true));

        String groupRegex = ".*\\.(\\d\\{8})\\.(\\d\\{6})\\..*";
        assertThat(StringUtils.matches("hub.binary.20140311.145000.logdata.0", groupRegex), is(true));

        assertThat(StringUtils.matches("prefix.19700101.000000.0.postfix", "{}\\.\\d\\{8}\\.\\d\\{6}(\\.\\d+)?\\.{}", "prefix", "postfix"), is(true));

    }

    @Test
    public void testMatchGroups() throws Exception {
        String groupRegex = ".*?(\\d\\{8})\\.(\\d\\{6})\\..*";
        List<String> matchGroups = StringUtils.matchGroups("hub.binary.20140311.145000.logdata.0", groupRegex);
        assertThat(matchGroups.get(0), is("20140311"));
        assertThat(matchGroups.get(1), is("145000"));

        matchGroups = StringUtils.matchGroups("20140311.145000.logdata.0", groupRegex);
        assertThat(matchGroups.get(0), is("20140311"));
        assertThat(matchGroups.get(1), is("145000"));
    }

    @Test
    public void testRemoveLast() throws Exception {
        assertThat(StringUtils.removeLast("ab", 1), is("a"));
        assertThat(StringUtils.removeLast("ab", 0), is("ab"));
        assertThat(StringUtils.removeLast("ab", 2), is(""));
        assertThat(StringUtils.removeLast("ab", 3), is(""));
        assertThat(StringUtils.removeLast("", 0), is(""));
        assertThat(StringUtils.removeLast("", 10), is(""));
    }
}
