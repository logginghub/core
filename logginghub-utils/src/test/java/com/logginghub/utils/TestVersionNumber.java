package com.logginghub.utils;

import org.junit.Test;

import com.logginghub.utils.VersionNumber;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;


public class TestVersionNumber {

    @Test public void testParse() {       
        VersionNumber number = VersionNumber.parse("1.2.3");
        assertThat(number.getMajor(), is(1));
        assertThat(number.getMinor(), is(2));
        assertThat(number.getBuild(), is(3));
        assertThat(number.toString(), is("1.2.3"));
        
        VersionNumber number2 = VersionNumber.parse("1.2");
        assertThat(number2.getMajor(), is(1));
        assertThat(number2.getMinor(), is(2));
        assertThat(number2.getBuild(), is(-1));
        assertThat(number2.toString(), is("1.2"));
        
        VersionNumber number3 = VersionNumber.parse("7");
        assertThat(number3.getMajor(), is(7));
        assertThat(number3.getMinor(), is(-1));
        assertThat(number3.getBuild(), is(-1));
        assertThat(number3.toString(), is("7"));
        
        VersionNumber number4 = VersionNumber.parse("1.6R5");
        assertThat(number4.getMajor(), is(1));
        assertThat(number4.getMinor(), is(6));
        assertThat(number4.getBuild(), is(-1));
        assertThat(number4.getComment(), is("R5"));
        assertThat(number4.toString(), is("1.6R5"));

        
    }
    
    @Test public void testCompareTo() { 
        assertThat(VersionNumber.parse("1.2.3").compareTo(VersionNumber.parse("1.2.4")), is(-1));
        assertThat(VersionNumber.parse("1.2.5").compareTo(VersionNumber.parse("1.2.4")), is(1));
        assertThat(VersionNumber.parse("1.2.5").compareTo(VersionNumber.parse("1.2.5")), is(0));
        assertThat(VersionNumber.parse("2.2.5").compareTo(VersionNumber.parse("1.2.5")), is(1));
        assertThat(VersionNumber.parse("2.2.5").compareTo(VersionNumber.parse("3.2.5")), is(-1));
        assertThat(VersionNumber.parse("2.2.5-RELEASE").compareTo(VersionNumber.parse("2.2.5-SNAPSHOT")), is(-1));
    }

    @Test
    public void testGetNextVersion() {
        assertThat(VersionNumber.parse("1.2.3").getNextBuildVersion().toString(), is("1.2.4"));
        assertThat(VersionNumber.parse("1.2.3").getNextMinorVersion().toString(), is("1.3.0"));
        assertThat(VersionNumber.parse("1.2.3").getNextMajorVersion().toString(), is("2.0.0"));
        
        assertThat(VersionNumber.parse("1.2.3-SNAPSHOT").getNextBuildVersion().toString(), is("1.2.4-SNAPSHOT"));
        assertThat(VersionNumber.parse("1.2.3-SNAPSHOT").getNextMinorVersion().toString(), is("1.3.0-SNAPSHOT"));
        assertThat(VersionNumber.parse("1.2.3-SNAPSHOT").getNextMajorVersion().toString(), is("2.0.0-SNAPSHOT"));
    }

}
