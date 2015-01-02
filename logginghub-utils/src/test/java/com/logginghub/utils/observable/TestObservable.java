package com.logginghub.utils.observable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.utils.observable.Observable;
import com.logginghub.utils.observable.ObservableDouble;
import com.logginghub.utils.observable.ObservableInteger;
import com.logginghub.utils.observable.ObservableList;
import com.logginghub.utils.observable.ObservableLong;
import com.logginghub.utils.observable.ObservableProperty;

public class TestObservable {

    @Before public void setupLineBreaks() {
        Observable.newline = "\r\n";
    }
    
    @Test public void test_string_list() {
        Observable_Multiple_With_Base_Type_List obs = new Observable_Multiple_With_Base_Type_List();
        obs.getStringList().add("value 1");
        obs.getStringList().add("value 2");

        obs.getIntList().add(1);
        obs.getIntList().add(2);
        obs.getIntList().add(3);

        String xml = obs.toXml("obs");
        System.out.println(xml);
        assertThat(xml,
                   is("<obs>\r\n<stringList>\r\n<string>value 1</string>\r\n<string>value 2</string>\r\n</stringList>\r\n<intList>\r\n<integer>1</integer>\r\n<integer>2</integer>\r\n<integer>3</integer>\r\n</intList>\r\n</obs>"));

        Observable_Multiple_With_Base_Type_List decoded = new Observable_Multiple_With_Base_Type_List();
        decoded.fromXml(xml);

        assertThat(decoded.getStringList().size(), is(2));
        assertThat(decoded.getStringList().get(0), is("value 1"));
        assertThat(decoded.getStringList().get(1), is("value 2"));

        assertThat(decoded.getIntList().size(), is(3));
        assertThat(decoded.getIntList().get(0), is(1));
        assertThat(decoded.getIntList().get(1), is(2));
        assertThat(decoded.getIntList().get(2), is(3));
    }

    @Test public void test_empty() throws Exception {
        Observable_Empty empty = new Observable_Empty();
        String xml = empty.toXml("empty");
        assertThat(xml, is("<empty/>"));
        assertThat(empty.toJSON("empty"), is("{\"empty\":{}}"));
        
        empty.fromXml(xml);
    }

    @Test public void test_attribute() throws Exception {
        Observable_Single single = new Observable_Single();
        single.getString().set("value");

        String xml = single.toXml("single");
        assertThat(xml, is("<single string=\"value\"/>"));

        assertThat(single.toJSON("single"), is("{\"single\":{\"string\":\"value\"}}"));
        
        Observable_Single decoded = new Observable_Single();
        decoded.fromXml(xml);

        assertThat(decoded.getString().get(), is("value"));
    }
    
    @Test public void test_string_escaping_quote() throws Exception {
        Observable_Single single = new Observable_Single();
        single.getString().set("value with quote \" mark");

        String xml = single.toXml("single");
        assertThat(xml, is("<single string=\"value with quote &quot; mark\"/>"));
        assertThat(single.toJSON("single"), is("{\"single\":{\"string\":\"value with quote \\\" mark\"}}"));
        
        Observable_Single decoded = new Observable_Single();
        decoded.fromXml(xml);

        assertThat(decoded.getString().get(), is("value with quote \" mark"));
    }
    
    @Test public void test_string_escaping_appostrophe() throws Exception {
        Observable_Single single = new Observable_Single();
        single.getString().set("value with apostrophe ' character");

        String xml = single.toXml("single");
        assertThat(xml, is("<single string=\"value with apostrophe &apos; character\"/>"));
        assertThat(single.toJSON("single"), is("{\"single\":{\"string\":\"value with apostrophe ' character\"}}"));

        Observable_Single decoded = new Observable_Single();
        decoded.fromXml(xml);

        assertThat(decoded.getString().get(), is("value with apostrophe ' character"));
    }

    @Test public void test_element() {
        Observable_With_Just_Element justElement = new Observable_With_Just_Element();
        justElement.getSubObject().get().getString().set("value");

        String xml = justElement.toXml("justElement");
        assertThat(xml, is("<justElement>\r\n<subObject string=\"value\"/>\r\n</justElement>"));
        
        assertThat(justElement.toJSON("justElement"), is("{\"justElement\":{\"subObject\":{\"string\":\"value\"}}}"));

        Observable_With_Just_Element decoded = new Observable_With_Just_Element();
        decoded.fromXml(xml);

        assertThat(decoded.getSubObject().get().getString().get(), is("value"));
    }
    
