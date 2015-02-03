package com.logginghub.logging.frontend.views.logeventdetail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.awt.Color;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventFactory;
import com.logginghub.logging.filters.MessageRegexFilter;

public class TestPhraseHighlighter {

    @Test public void test_regex_filter() {

        PhraseHighlighter highlighter = new PhraseHighlighter(new MessageRegexFilter(".*phrase.*"));
        highlighter.setHighlightBackgroundColour(Color.pink);
        highlighter.setHighlightForegroundColour(Color.white);

        assertThat(highlighter.getFilter(), is(instanceOf(MessageRegexFilter.class)));

        int rowIndex = 0;
        int columnIndex = 0;

        boolean isSelected = false;
        boolean isBookmarked = true;

        DefaultLogEvent matchingEvent = LogEventFactory.createFullLogEvent1();
        DefaultLogEvent notMatchingEvent = LogEventFactory.createFullLogEvent1();

        matchingEvent.setMessage("This message contains the phrase");
        notMatchingEvent.setMessage("This message contains nothing");

        HighlightSettings matchingColours = new HighlightSettings(null, null);
        highlighter.updateSettings(matchingColours, rowIndex, columnIndex, isSelected, isBookmarked, matchingEvent);
        assertThat(matchingColours.getBackground(), is(Color.pink));
        assertThat(matchingColours.getForeground(), is(Color.white));

        HighlightSettings notMatchinColours = new HighlightSettings(null, null);
        highlighter.updateSettings(notMatchinColours, rowIndex, columnIndex, isSelected, isBookmarked, notMatchingEvent);
        assertThat(notMatchinColours.getBackground(), is(nullValue()));
        assertThat(notMatchinColours.getForeground(), is(nullValue()));

        assertThat(highlighter.isInterested(rowIndex, columnIndex, isSelected, isBookmarked, matchingEvent), is(true));
        assertThat(highlighter.isInterested(rowIndex, columnIndex, isSelected, isBookmarked, notMatchingEvent), is(false));

    }

    @Test public void test_passes_filter_not_selected() {
        PhraseHighlighter highlighter = new PhraseHighlighter("phrase");
        highlighter.setHighlightBackgroundColour(Color.red);

        int rowIndex = 0;
        int columnIndex = 0;

        boolean isSelected = false;
        boolean isBookmarked = true;

        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage("This message contains the phrase");

        HighlightSettings colours = new HighlightSettings(null, null);
        highlighter.updateSettings(colours, rowIndex, columnIndex, isSelected, isBookmarked, event);

        Color background = colours.getBackground();
        Color foreground = colours.getForeground();

        assertThat(background, is(Color.red));
        assertThat(foreground, is(Color.black));

        boolean interested = highlighter.isInterested(rowIndex, columnIndex, isSelected, isBookmarked, event);
        assertThat(interested, is(true));
    }

    @Test public void test_fails_filter() {
        PhraseHighlighter highlighter = new PhraseHighlighter("phrase");
        highlighter.setHighlightBackgroundColour(Color.red);

        int rowIndex = 0;
        int columnIndex = 0;

        boolean isSelected = false;
        boolean isBookmarked = true;

        DefaultLogEvent event = LogEventFactory.createFullLogEvent1();
        event.setMessage("This message contains nothing");

        HighlightSettings colours = new HighlightSettings(null, null);
        highlighter.updateSettings(colours, rowIndex, columnIndex, isSelected, isBookmarked, event);

        Color background = colours.getBackground();
        Color foreground = colours.getForeground();

        assertThat(background, is(nullValue()));
        assertThat(foreground, is(nullValue()));

        boolean interested = highlighter.isInterested(rowIndex, columnIndex, isSelected, isBookmarked, event);
        assertThat(interested, is(false));
    }

}
