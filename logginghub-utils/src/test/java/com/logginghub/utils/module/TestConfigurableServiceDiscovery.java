package com.logginghub.utils.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

public class TestConfigurableServiceDiscovery {

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
        
        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.getExceptionPolicy().setPolicy(Policy.SystemErr);
        
        ProviderA providerA = new ProviderA();
        ProviderB providerB = new ProviderB();
        ProviderB providerBNamed = new ProviderB();
        
        discovery.bind(Destination.class, ConcreteA.class, providerA);
        discovery.bind(Destination.class, ConcreteB.class, providerB);
        discovery.bind(Destination.class, ConcreteB.class, "alternative", providerBNamed);
        
        assertThat(discovery.findService(Destination.class), is(nullValue()));
        assertThat(discovery.findService(Destination.class, ConcreteA.class), is((Destination)providerA));
        assertThat(discovery.findService(Destination.class, ConcreteB.class), is((Destination)providerB));
        assertThat(discovery.findService(Destination.class, ConcreteA.class, "alternative"), is(nullValue()));
        assertThat(discovery.findService(Destination.class, ConcreteB.class, "alternative"), is((Destination)providerBNamed));
        
    }
    
    @Test public void test_interface() throws Exception {

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.getExceptionPolicy().setPolicy(Policy.Ignore);

        assertThat(discovery.findService(Foo.class), is(nullValue()));

        Foo fooA = new Foo() {};

        discovery.bind(Foo.class, "fooA", fooA);

        assertThat(discovery.findService(Foo.class), is(nullValue()));
        assertThat(discovery.findService(Foo.class, "fooA"), is(fooA));

        Foo fooB = new Foo() {};
        discovery.bind(Foo.class, fooB);

        assertThat(discovery.findService(Foo.class), is(fooB));
        assertThat(discovery.findService(Foo.class), is(fooB));
        assertThat(discovery.findService(Foo.class, "fooA"), is(fooA));
        assertThat(discovery.findService(Foo.class, "fooB"), is(nullValue()));

        discovery.bind(Foo.class, "fooB", fooB);

        assertThat(discovery.findService(Foo.class), is(fooB));
        assertThat(discovery.findService(Foo.class), is(fooB));
        assertThat(discovery.findService(Foo.class, "fooA"), is(fooA));
        assertThat(discovery.findService(Foo.class, "fooB"), is(fooB));

    }

}
