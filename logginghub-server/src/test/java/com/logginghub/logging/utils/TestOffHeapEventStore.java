package com.logginghub.logging.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.logginghub.logging.utils.OffHeapEventStore;
import com.logginghub.utils.ByteUtils;
import com.logginghub.utils.Decoder;
import com.logginghub.utils.Encoder;
import com.logginghub.utils.Factory;
import com.logginghub.utils.Out;
import com.logginghub.utils.filter.Filter;

public class TestOffHeapEventStore {

    class Element {
        public String message;

        public String getMessage() {
            return message;
        }

        public Element(String message) {
            this.message = message;
        }

        public Element() {}

        @Override public String toString() {
            return message;
        }
    }

    private Encoder<Element> encoder = new Encoder<Element>() {
        @Override public void encode(Element event, ByteBuffer dataBuffer) {
            encodeString(dataBuffer, event.message);
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

    private Decoder<Element> decoder = new Decoder<Element>() {
        @Override public void decode(Element event, ByteBuffer buffer) {
            event.message = decodeString(buffer);
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

    private Factory<Element> factory = new Factory<Element>() {
        @Override public Element create() {
            return new Element();
        }
    };

    private OffHeapEventStore<Element> store = new OffHeapEventStore<Element>((int) ByteUtils.kilobytes(1),
                                                                              (int) ByteUtils.kilobytes(1),
                                                                              encoder,
                                                                              decoder,
                                                                              factory);

    private Element event1 = new Element("Event 1");
    private Element event2 = new Element("Event 2");
    private Element event3 = new Element("Event 3");
    private Element event4 = new Element("Event 4");
    private Element event5 = new Element("Event 5");
    private Element event6 = new Element("Event 6");
    private Element event7 = new Element("Event 7");
    private Element event8 = new Element("Event 8");
    private Element event9 = new Element("Event 9");
    private Element event10 = new Element("Event 10");
    private Element event11 = new Element("Event 11");
    private Element event12 = new Element("Event 12");
    private Element event13 = new Element("Event 13");
    private Element event14 = new Element("Event 14");
    private Element event15 = new Element("Event 15");
    private Element event16 = new Element("Event 16");

    @Test public void test_add_empty() {
        store = new OffHeapEventStore<Element>(100, 100, encoder, decoder, factory);
        store.add(new Element("hello world"));
        Out.out(store);
        assertThat(store.get(0).message, is("hello world"));
    }

    @Test public void test_add_second() {
        store = new OffHeapEventStore<Element>(100, 100, encoder, decoder, factory);
        store.add(new Element("hello world"));
        store.add(new Element("hello world 2"));
        Out.out(store);

        assertThat(store.get(0).message, is("hello world"));
        assertThat(store.get(1).message, is("hello world 2"));
    }

    @Test public void test_add_last_fits() {

        int size = "hello world".getBytes().length + 4 + "hello world 2".getBytes().length + 4 + "hello world 3".getBytes().length + 4;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("hello world"));
        store.add(new Element("hello world 2"));
        store.add(new Element("hello world 3"));

        assertThat(store.get(0).message, is("hello world"));
        assertThat(store.get(1).message, is("hello world 2"));
        assertThat(store.get(2).message, is("hello world 3"));

        assertThat(store.getDataBuffer().position(), is(size));
    }

    @Test public void test_add_overflow_smaller_than_previous() {

        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("13 bytes1")); // 4+ 2 + 1 + 6 = 13 bytes encoded
        store.add(new Element("13 bytes2"));
        store.add(new Element("hello"));

        // store.dumpState();

        assertThat(store.getCount(), is(2));
        assertThat(store.get(0).message, is("13 bytes2"));
        assertThat(store.get(1).message, is("hello"));
    }

    @Test public void test_add_overflow_same_as_previous() {

        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("13 bytes1")); // 4+ 2 + 1 + 6 = 13 bytes encoded
        store.add(new Element("13 bytes2"));
        store.add(new Element("13 bytes3"));
        Out.out(store);

        // store.dumpState();

        assertThat(store.getCount(), is(2));
        assertThat(store.get(0).message, is("13 bytes2"));
        assertThat(store.get(1).message, is("13 bytes3"));
    }

    @Test public void test_add_overflow_larger_than_previous() {

        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("13 bytes1")); // 4+ 2 + 1 + 6 = 13 bytes encoded
        store.add(new Element("13 bytes2"));
        store.add(new Element("larger !!!"));

        store.dumpState();

        assertThat(store.getCount(), is(1));
        assertThat(store.get(0).message, is("larger !!!"));
    }

    @Test public void test_add_overflow_larger_than_previous_add_second() {

        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("13 bytes1")); // 4+ 2 + 1 + 6 = 13 bytes encoded
        store.add(new Element("13 bytes2"));
        store.add(new Element("larger !!!"));
        store.add(new Element("13 bytes3"));

        store.dumpState();

        assertThat(store.getCount(), is(2));
        assertThat(store.get(0).message, is("larger !!!"));
        assertThat(store.get(1).message, is("13 bytes3"));
    }

    @Test public void test_add_overflow_second_fill() {

        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("13 bytes1")); // 4+ 2 + 1 + 6 = 13 bytes encoded
        store.add(new Element("13 bytes2"));
        store.add(new Element("larger !!!"));
        store.add(new Element("13 bytes3"));
        assertThat(store.getCount(), is(2));

        store.add(new Element("7_1"));
        store.dumpState();
        assertThat(store.getCount(), is(2));

        store.add(new Element("7_2"));
        store.dumpState();
        assertThat(store.getCount(), is(3));

        store.add(new Element("7_3"));
        store.dumpState();
        assertThat(store.getCount(), is(3));

        store.add(new Element("7_4"));
        store.dumpState();
        assertThat(store.getCount(), is(4));

        assertThat(store.getCount(), is(4));
        assertThat(store.get(0).message, is("7_1"));
        assertThat(store.get(1).message, is("7_2"));
        assertThat(store.get(2).message, is("7_3"));
        assertThat(store.get(3).message, is("7_4"));

    }

    @Test public void test_filter() {
        
        int size = 28;

        store = new OffHeapEventStore<Element>(size, 100, encoder, decoder, factory);
        store.add(new Element("7_1"));
        store.add(new Element("7_2"));
        store.add(new Element("7_3"));
        store.add(new Element("7_4"));
        
        assertThat(store.getCount(), is(4));
        assertThat(store.get(0).message, is("7_1"));
        assertThat(store.get(1).message, is("7_2"));
        assertThat(store.get(2).message, is("7_3"));
        assertThat(store.get(3).message, is("7_4"));
        
        store.applyFilter(new Filter<TestOffHeapEventStore.Element>() {
            @Override public boolean passes(Element t) {
                return t.message.contains("3");
            }
        });
        
        assertThat(store.getCount(), is(1));
        assertThat(store.get(0).message, is("7_3"));
        
        store.clearFilter();
        
        assertThat(store.getCount(), is(4));
        assertThat(store.get(0).message, is("7_1"));
        assertThat(store.get(1).message, is("7_2"));
        assertThat(store.get(2).message, is("7_3"));
        assertThat(store.get(3).message, is("7_4"));
        
    }
    
}
