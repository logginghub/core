package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.logginghub.logging.modules.GeneratorModule;

public class TestGeneratorModule {

    @Test public void testRandomiseValue() throws Exception {

        assertThat(GeneratorModule.randomiseValue("a[1],b[1]", 0), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1]", 0.4999), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1]", 0.5), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1]", 0.99999), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1]", 1), is("b"));

        assertThat(GeneratorModule.randomiseValue(" a[1] , b[1] ", 0), is("a"));
        assertThat(GeneratorModule.randomiseValue(" a[1] , b[1] ", 0.4999), is("a"));
        assertThat(GeneratorModule.randomiseValue(" a[1] , b[1] ", 0.5), is("b"));
        assertThat(GeneratorModule.randomiseValue(" a[1] , b[1] ", 0.99999), is("b"));
        assertThat(GeneratorModule.randomiseValue(" a[1] , b[1] ", 1), is("b"));

        assertThat(GeneratorModule.randomiseValue("a[1],b[2]", 0), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[2]", 0.3333), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[2]", 0.3334), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[2]", 0.99999), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[2]", 1), is("b"));

        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.24), is("a"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.25), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.49), is("b"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.50), is("c"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.74), is("c"));
        assertThat(GeneratorModule.randomiseValue("a[1],b[1],c[1],d[1]", 0.75), is("d"));
    }

}
