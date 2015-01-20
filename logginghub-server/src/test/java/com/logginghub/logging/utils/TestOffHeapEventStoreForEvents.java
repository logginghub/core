package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.utils.OffHeapEventStore;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Decoder;
import com.logginghub.utils.Encoder;
import com.logginghub.utils.Factory;

public class TestOffHeapEventStoreForEvents {

    private Encoder<DefaultLogEvent> encoder = new Encoder<DefaultLogEvent>() {
        @Override public void encode(DefaultLogEvent event, ByteBuffer dataBuffer) {
            dataBuffer.putLong(event.getSequenceNumber());
            dataBuffer.putLong(event.getOriginTime());
            dataBuffer.putInt(event.getLevel());
            dataBuffer.putInt(event.getPid());
            encodeString(dataBuffer, event.getMessage());
            encodeString(dataBuffer, event.getChannel());
            encodeString(dataBuffer, event.getFormattedException());
            encodeString(dataBuffer, event.getLoggerName());
            encodeString(dataBuffer, event.getSourceAddress());
            encodeString(dataBuffer, event.getSourceApplication());
            encodeString(dataBuffer, event.getSourceClassName());
            encodeString(dataBuffer, event.getSourceHost());
            encodeString(dataBuffer, event.getSourceMethodName());
            encodeString(dataBuffer, event.getThreadName());
        }

        private void encodeString(ByteBuffer buffer, String message) {
            if (message == null) {
                buffer.putInt(-1);
            }
            else {
                buffer.putInt(message.getBytes().length);
                buffer.put(message.getBytes());
            }
        }
    };

    private Decoder<DefaultLogEvent> decoder = new Decoder<DefaultLogEvent>() {
        @Override public void decode(DefaultLogEvent event, ByteBuffer buffer) {
            event.setSequenceNumber(buffer.getLong());
            event.setLocalCreationTimeMillis(buffer.getLong());
            event.setLevel(buffer.getInt());
            event.setPid(buffer.getInt());
            event.setMessage(decodeString(buffer));
            event.setChannel(decodeString(buffer));
            event.setFormattedException(decodeString(buffer));
            event.setLoggerName(decodeString(buffer));
            event.setSourceAddress(decodeString(buffer));
            event.setSourceApplication(decodeString(buffer));
            event.setSourceClassName(decodeString(buffer));
            event.setSourceHost(decodeString(buffer));
            event.setSourceMethodName(decodeString(buffer));
            event.setThreadName(decodeString(buffer));
        }

        private String decodeString(ByteBuffer buffer) {
            String decoded;
            int size = buffer.getInt();
            if (size == -1) {
                decoded = null;
            }
            else {
                byte[] data = new byte[size];
                buffer.get(data);
                decoded = new String(data);
            }
            return decoded;
        }

    };

    private Factory<DefaultLogEvent> factory = new Factory<DefaultLogEvent>() {
        @Override public DefaultLogEvent create() {
            return new DefaultLogEvent();
        }
    };

    private OffHeapEventStore<DefaultLogEvent> store = new OffHeapEventStore<DefaultLogEvent>((int) ByteUtils.kilobytes(1),
                                                                                              (int) ByteUtils.kilobytes(1),
                                                                                              encoder,
                                                                                              decoder,
                                                                                              factory);

    private DefaultLogEvent event1 = LogEventBuilder.start().setMessage("Event 01").setSequenceNumber(1).toLogEvent();
    private DefaultLogEvent event2 = LogEventBuilder.start().setMessage("Event 02").setSequenceNumber(2).toLogEvent();
    private DefaultLogEvent event3 = LogEventBuilder.start().setMessage("Event 03").setSequenceNumber(3).toLogEvent();

    private DefaultLogEvent event4 = LogEventBuilder.start().setMessage("Event 04").setSequenceNumber(4).toLogEvent();
    private DefaultLogEvent event4a = LogEventBuilder.start().setMessage("Event 04 bigger!").setSequenceNumber(4).toLogEvent();

    private DefaultLogEvent event5 = LogEventBuilder.start().setMessage("Event 05").setSequenceNumber(5).toLogEvent();
    private DefaultLogEvent event6 = LogEventBuilder.start().setMessage("Event 06").setSequenceNumber(6).toLogEvent();
    private DefaultLogEvent event7 = LogEventBuilder.start().setMessage("Event 07").setSequenceNumber(7).toLogEvent();
    private DefaultLogEvent event8 = LogEventBuilder.start().setMessage("Event 08").setSequenceNumber(8).toLogEvent();
    private DefaultLogEvent event9 = LogEventBuilder.start().setMessage("Event 09").setSequenceNumber(9).toLogEvent();

    private DefaultLogEvent event10 = LogEventBuilder.start().setMessage("Event 10").setSequenceNumber(10).toLogEvent();
    private DefaultLogEvent event11 = LogEventBuilder.start().setMessage("Event 11").setSequenceNumber(11).toLogEvent();
    private DefaultLogEvent event12 = LogEventBuilder.start().setMessage("Event 12").setSequenceNumber(12).toLogEvent();

    @Test public void test_add_one() throws Exception {
        store.add(event1);

        assertThat(store.getCount(), is(1));

        List<DefaultLogEvent> decodeAll = store.decodeAll();
        assertThat(decodeAll.size(), is(1));
        assertThat(decodeAll.get(0).getMessage(), is("Event 01"));
    }

