package com.logginghub.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.awt.Color;

import org.junit.Test;

import com.logginghub.utils.ColourUtils;

public class TestColourUtils {

    @Test public void testToHtmlHex() {
        assertThat(ColourUtils.toHtmlHex(Color.black), is("#000000"));
        assertThat(ColourUtils.toHtmlHex(Color.white), is("#ffffff"));
        assertThat(ColourUtils.toHtmlHex(Color.red), is("#ff0000"));
        assertThat(ColourUtils.toHtmlHex(Color.green), is("#00ff00"));
        assertThat(ColourUtils.toHtmlHex(Color.blue), is("#0000ff"));
    }

    @Test public void testToHex() {
        assertThat(ColourUtils.toHex(Color.black), is("000000"));
        assertThat(ColourUtils.toHex(Color.white), is("ffffff"));
        assertThat(ColourUtils.toHex(Color.red), is("ff0000"));
        assertThat(ColourUtils.toHex(Color.green), is("00ff00"));
        assertThat(ColourUtils.toHex(Color.blue), is("0000ff"));
    }

    
    @Test public void testParseColor() {
        assertColour(ColourUtils.parseColor("black"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("white"), 255, 255, 255);
        assertColour(ColourUtils.parseColor("000000"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("FF00FF"), 255, 0, 255);
        assertColour(ColourUtils.parseColor("0x000000"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("#000000"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("00 00 00"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("0,0,0"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("0,0,0"), 0, 0, 0);
        assertColour(ColourUtils.parseColor("PeachPuff"), Integer.parseInt("FF", 16), Integer.parseInt("DA", 16), Integer.parseInt("B9", 16));
    }

    private void assertColour(Color parseColor, int r, int g, int b) {
        assertThat(parseColor.getRed(), is(r));
        assertThat(parseColor.getGreen(), is(g));
        assertThat(parseColor.getBlue(), is(b));
    }

}
