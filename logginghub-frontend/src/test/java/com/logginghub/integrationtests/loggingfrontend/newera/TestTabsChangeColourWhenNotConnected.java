package com.logginghub.integrationtests.loggingfrontend.newera;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.awt.Color;
import java.io.IOException;

import javax.swing.JTabbedPane;

import org.fest.swing.fixture.JTabbedPaneFixture;
import org.junit.Test;

import com.logginghub.utils.ColourUtils;
import com.logginghub.integrationtests.loggingfrontend.helpers.NewEraBase;

public class TestTabsChangeColourWhenNotConnected extends NewEraBase {
    
    @Test public void testWithTwoEnvironments() throws IOException {
        JTabbedPaneFixture tabbedPane = frameFixture.tabbedPane("mainTabbedPane");
        assertThat(tabbedPane.tabTitles(), is(new String[] { "Environment 1", "Environment 2" }));

        JTabbedPane target = tabbedPane.target;
        Color backgroundAt1 = target.getBackgroundAt(0);
        Color backgroundAt2 = target.getBackgroundAt(1);

        Color foregroundAt1 = target.getForegroundAt(0);
        Color foregroundAt2 = target.getForegroundAt(1);

        // This is a bit lazy, but the socket connection is async and could be
        // disconnected or trying to connect, so there is no telling what the
        // actual colour would be. But as longs as its not default...
        assertThat(ColourUtils.toHex(foregroundAt1), is(not("333333")));
        assertThat(ColourUtils.toHex(foregroundAt2), is(not("333333")));

        assertThat(ColourUtils.toHex(backgroundAt1), is(not("b8cfe5")));
        assertThat(ColourUtils.toHex(backgroundAt2), is(not("b8cfe5")));

        swingFrontEnd.dispose();
        frameFixture.cleanUp();
    }
}
