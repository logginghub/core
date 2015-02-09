package com.logginghub.logging.messages;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.utils.Visitor;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.sof.SofConfiguration;
import com.logginghub.utils.sof.SofSerialiser;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestCompressedBlock {

    private DefaultLogEvent event1 = LogEventBuilder.create(0, Logger.info, "event1");
    private DefaultLogEvent event2 = LogEventBuilder.create(0, Logger.warning, "event2");
    private DefaultLogEvent event3 = LogEventBuilder.create(0, Logger.severe, "event3");
    private DefaultLogEvent event4 = LogEventBuilder.create(0, Logger.info, "event4");
    private DefaultLogEvent event5 = LogEventBuilder.create(0, Logger.debug, "event5");

    @Test public void test_breaking_it_down() throws Exception {

        SofSerialisationStrategy serialiser = new SofSerialisationStrategy(true, true);
        serialiser.getConfiguration().registerType(DefaultLogEvent.class, 0);
        LZ4CompressionStrategy compression = new LZ4CompressionStrategy();

        ByteBuffer byteBuffer = ByteBuffer.allocate(100000);

        serialiser.serialise(byteBuffer, event1);
        serialiser.serialise(byteBuffer, event2);
        serialiser.serialise(byteBuffer, event3);
        serialiser.serialise(byteBuffer, event4);
        serialiser.serialise(byteBuffer, event5);

        byteBuffer.flip();
        byteBuffer.mark();

        assertThat((DefaultLogEvent) serialiser.deserialise(byteBuffer), is(event1));
        assertThat((DefaultLogEvent) serialiser.deserialise(byteBuffer), is(event2));
        assertThat((DefaultLogEvent) serialiser.deserialise(byteBuffer), is(event3));
        assertThat((DefaultLogEvent) serialiser.deserialise(byteBuffer), is(event4));
        assertThat((DefaultLogEvent) serialiser.deserialise(byteBuffer), is(event5));

        byteBuffer.reset();

        ByteBuffer compressed = compression.compress(byteBuffer);
        ByteBuffer decompressed = compression.decompress(compressed);

        assertThat((DefaultLogEvent) serialiser.deserialise(decompressed), is(event1));
        assertThat((DefaultLogEvent) serialiser.deserialise(decompressed), is(event2));
        assertThat((DefaultLogEvent) serialiser.deserialise(decompressed), is(event3));
        assertThat((DefaultLogEvent) serialiser.deserialise(decompressed), is(event4));
        assertThat((DefaultLogEvent) serialiser.deserialise(decompressed), is(event5));


    }

    @Test public void test_lz4() throws Exception {

        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(CompressedBlock.class, 0);
        sofConfiguration.registerType(DefaultLogEvent.class, 1);

        CompressedBlock<DefaultLogEvent> block = new CompressedBlock<DefaultLogEvent>();
        block.setCompressionStrategy(CompressionStrategyFactory.compression_lz4);

        block.addObject(event1);
        block.addObject(event2);
        block.addObject(event3);
        block.addObject(event4);
        block.addObject(event5);

        byte[] bytes = SofSerialiser.toBytes(block, sofConfiguration);

        CompressedBlock<DefaultLogEvent> decoded = SofSerialiser.fromBytes(bytes, CompressedBlock.class);

        final List<DefaultLogEvent> events = new ArrayList<DefaultLogEvent>();

        decoded.setSofConfiguration(sofConfiguration);
        decoded.decodeObjects(DefaultLogEvent.class, new Visitor<DefaultLogEvent>() {
            public void visit(DefaultLogEvent t) {
                events.add(t);
            }
        });

        assertThat(events.size(), is(5));
        assertThat(events.get(0), is(event1));
        assertThat(events.get(1), is(event2));
        assertThat(events.get(2), is(event3));
        assertThat(events.get(3), is(event4));
        assertThat(events.get(4), is(event5));

        DefaultLogEvent[] decodeAll = decoded.decodeAll(DefaultLogEvent.class);
        assertThat(decodeAll.length, is(5));
        assertThat(decodeAll[0], is(event1));
        assertThat(decodeAll[1], is(event2));
        assertThat(decodeAll[2], is(event3));
        assertThat(decodeAll[3], is(event4));
        assertThat(decodeAll[4], is(event5));

    }

    @Test public void test_plain() throws Exception {

        SofConfiguration sofConfiguration = new SofConfiguration();
        sofConfiguration.registerType(CompressedBlock.class, 0);
        sofConfiguration.registerType(DefaultLogEvent.class, 1);

        CompressedBlock<DefaultLogEvent> block = new CompressedBlock<DefaultLogEvent>();
        block.setCompressionStrategy(CompressionStrategyFactory.compression_none);

        block.addObject(event1);
        block.addObject(event2);
        block.addObject(event3);
        block.addObject(event4);
        block.addObject(event5);

        byte[] bytes = SofSerialiser.toBytes(block, sofConfiguration);

        CompressedBlock<DefaultLogEvent> decoded = SofSerialiser.fromBytes(bytes, CompressedBlock.class);

        decoded.setSofConfiguration(sofConfiguration);

        final List<DefaultLogEvent> events = new ArrayList<DefaultLogEvent>();

        decoded.decodeObjects(DefaultLogEvent.class, new Visitor<DefaultLogEvent>() {
            public void visit(DefaultLogEvent t) {
                events.add(t);
            }
        });

        assertThat(events.size(), is(5));
        assertThat(events.get(0), is(event1));
        assertThat(events.get(1), is(event2));
        assertThat(events.get(2), is(event3));
        assertThat(events.get(3), is(event4));
        assertThat(events.get(4), is(event5));

    }

}
