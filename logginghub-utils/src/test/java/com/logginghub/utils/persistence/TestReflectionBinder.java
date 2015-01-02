package com.logginghub.utils.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.logginghub.utils.persistence.Bag;
import com.logginghub.utils.persistence.ReflectionBinder;

public class TestReflectionBinder {

    private ReflectionBinder binder = new ReflectionBinder();

    static {
//        Logger.setLevel(ReflectionBinder.class, Logger.trace);
    }

    @Test public void testFromObject() throws Exception {

        AllTypesDummyObject object = new AllTypesDummyObject();
        object.setStringObject("this is a new string");
        object.setIntegerObject(Integer.valueOf(3452));
        object.setBigDecimalObject(null);
        object.setStringArrayObject(new String[] { "sa", "sb", "sc" });

        Bag bag = binder.fromObject(object);

        assertThat(bag.getString("stringObject"), is("this is a new string"));
        assertThat(bag.getInteger("integerObject"), is(3452));
        assertThat(bag.get("bigDecimalObject"), is(nullValue()));
        assertThat(bag.getStringArray("stringArrayObject")[1], is("sb"));

        AllTypesDummyObject decoded = binder.toObject(bag, AllTypesDummyObject.class);
        
        assertThat(decoded.getStringObject(), is("this is a new string"));
        assertThat(decoded.getIntegerObject(), is(3452));
        assertThat(decoded.getBigDecimalObject(), is(nullValue()));
        assertThat(decoded.getStringArrayObject()[1], is("sb"));
        
    }

}
