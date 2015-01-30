package com.logginghub.logging.repository;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.modules.LogEventFixture1;
import com.logginghub.utils.Bucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FixedTimeProvider;
import com.logginghub.utils.sof.SerialisableObject;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofException;
import com.logginghub.utils.sof.SofSerialiser;
import org.junit.Test;

import java.io.EOFException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

public class TestInMemorySofBlock {

    @Test public void test_using_event_times() throws SofException, EOFException {

        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration);
        block.write(LogEventFixture1.event5);

        assertThat(block.getStartTime(), is(1000L));
        assertThat(block.getEndTime(), is(1000L));
        assertThat(block.getCompressLength(), is(0L));

        block.write(LogEventFixture1.event10);

        assertThat(block.getStartTime(), is(1000L));
        assertThat(block.getEndTime(), is(1500L));
        assertThat(block.getCompressLength(), is(0L));

        block.compress();

        byte[] bytes = SofSerialiser.toBytes(block);

        InMemorySofBlock reloaded = SofSerialiser.fromBytes(bytes, InMemorySofBlock.class);
        reloaded.setSofConfiguration(configuration);

        assertThat(reloaded.getStartTime(), is(1000L));
        assertThat(reloaded.getEndTime(), is(1500L));

        final Bucket<SerialisableObject> bucket = new Bucket<SerialisableObject>();
        reloaded.visitForwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                bucket.add(t);
            }
        });

        assertThat(bucket.size(), is(2));
        DefaultLogEvent event1 = (DefaultLogEvent) bucket.get(0);
        DefaultLogEvent event2 = (DefaultLogEvent) bucket.get(1);

        assertThat(event1.getMessage(), is(LogEventFixture1.event5.getMessage()));
        assertThat(event2.getMessage(), is(LogEventFixture1.event10.getMessage()));

    }

    @Test public void test_using_time_provider() throws SofException, EOFException {

        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration);
        FixedTimeProvider timeProvider = new FixedTimeProvider(666);
        block.setTimeProvider(timeProvider);

        block.write(LogEventFixture1.event5);

        assertThat(block.getStartTime(), is(666L));
        assertThat(block.getEndTime(), is(666L));
        assertThat(block.getCompressLength(), is(0L));

        timeProvider.setTime(1232);
        block.write(LogEventFixture1.event10);

        assertThat(block.getStartTime(), is(666L));
        assertThat(block.getEndTime(), is(1232L));
        assertThat(block.getCompressLength(), is(0L));

        block.compress();

        byte[] bytes = SofSerialiser.toBytes(block);

        InMemorySofBlock reloaded = SofSerialiser.fromBytes(bytes, InMemorySofBlock.class);
        reloaded.setSofConfiguration(configuration);

        assertThat(reloaded.getStartTime(), is(666L));
        assertThat(reloaded.getEndTime(), is(1232L));

        final Bucket<SerialisableObject> bucket = new Bucket<SerialisableObject>();
        reloaded.visitForwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject t) {
                bucket.add(t);
            }
        });

        assertThat(bucket.size(), is(2));
        DefaultLogEvent event1 = (DefaultLogEvent) bucket.get(0);
        DefaultLogEvent event2 = (DefaultLogEvent) bucket.get(1);

        assertThat(event1.getMessage(), is(LogEventFixture1.event5.getMessage()));
        assertThat(event2.getMessage(), is(LogEventFixture1.event10.getMessage()));

    }

    @Test public void test_fill_block() throws SofException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(DefaultLogEvent.class, 0);

        int encodedLength = SofSerialiser.toBytes(LogEventFixture1.event5).length;

        InMemorySofBlock block = new InMemorySofBlock(configuration, encodedLength + 10);

        block.write(LogEventFixture1.event5);

        try {
            block.write(LogEventFixture1.event10);
            fail("The block should have overfloweth");
        } catch (BufferOverflowException boe) {

        }
    }

    @Test public void test_visit_backwards_not_compressed() throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration, 50);

        block.write(new SofString("a"));
        block.write(new SofString("b"));
        block.write(new SofString("c"));

        final List<SofString> results = new ArrayList<SofString>();

        block.visitBackwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject serialisableObject) {
                results.add((SofString) serialisableObject);
            }
        });

        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is("c"));
        assertThat(results.get(1).getValue(), is("b"));
        assertThat(results.get(2).getValue(), is("a"));

    }

    @Test public void test_visit_forwards_not_compressed() throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration, 50);

        block.write(new SofString("a"));
        block.write(new SofString("b"));
        block.write(new SofString("c"));

        final List<SofString> results = new ArrayList<SofString>();

        block.visitForwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject serialisableObject) {
                results.add((SofString) serialisableObject);
            }
        });

        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is("a"));
        assertThat(results.get(1).getValue(), is("b"));
        assertThat(results.get(2).getValue(), is("c"));


    }

    @Test public void test_visit_forwards_compressed() throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration, 50);

        block.write(new SofString("a"));
        block.write(new SofString("b"));
        block.write(new SofString("c"));
        block.compress();

        final List<SofString> results = new ArrayList<SofString>();

        block.visitForwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject serialisableObject) {
                results.add((SofString) serialisableObject);
            }
        });

        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is("a"));
        assertThat(results.get(1).getValue(), is("b"));
        assertThat(results.get(2).getValue(), is("c"));
    }

    @Test public void test_visit_backwards_compressed() throws SofException, EOFException {
        SofConfiguration configuration = new SofConfiguration();
        configuration.registerType(SofString.class, 0);

        InMemorySofBlock block = new InMemorySofBlock(configuration, 50);

        block.write(new SofString("a"));
        block.write(new SofString("b"));
        block.write(new SofString("c"));
        block.compress();
        block.setNeedsDecompression(true);

        final List<SofString> results = new ArrayList<SofString>();

        block.visitBackwards(new Destination<SerialisableObject>() {
            @Override public void send(SerialisableObject serialisableObject) {
                results.add((SofString) serialisableObject);
            }
        });

        assertThat(results.size(), is(3));
        assertThat(results.get(0).getValue(), is("c"));
        assertThat(results.get(1).getValue(), is("b"));
        assertThat(results.get(2).getValue(), is("a"));

    }

}
