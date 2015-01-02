package com.logginghub.utils.module;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.StringUtils.StringUtilsBuilder;
import com.logginghub.utils.module.ClassResolver;
import com.logginghub.utils.module.Container2;
import com.logginghub.utils.module.ResolutionNotPossibleException;

public class TestContainer2 {

    private Container2 container = new Container2();

    @Before public void setup() {
        container.addClassResolver(new ClassResolver() {
            public String resolve(String name) {
                return "com.logginghub.utils.module." + StringUtils.capitalise(name);
            }
        });
    }

    @Test public void test_instantiate_and_configure() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.append("<container><stubObject id='a' stringValue='test'/></container>");

        container.fromXmlString(builder.toString());

        assertThat(container.getInstancesByID().get("a"), is(not(nullValue())));

        StubObject test = (StubObject) container.getInstances().get(0);
        assertThat(test.getStringValue(), is("test"));

    }
    
    @Test public void test_instantiate_and_configure_with_list() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubObjectWithList>");
        builder.appendLine("     <object stringValue='1'/>");
        builder.appendLine("     <object stringValue='2'/>");
        builder.appendLine("     <object stringValue='3'/>");
        builder.appendLine("  </stubObjectWithList>");
        builder.appendLine("</container>");
        String configuration = builder.toString();
        
        container.fromXmlString(configuration);

        assertThat(container.getInstances().size(), is(1));
        
        StubObjectWithList stub = (StubObjectWithList)container.getInstances().get(0);
        
        assertThat(stub.getObjects().size(), is(3));
        assertThat(stub.getObjects().get(0).getStringValue(), is("1"));
        assertThat(stub.getObjects().get(1).getStringValue(), is("2"));
        assertThat(stub.getObjects().get(2).getStringValue(), is("3"));
    }

    @Test public void test_two_levels() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubObject stringValue='outer'>");
        builder.appendLine("    <stubObject stringValue='inner'>");
        builder.appendLine("  </stubObject>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubObject outer = (StubObject) container.getInstances().get(0);
        StubObject inner = (StubObject) container.getChildren(outer).getInstances().get(0);

        assertThat(outer.getStringValue(), is("outer"));
        assertThat(inner.getStringValue(), is("inner"));

    }

    @Test public void test_auto_wire_via_constructor() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("<stubProvider />");
        builder.appendLine("<stubConsumerViaConstructor />");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider provider = (StubProvider) container.getInstances().get(0);
        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) container.getInstances().get(1);

        assertThat(consumer.getService(), is((StubService) provider));

    }
    

    @Test public void test_auto_wire_via_annotation() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("<stubProvider />");
        builder.appendLine("<stubConsumerViaInjectAnnotation />");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider provider = (StubProvider) container.getInstances().get(0);
        StubConsumerViaInjectAnnotation consumer = (StubConsumerViaInjectAnnotation) container.getInstances().get(1);

        assertThat(consumer.getService(), is((StubService) provider));

    }
    
    @Test public void test_auto_wire_via_annotation_multi_level() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider id='level1'>");
        builder.appendLine("    <stubConsumerViaInjectAnnotation />");
        builder.appendLine("    <stubProvider id='level2'>");
        builder.appendLine("      <stubConsumerViaInjectAnnotation />");
        builder.appendLine("      <stubProvider id='level3'>");
        builder.appendLine("        <stubConsumerViaInjectAnnotation />");
        builder.appendLine("      </stubProvider>");
        builder.appendLine("    </stubProvider>");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider level1Provider = (StubProvider) container.getInstances().get(0);
        StubConsumerViaInjectAnnotation level1Consumer = (StubConsumerViaInjectAnnotation) container.getChildren(level1Provider).getInstances().get(0);
        
        StubProvider level2Provider = (StubProvider) container.getChildren(level1Provider).getInstances().get(1);
        StubConsumerViaInjectAnnotation level2Consumer = (StubConsumerViaInjectAnnotation) container.getChildren(level1Provider).getChildren(level2Provider).getInstances().get(0);
        
        StubProvider level3Provider = (StubProvider) container.getChildren(level1Provider).getChildren(level2Provider).getInstances().get(1);
        StubConsumerViaInjectAnnotation level3Consumer = (StubConsumerViaInjectAnnotation) container.getChildren(level1Provider).getChildren(level2Provider).getChildren(level3Provider).getInstances().get(0);

        assertThat(level1Consumer.getService(), is((StubService)level1Provider));
        assertThat(level2Consumer.getService(), is((StubService)level2Provider));
        assertThat(level3Consumer.getService(), is((StubService)level3Provider));
    }
    

    @Test public void test_auto_wire_via_annotation_just_parent() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider id='parent'>");
        builder.appendLine("    <stubProvider id='sibling'/>");
        builder.appendLine("    <stubConsumerViaInjectAnnotationWithParentOnlyDirection />");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider outerProvider = (StubProvider) container.getInstances().get(0);
        StubProvider innerProvider = (StubProvider) container.getChildren(outerProvider).getInstances().get(0);
        StubConsumerViaInjectAnnotationWithParentOnlyDirection consumer = (StubConsumerViaInjectAnnotationWithParentOnlyDirection) container.getChildren(outerProvider).getInstances().get(1);

        assertThat(consumer.getService(), is((StubService) outerProvider));

    }
    
    @Test public void test_auto_wire_prefers_parent_via_constructor() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider id='providerA'>");
        builder.appendLine("    <stubConsumerViaConstructor />");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("  <stubProvider id='providerB'>");
        builder.appendLine("    <stubConsumerViaConstructor />");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider providerA = (StubProvider) container.getInstances().get(0);
        StubProvider providerB = (StubProvider) container.getInstances().get(1);
        
        StubConsumerViaConstructor consumerA = (StubConsumerViaConstructor) container.getChildren(providerA).getInstances().get(0);
        StubConsumerViaConstructor consumerB = (StubConsumerViaConstructor) container.getChildren(providerB).getInstances().get(0);

        assertThat(providerA.getId(), is("providerA"));
        assertThat(providerB.getId(), is("providerB"));
        
        assertThat(consumerA.getService(), is((StubService)providerA));
        assertThat(consumerB.getService(), is((StubService)providerB));


    }
    
    @Test public void test_auto_wire_prefers_parent_via_annotation() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider id='providerA'>");
        builder.appendLine("    <stubConsumerViaInjectAnnotation />");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("  <stubProvider id='providerB'>");
        builder.appendLine("    <stubConsumerViaInjectAnnotation />");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider providerA = (StubProvider) container.getInstances().get(0);
        StubProvider providerB = (StubProvider) container.getInstances().get(1);
        
        StubConsumerViaInjectAnnotation consumerA = (StubConsumerViaInjectAnnotation) container.getChildren(providerA).getInstances().get(0);
        StubConsumerViaInjectAnnotation consumerB = (StubConsumerViaInjectAnnotation) container.getChildren(providerB).getInstances().get(0);

        assertThat(providerA.getId(), is("providerA"));
        assertThat(providerB.getId(), is("providerB"));
        
        assertThat(consumerA.getService(), is((StubService)providerA));
        assertThat(consumerB.getService(), is((StubService)providerB));


    }

    @Test public void test_auto_wire_from_parent() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider>");
        builder.appendLine("    <stubConsumerViaConstructor>");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubProvider provider = (StubProvider) container.getInstances().get(0);
        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) container.getChildren(provider).getInstances().get(0);

        assertThat(consumer.getService(), is((StubService) provider));

    }

    @Test public void test_auto_wire_with_id_from_parent() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubProvider id='a'>");
        builder.appendLine("    <stubConsumerViaConstructor stubServiceRef='b'/>");
        builder.appendLine("  </stubProvider>");
        builder.appendLine("  <stubProvider id='b'/>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());
        assertThat(container.getInstances().size(), is(2));

        StubProvider providerA = (StubProvider) container.getInstances().get(0);
        assertThat(providerA.getId(), is("a"));

        StubProvider providerB = (StubProvider) container.getInstances().get(1);
        assertThat(providerB.getId(), is("b"));

        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) container.getChildren(providerA).getInstances().get(0);

        assertThat(consumer.getService(), is((StubService) providerB));

    }

    @Test public void test_auto_wire_with_id() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("<stubConsumerViaConstructor stubServiceRef='b'/>");
        builder.appendLine("<stubProvider id='a'/>");
        builder.appendLine("<stubProvider id='b'/>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) container.getInstances().get(0);
        StubProvider providerA = (StubProvider) container.getInstances().get(1);
        StubProvider providerB = (StubProvider) container.getInstances().get(2);

        assertThat(providerA.getId(), is("a"));
        assertThat(providerB.getId(), is("b"));

        assertThat(consumer.getService(), is((StubService) providerB));

    }

    @Test public void test_auto_wire_bad_ordering() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("<stubConsumerViaConstructor />");
        builder.appendLine("<stubProvider />");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) container.getInstances().get(0);
        StubProvider provider = (StubProvider) container.getInstances().get(1);

        assertThat(consumer.getService(), is((StubService) provider));

    }

    @Test public void test_auto_wire_bad_ordering_in_children() throws Exception {
        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("  <stubObject stringValue='outer'>");
        builder.appendLine("    <stubConsumerViaConstructor />");
        builder.appendLine("    <stubProvider />");
        builder.appendLine("  </stubObject>");
        builder.appendLine("</container>");

        container.fromXmlString(builder.toString());

        StubObject outer = (StubObject) container.getInstances().get(0);

        Container2 innerContainer = container.getChildren(outer);

        StubConsumerViaConstructor consumer = (StubConsumerViaConstructor) innerContainer.getInstances().get(0);
        StubProvider provider = (StubProvider) innerContainer.getInstances().get(1);

        assertThat(consumer.getService(), is((StubService) provider));

    }

    @Test public void test_auto_wire_impossible_ordering() throws Exception {

        StringUtilsBuilder builder = new StringUtilsBuilder();
        builder.appendLine("<container>");
        builder.appendLine("<stubConsumerViaConstructor />");
        builder.appendLine("<stubObject/>");
        builder.appendLine("</container>");

        try {
            container.fromXmlString(builder.toString());

            fail();
        }
        catch (ResolutionNotPossibleException e) {

        }

    }


}
