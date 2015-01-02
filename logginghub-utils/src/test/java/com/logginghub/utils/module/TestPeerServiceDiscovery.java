package com.logginghub.utils.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.logginghub.utils.Destination;
import com.logginghub.utils.ExceptionPolicy.Policy;
import com.logginghub.utils.NamedModule;

public class TestPeerServiceDiscovery {

    public interface Foo {

    }

    public static class ConcreteA {

    }

    public static class ConcreteB {

    }

    @Provides(ConcreteA.class) public class ProviderA implements Destination<ConcreteA>, Module<String> {
        public void send(ConcreteA t) {}

        public void configure(String configuration, ServiceDiscovery discovery) {}

        public void start() {}

        public void stop() {}
    }

    @Provides(ConcreteB.class) public class ProviderB implements Destination<ConcreteB>, NamedModule<String> {
        private String name;

        public ProviderB() {

        }

        public ProviderB(String name) {
            this.name = name;
        }

        public void send(ConcreteB t) {}

        public void configure(String configuration, ServiceDiscovery discovery) {}

        public void start() {}

        public void stop() {}

        public String getName() {
            return name;
        }
    }

    @Test public void test_ambigous_generic() throws Exception {

        ProviderA providerA = new ProviderA();
        ProviderB providerB = new ProviderB();
        ProviderB providerBNamed = new ProviderB("alternative");

        Container container = new Container();
        container.getModules().add(providerA);
        container.getModules().add(providerB);
        container.getModules().add(providerBNamed);

        container.getSuccessfullyConfiguredModules().add(providerA);
        
        PeerServiceDiscovery discovery = new PeerServiceDiscovery(container);
        discovery.getExceptionPolicy().setPolicy(Policy.Ignore);

        assertThat(discovery.findService(Destination.class), is((Destination) providerA));
        assertThat(discovery.findService(Destination.class, ConcreteA.class), is((Destination) providerA));
        assertThat(discovery.findService(Destination.class, ConcreteB.class), is((Destination) providerB));
        assertThat(discovery.findService(Destination.class, ConcreteA.class, "alternative"), is(nullValue()));
        assertThat(discovery.findService(Destination.class, ConcreteB.class, "alternative"), is((Destination) providerBNamed));
    }

}
