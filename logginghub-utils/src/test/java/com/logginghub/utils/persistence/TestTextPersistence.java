package com.logginghub.utils.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;
import com.logginghub.utils.persistence.Bag;
import com.logginghub.utils.persistence.ReflectionBinder;
import com.logginghub.utils.persistence.TextPersistence;

public class TestTextPersistence {
    
    @Test public void test_with_observables() throws Exception {
        
        WithObservables withObservables = new WithObservables();
        withObservables.getStringValue().set("new string");
        
        String string = TextPersistence.toString(withObservables);
        System.out.println(string);
        assertThat(string, is("{\"doubleValue\":57.0,\"integerValue\":55,\"longValue\":56,\"normalString\":\"normalString\",\"stringValue\":\"new string\"}"));
        
        WithObservables fromString = TextPersistence.fromString(string, WithObservables.class);
        
        assertThat(fromString.getStringValue().asString(), is("new string"));
    }
    
    @Test public void test_string_array_single() throws Exception {
        validate("hello", "\"hello\"");
        validate(10, "\"10\"");
        validate(new String[] { "a", "b", "c" }, "[\"a\",\"b\",\"c\"]");
        validate(new String[] { "1", "2", "3" }, "[\"1\",\"2\",\"3\"]");
        validate(new int[] { 1, 2, 3 }, "[1,2,3]");
        validate(new double[] { 1.2, 2.3, 3.4 }, "[1.2,2.3,3.4]");
        validate(new WithArray(), "{\"array\":[\"1\",\"2\"],\"message\":\"message\"}");
    }

    private void validate(Object object, String expectedText) throws Exception {
        ReflectionBinder binder = new ReflectionBinder();
        Bag bag = binder.fromObject(object);

        Object decoded = binder.toObject(bag, object.getClass());
        assertThat(decoded, is(object));

        TextPersistence textPersistence = new TextPersistence();
        String output = textPersistence.toString(bag);

        assertThat(output, is(expectedText));

        Bag fromString = textPersistence.fromString(output);

        assertThat(fromString.hasValue(), is(bag.hasValue()));
        assertThat(fromString.getValue(), is(bag.getValue()));
        assertThat(fromString.getContents().size(), is(bag.getContents().size()));

        // Pah, have to do the map comparison by hand...
        Set<Entry<String, Object>> entrySet = fromString.getContents().entrySet();
        for (Entry<String, Object> entry : entrySet) {

            String aKey = entry.getKey();
            Object aValue = entry.getValue();

            Object bValue = bag.get(aKey);

            assertThat(aValue.getClass().getName(), is(bValue.getClass().getName()));
            assertThat(aValue, is(bValue));
        }

        Object decodedFromText = binder.toObject(fromString, object.getClass());
        assertThat(decodedFromText, is(object));
    }

    @Test public void testToStringMultiLine() throws Exception {

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setStringObject("this is a new string");
        object.setIntegerObject(Integer.valueOf(3452));
        object.setBigDecimalObject(null);
        object.setStringArrayObject(new String[] { "sa", "sb", "sc" });

        ReflectionBinder binder = new ReflectionBinder();
        Bag bag = binder.fromObject(object);

        TextPersistence textPersistence = new TextPersistence();
        String output = textPersistence.toString(bag);
        System.out.println(output);

        assertThat(output,
                   is("{\"booleanObject\":\"true\",\"booleanType\":\"false\",\"byteArrayObject\":[98,121,116,101,32,97,114,114,97,121],\"byteObject\":\"7\",\"byteType\":\"-1\",\"charType\":\"s\",\"characterObject\":\"j\",\"dateObject\":\"Fri Jan 02 11:12:03 GMT 1970\",\"doubleObject\":\"123.123\",\"doubleType\":\"1.23\",\"floatObject\":\"10.1\",\"floatType\":\"20.2\",\"intType\":\"2\",\"integerObject\":\"3452\",\"longObject\":\"123123123123\",\"longType\":\"300000000000\",\"shortObject\":\"5555\",\"shortType\":\"1111\",\"stringArrayObject\":[\"sa\",\"sb\",\"sc\"],\"stringObject\":\"this is a new string\"}"));

    }

    public static class WithObservables {
        private ObservableInteger integerValue = new ObservableInteger(55);
        private ObservableLong longValue = new ObservableLong(56L);
        private ObservableDouble doubleValue = new ObservableDouble(57d);
        private ObservableProperty<String> stringValue = new ObservableProperty<String>("stringValue");
        private String normalString = "normalString";

        public ObservableInteger getIntegerValue() {
            return integerValue;
        }

        public ObservableLong getLongValue() {
            return longValue;
        }

        public ObservableDouble getDoubleValue() {
            return doubleValue;
        }

        public ObservableProperty<String> getStringValue() {
            return stringValue;
        }

        public String getNormalString() {
            return normalString;
        }
    }

    public static class WithArray {
        public WithArray() {}

        private String[] array = new String[] { "1", "2" };
        private String message = "message";

        public String[] getArray() {
            return array;
        }

        public String getMessage() {
            return message;
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(array);
            result = prime * result + ((message == null) ? 0 : message.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            WithArray other = (WithArray) obj;
            if (!Arrays.equals(array, other.array)) return false;
            if (message == null) {
                if (other.message != null) return false;
            }
            else if (!message.equals(other.message)) return false;
            return true;
        }

        @Override public String toString() {
            return "WithArray [array=" + Arrays.toString(array) + ", message=" + message + "]";
        }

    }

}