    @Test public void test_multiple_element() {
        Observable_With_Multiple_Elements multiple = new Observable_With_Multiple_Elements();
        multiple.getSub1().get().getString().set("value1");
        multiple.getSub2().get().getString().set("value2");
        multiple.getSub3().get().getString().set("value3");
        

        String xml = multiple.toXml("multiple");
        assertThat(xml, is("<multiple>\r\n<subObject1 string=\"value1\"/>\r\n<subObject2 string=\"value2\"/>\r\n<subObject3 string=\"value3\"/>\r\n</multiple>"));
        
        assertThat(multiple.toJSON("multiple"), is("{\"multiple\":{\"subObject1\":{\"string\":\"value1\"},\"subObject2\":{\"string\":\"value2\"},\"subObject3\":{\"string\":\"value3\"}}}"));

        Observable_With_Multiple_Elements decoded = new Observable_With_Multiple_Elements();
        decoded.fromXml(xml);

        assertThat(decoded.getSub1().get().getString().get(), is("value1"));
        assertThat(decoded.getSub2().get().getString().get(), is("value2"));
        assertThat(decoded.getSub3().get().getString().get(), is("value3"));
    }

    @Test public void test_mixed() {
        Observable_With_Both both = new Observable_With_Both();
        both.getString().set("other value");
        both.getSubObject().get().getString().set("value");
        both.getDoubleValue().set(6);

        String xml = both.toXml("both");
        assertThat(xml, is("<both string=\"other value\" doubleValue=\"6.0\">\r\n<subObject string=\"value\"/>\r\n</both>"));

        assertThat(both.toJSON("both"), is("{\"both\":{\"string\":\"other value\",\"doubleValue\":6.0,\"subObject\":{\"string\":\"value\"}}}"));
        
        Observable_With_Both decoded = new Observable_With_Both();
        decoded.fromXml(xml);

        assertThat(decoded.getString().get(), is("other value"));
        assertThat(decoded.getSubObject().get().getString().get(), is("value"));
        assertThat(decoded.getDoubleValue().get(), is(6.0d));
    }

    @Test public void test_multiple_attributes() {
        Observable_Multiple multiple = new Observable_Multiple();

        multiple.getString().set("a");
        multiple.getBool().set(false);
        multiple.getInteger().set(1);
        multiple.getLongValue().set(2);
        multiple.getDoubleValue().set(3.4d);

        String xml = multiple.toXml("multiple");
        assertThat(xml, is("<multiple string=\"a\" bool=\"false\" integer=\"1\" longValue=\"2\" doubleValue=\"3.4\"/>"));
        assertThat(multiple.toJSON("multiple"), is("{\"multiple\":{\"string\":\"a\",\"bool\":false,\"integer\":1,\"longValue\":2,\"doubleValue\":3.4}}"));
        assertThat(multiple.toJSON("multiple", false, false), is("{\"string\":\"a\",\"bool\":false,\"integer\":1,\"longValue\":2,\"doubleValue\":3.4}"));

        Observable_Multiple decoded = new Observable_Multiple();
        decoded.fromXml(xml);

        assertThat(decoded.getString().get(), is("a"));
        assertThat(decoded.getBool().get(), is(false));
        assertThat(decoded.getInteger().get(), is(1));
        assertThat(decoded.getLongValue().get(), is(2L));
        assertThat(decoded.getDoubleValue().get(), is(3.4d));
    }

    @Test public void test_with_object_list() {

        Observable_With_List withList = new Observable_With_List();

        withList.getString().set("a");
        withList.getSubObjects().add(new Observable_Single("item 1"));
        withList.getSubObjects().add(new Observable_Single("item 2"));

        String xml = withList.toXml("withList");
        assertThat(xml, is("<withList string=\"a\">\r\n<subObjects>\r\n<observable_single string=\"item 1\"/>\r\n<observable_single string=\"item 2\"/>\r\n</subObjects>\r\n</withList>"));
        assertThat(withList.toJSON("withList"), is("{\"withList\":{\"string\":\"a\",\"subObjects\":{\"observable_single\":[{\"string\":\"item 1\"},{\"string\":\"item 2\"}]}}}"));
        
        Observable_With_List decoded = new Observable_With_List();
        decoded.fromXml(xml);
        assertThat(decoded.getString().get(), is("a"));
        assertThat(decoded.getSubObjects().size(), is(2));
        assertThat(decoded.getSubObjects().get(0).getString().get(), is("item 1"));
        assertThat(decoded.getSubObjects().get(1).getString().get(), is("item 2"));

    }