    @Test public void test_add_multiple() throws Exception {
        store.add(event1);
        store.add(event2);
        store.add(event3);

        assertThat(store.getCount(), is(3));

        List<DefaultLogEvent> decodeAll = store.decodeAll();
        assertThat(decodeAll.size(), is(3));
        assertThat(decodeAll.get(0).getMessage(), is("Event 01"));
        assertThat(decodeAll.get(1).getMessage(), is("Event 02"));
        assertThat(decodeAll.get(2).getMessage(), is("Event 03"));
    }

    @Test public void test_wrapping_equal_size_events() {

//        store.add(event1);
//
//        int used = store.getUsed();
//
//        // Create a new store with a specific size - it will be able to store 3 events, but will
//        // wrap on the fourth
//        store = new OffHeapEventStore(used * 3 + 1, used * 3 + 1, encoder, decoder, factory);
//
//        store.add(event1);
//        store.dumpLookups();
//        assertThat(store.getDataBuffer().position(), is(used));
//        assertThat(store.getLookupBuffer().position(), is(4));
//        assertThat(store.getLookupBuffer().getInt(0), is(0));
//        assertThat(store.getCount(), is(1));
//
//        store.add(event2);
//        store.dumpLookups();
//        assertThat(store.getDataBuffer().position(), is(used * 2));
//        assertThat(store.getLookupBuffer().position(), is(8));
//        assertThat(store.getLookupBuffer().getInt(0), is(0));
//        assertThat(store.getLookupBuffer().getInt(4), is(used));
//        assertThat(store.getCount(), is(2));
//
//        store.add(event3);
//        store.dumpLookups();
//        assertThat(store.getDataBuffer().position(), is(used * 3));
//        assertThat(store.getLookupBuffer().position(), is(12));
//
//        assertThat(store.getLookupBuffer().getInt(0), is(0));
//        assertThat(store.getLookupBuffer().getInt(4), is(used));
//        assertThat(store.getLookupBuffer().getInt(8), is(used + used));
//
//        assertThat(store.get(0).getMessage(), is("Event 01"));
//        assertThat(store.get(1).getMessage(), is("Event 02"));
//        assertThat(store.get(2).getMessage(), is("Event 03"));
//
//        assertThat(store.getCount(), is(3));
//
//        // This will wrap around
//        store.add(event4);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 02"));
//        assertThat(store.get(1).getMessage(), is("Event 03"));
//        assertThat(store.get(2).getMessage(), is("Event 04"));
//
//        assertThat(store.getDataBuffer().position(), is(used));
//
//        List<DefaultLogEvent> decodeAll = store.decodeAll();
//        assertThat(decodeAll.size(), is(3));
//        assertThat(decodeAll.get(0).getMessage(), is("Event 02"));
//        assertThat(decodeAll.get(1).getMessage(), is("Event 03"));
//        assertThat(decodeAll.get(2).getMessage(), is("Event 04"));
//
//        // Now we need to to wrap around again entirely to make sure the read pointer gets reset
//        store.add(event5);
//
//        store.dumpLookups();
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 03"));
//        assertThat(store.get(1).getMessage(), is("Event 04"));
//        assertThat(store.get(2).getMessage(), is("Event 05"));
//
//        store.add(event6);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 04"));
//        assertThat(store.get(1).getMessage(), is("Event 05"));
//        assertThat(store.get(2).getMessage(), is("Event 06"));
//
//        store.add(event7);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 05"));
//        assertThat(store.get(1).getMessage(), is("Event 06"));
//        assertThat(store.get(2).getMessage(), is("Event 07"));
//
//        store.add(event8);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 06"));
//        assertThat(store.get(1).getMessage(), is("Event 07"));
//        assertThat(store.get(2).getMessage(), is("Event 08"));
//
//        store.add(event9);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 07"));
//        assertThat(store.get(1).getMessage(), is("Event 08"));
//        assertThat(store.get(2).getMessage(), is("Event 09"));
//
//        store.add(event10);
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 08"));
//        assertThat(store.get(1).getMessage(), is("Event 09"));
//        assertThat(store.get(2).getMessage(), is("Event 10"));
//
//        store.add(event11);
//        store.dumpLookups();
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 09"));
//        assertThat(store.get(1).getMessage(), is("Event 10"));
//        assertThat(store.get(2).getMessage(), is("Event 11"));
//
//        store.add(event12);
//        store.dumpLookups();
//
//        assertThat(store.getCount(), is(3));
//        assertThat(store.get(0).getMessage(), is("Event 10"));
//        assertThat(store.get(1).getMessage(), is("Event 11"));
//        assertThat(store.get(2).getMessage(), is("Event 12"));

    }

    @Test public void test_wrapping_uneven_size_events() {

//        store.add(event1);
//
//        int used = store.getUsed();
//
//        // Create a new store with a specific size - it will be able to store 3 events, but will
//        // wrap on the fourth
//        store = new OffHeapEventStore(used * 3 + 1, used * 3 + 1, encoder, decoder, factory);
//
//        store.add(event1);
//        store.add(event2);
//        store.add(event3);
//
//        assertThat(store.get(0).getSequenceNumber(), is(1L));
//        assertThat(store.get(1).getSequenceNumber(), is(2L));
//        assertThat(store.get(2).getSequenceNumber(), is(3L));
//
//        // This will wrap around, and overwrite all of event1 and a bit of the start of event2
//        store.add(event4a);
//        store.dumpLookups();
//
//        assertThat(store.getCount(), is(2));
//        assertThat(store.get(0).getMessage(), is("Event 03"));
//        assertThat(store.get(1).getMessage(), is("Event 04 bigger!"));
//
//        assertThat(store.get(0).getSequenceNumber(), is(3L));
//        assertThat(store.get(1).getSequenceNumber(), is(4L));

    }
}
