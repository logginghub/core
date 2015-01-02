package com.logginghub.utils.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.logginghub.utils.Destination;
import com.logginghub.utils.module.ProxyServiceDiscovery;


public class TestProxyServiceDiscovery {

    public interface Foo {

    }

    public static class ConcreteA {

    }

    public static class ConcreteB {

    }

    public class ProviderA implements Destination<ConcreteA> {
        public void send(ConcreteA t) {}
    }
    
    public class ProviderB implements Destination<ConcreteB> {
        public void send(ConcreteB t) {}
    }

    @Test public void test_ambigous_generic() throws Exception {
        
        ProxyServiceDiscovery discovery = new ProxyServiceDiscovery();
        ProviderA providerA = new ProviderA();
        ProviderB providerB = new ProviderB();
        ProviderB providerBNamed = new ProviderB();
        
        assertThat(discovery.findService(Destination.class), is(not(nullValue())));
        assertThat(discovery.findService(Destination.class, ConcreteA.class), is(not(nullValue())));
        assertThat(discovery.findService(Destination.class, ConcreteB.class), is(not(nullValue())));
        assertThat(discovery.findService(Destination.class, ConcreteA.class, "alternative"), is(not(nullValue())));
        assertThat(discovery.findService(Destination.class, ConcreteB.class, "alternative"), is(not(nullValue())));
        
    }
    
    @Test public void test_interface() throws Exception {

        ProxyServiceDiscovery discovery = new ProxyServiceDiscovery();

        assertThat(discovery.findService(Foo.class), is(not(nullValue())));

        Foo fooA = new Foo() {};

        assertThat(discovery.findService(Foo.class), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class, "fooA"), is(not(nullValue())));

        Foo fooB = new Foo() {};

        assertThat(discovery.findService(Foo.class), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class, "fooA"), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class, "fooB"), is(not(nullValue())));

        assertThat(discovery.findService(Foo.class), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class, "fooA"), is(not(nullValue())));
        assertThat(discovery.findService(Foo.class, "fooB"), is(not(nullValue())));

    }
}