    // /////////////////////////
    // Fixtures
    // //////////////////////////

    public static class Observable_Empty extends Observable {

    }

    public static class Observable_Single extends Observable {

        private ObservableProperty<String> string = createStringProperty("string", "initial");

        public Observable_Single() {}

        public Observable_Single(String value) {
            string.set(value);
        }

        public ObservableProperty<String> getString() {
            return string;
        }
    }

    public static class Observable_With_Just_Element extends Observable {

        private ObservableProperty<Observable_Single> subObject = createProperty("subObject", Observable_Single.class, new Observable_Single());

        public ObservableProperty<Observable_Single> getSubObject() {
            return subObject;
        }
    }
    
    public static class Observable_With_Multiple_Elements extends Observable {

        private ObservableProperty<Observable_Single> sub1 = createProperty("subObject1", Observable_Single.class, new Observable_Single());
        private ObservableProperty<Observable_Single> sub2 = createProperty("subObject2", Observable_Single.class, new Observable_Single());
        private ObservableProperty<Observable_Single> sub3 = createProperty("subObject3", Observable_Single.class, new Observable_Single());

        public ObservableProperty<Observable_Single> getSub1() {
            return sub1;
        }
        
        public ObservableProperty<Observable_Single> getSub2() {
            return sub2;
        }
        
        public ObservableProperty<Observable_Single> getSub3() {
            return sub3;
        }
        
    }

    public static class Observable_With_Both extends Observable {

        private ObservableProperty<String> string = createStringProperty("string", "initial");
        private ObservableDouble doubleValue = createDoubleProperty("doubleValue", 23.2);
        private ObservableProperty<Observable_Single> subObject = createProperty("subObject", Observable_Single.class, new Observable_Single());

        public ObservableDouble getDoubleValue() {
            return doubleValue;
        }
        
        public ObservableProperty<String> getString() {
            return string;
        }

        public ObservableProperty<Observable_Single> getSubObject() {
            return subObject;
        }
    }

    public static class Observable_With_List extends Observable {

        private ObservableProperty<String> string = createStringProperty("string", "initial");
        private ObservableList<Observable_Single> subObjects = createListProperty("subObjects", Observable_Single.class);

        public ObservableProperty<String> getString() {
            return string;
        }

        public ObservableList<Observable_Single> getSubObjects() {
            return subObjects;
        }
    }

    public static class Observable_Multiple extends Observable {

        private ObservableProperty<String> string = createStringProperty("string", "initial");
        private ObservableProperty<Boolean> bool = createBooleanProperty("bool", true);
        private ObservableInteger integer = createIntProperty("integer", 666);
        private ObservableLong longValue = createLongProperty("longValue", 333L);
        private ObservableDouble doubleValue = createDoubleProperty("doubleValue", 12.4d);

        public ObservableDouble getDoubleValue() {
            return doubleValue;
        }

        public ObservableProperty<Boolean> getBool() {
            return bool;
        }

        public ObservableInteger getInteger() {
            return integer;
        }

        public ObservableLong getLongValue() {
            return longValue;
        }

        public ObservableProperty<String> getString() {
            return string;
        }
    }

    public static class Observable_Multiple_With_List_Recursive extends Observable {

        private ObservableProperty<String> string = createStringProperty("string", "initial");
        private ObservableProperty<Boolean> bool = createBooleanProperty("boolean", true);
        private ObservableInteger integer = createIntProperty("integer", 666);
        private ObservableLong longValue = createLongProperty("longValue", 333L);

        private ObservableList<Observable_Multiple_With_List_Recursive> list = createListProperty("list", Observable_Multiple_With_List_Recursive.class);

        public ObservableList<Observable_Multiple_With_List_Recursive> getList() {
            return list;
        }

        public ObservableProperty<Boolean> getBool() {
            return bool;
        }

        public ObservableInteger getInteger() {
            return integer;
        }

        public ObservableLong getLongValue() {
            return longValue;
        }

        public ObservableProperty<String> getString() {
            return string;
        }
    }

    public static class Observable_Multiple_With_Base_Type_List extends Observable {
        private ObservableList<String> stringList = createListProperty("stringList", String.class);
        private ObservableList<Integer> intList = createListProperty("intList", Integer.class);

        public ObservableList<String> getStringList() {
            return stringList;
        }

        public ObservableList<Integer> getIntList() {
            return intList;
        }
    }

}
