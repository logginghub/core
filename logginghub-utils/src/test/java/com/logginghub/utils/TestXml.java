package com.logginghub.utils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestXml {
 
    @Test public void testXml() {
        Xml xml = new Xml("<outer><inner value='foo'/></outer>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("inner"));
        assertThat(xml.getRoot().getChildren().get(0).getAttribute("value"), is("foo"));
    }



    @Test public void test_quoted_slash_in_attributes() {
        Xml xml = new Xml("<outer><inner value=\"/path/path/path/\"/></outer>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("inner"));
        assertThat(xml.getRoot().getChildren().get(0).getAttribute("value"), is("/path/path/path/"));
    }
    
    @Test public void test_single_quotes_in_attribute() { 
        Xml xml = new Xml("<outer><inner value=\"foo with 'inner quoted bit' oh yeah\"/></outer>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("inner"));
        assertThat(xml.getRoot().getChildren().get(0).getAttribute("value"), is("foo with 'inner quoted bit' oh yeah"));
    }
    
    @Test public void test_mutliline_element() {
        Xml xml = new Xml("<outer>This is a message\r\non two lines</outer>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getElementData(), is("This is a message\r\non two lines"));
    }
    
    
    @Test public void test_quotes_double() {
        Xml xml = new Xml("<Location name=\"Bournemouth - Fisherman's Walk (Beach)\" region=\"sw\" unitaryAuthArea=\"Bournemouth\"></Location>");
        assertThat(xml.getRoot().getAttribute("name"), is("Bournemouth - Fisherman's Walk (Beach)"));
    }

    @Test public void test_quotes_single() {
        Xml xml = new Xml("<Location name='Bournemouth - Fisherman\"s Walk (Beach)' region='sw' unitaryAuthArea='Bournemouth'></Location>");
        assertThat(xml.getRoot().getAttribute("name"), is("Bournemouth - Fisherman\"s Walk (Beach)"));
    }
    
    @Test public void test_newlines() {
        
        Xml xml = new Xml("\r\n<outer>\r\nThis is a message\r\non two lines\r\n<inner element='value'\r\n/></outer>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getElementData(), is("This is a message\r\non two lines"));
        assertThat(xml.getRoot().getNode("inner").getAttribute("element"), is("value"));
    }

    @Test public void test_newlines2() {
        Xml xml = new Xml("\n<\ninner\nelement\n=\n'value with spaces'\n element2='another value'\n /\n>\n");
        assertThat(xml.getRoot().getTagName(), is("inner"));
        assertThat(xml.getRoot().getAttribute("element"), is("value with spaces"));
        assertThat(xml.getRoot().getAttribute("element2"), is("another value"));
    }

    @Test public void test_newlines3() {
        Xml xml = new Xml("\n<\ninner\nelement\n=\n'value'\n\n>\n<\n/\ninner\n>\n");
        assertThat(xml.getRoot().getTagName(), is("inner"));
        assertThat(xml.getRoot().getAttribute("element"), is("value"));
    }

    @Test public void test_newlines4() {
        Xml xml = new Xml("\n<\nouter\n>\nThis is a message\non two lines\n<\ninner\nelement\n=\n'value'\n/\n>\n<\n/outer\n>");
        assertThat(xml.getRoot().getTagName(), is("outer"));
        assertThat(xml.getRoot().getElementData(), is("This is a message\non two lines"));
        assertThat(xml.getRoot().getNode("inner").getAttribute("element"), is("value"));
    }

    @Test public void testXml2() {

        Xml xml = new Xml("<html><body><table><tr><td><a href='foo'>link</a></td></tr></table></body></html>");

        assertThat(xml.getRoot().getTagName(), is("html"));
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("body"));
        assertThat(xml.path("html.body.table.tr.td.a.href"), is("foo"));
        
        assertThat(xml.pathRelaxed("html", "body","table", "tr", "td", "a", "href"), is("foo"));
        assertThat(xml.pathRelaxed("html", "body","table", "tr", "td", "a"), is("link"));
    }

    @Test public void testMultipleChildElements() {

        Xml xml = new Xml("<project><a>a</a><b>b</b><c>c</c></project>");

        assertThat(xml.getRoot().getTagName(), is("project"));
        assertThat(xml.getRoot().getChildren().size(), is(3));
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("a"));
        assertThat(xml.getRoot().getChildren().get(1).getTagName(), is("b"));
        assertThat(xml.getRoot().getChildren().get(2).getTagName(), is("c"));
        assertThat(xml.getRoot().getChildren().get(0).getElementData(), is("a"));
        assertThat(xml.getRoot().getChildren().get(1).getElementData(), is("b"));
        assertThat(xml.getRoot().getChildren().get(2).getElementData(), is("c"));
        assertThat(xml.path("project.b"), is("b"));

    }

    @Test public void testMultipleNestedChildElements() {

        Xml xml = new Xml("<project>" + "<a><a1>a1</a1><a2>a2</a2><a3>a3</a3></a>" + "<b><b1>b1</b1><b2>b2</b2><b3>b3</b3></b>" + "<c><c1>c1</c1><c2>c2</c2><c3>c3</c3></c>" + "</project>");

        assertThat(xml.getRoot().getTagName(), is("project"));
        assertThat(xml.getRoot().getChildren().size(), is(3));
        
        assertThat(xml.getRoot().getChildren().get(0).getChildren().size(), is(3));
        assertThat(xml.getRoot().getChildren().get(1).getChildren().size(), is(3));
        assertThat(xml.getRoot().getChildren().get(2).getChildren().size(), is(3));
        
        assertThat(xml.getRoot().getChildren().get(0).getTagName(), is("a"));
        assertThat(xml.getRoot().getChildren().get(1).getTagName(), is("b"));
        assertThat(xml.getRoot().getChildren().get(2).getTagName(), is("c"));
        
        assertThat(xml.getRoot().getChildren().get(0).getChildren().get(0).getElementData(), is("a1"));
        assertThat(xml.getRoot().getChildren().get(0).getChildren().get(1).getElementData(), is("a2"));
        assertThat(xml.getRoot().getChildren().get(0).getChildren().get(2).getElementData(), is("a3"));
        
        assertThat(xml.getRoot().getChildren().get(1).getChildren().get(0).getElementData(), is("b1"));
        assertThat(xml.getRoot().getChildren().get(1).getChildren().get(1).getElementData(), is("b2"));
        assertThat(xml.getRoot().getChildren().get(1).getChildren().get(2).getElementData(), is("b3"));
        
        assertThat(xml.getRoot().getChildren().get(2).getChildren().get(0).getElementData(), is("c1"));
        assertThat(xml.getRoot().getChildren().get(2).getChildren().get(1).getElementData(), is("c2"));
        assertThat(xml.getRoot().getChildren().get(2).getChildren().get(2).getElementData(), is("c3"));

        assertThat(xml.path("project.b.b2"), is("b2"));

    }

    @Test public void testPathing() {

        Xml xml = new Xml("<html><inner value='first'/><inner value='second'/><deep><nested attribute='12'>value</nested></deep></html>");

        assertThat(xml.path("html.inner.value"), is("first"));
        assertThat(xml.path("html.deep.nested"), is("value"));
        assertThat(xml.path("html.deep.nested.attribute"), is("12"));

        assertThat(xml.getRoot().path("inner.value"), is("first"));
        assertThat(xml.getRoot().path("deep.nested"), is("value"));
        assertThat(xml.getRoot().path("deep.nested.attribute"), is("12"));

    }
    
    @Test public void test_node_path() {

        Xml xml = new Xml("<html><inner value='first'/><inner value='second'/><deep><nested attribute='12'>value</nested></deep></html>");

        assertThat(xml.nodePath("html.inner").getAttribute("value"), is("first"));
        assertThat(xml.nodePath("html.deep.nested").getElementData(), is("value"));
        assertThat(xml.nodePath("html.deep.nested").getAttribute("attribute"), is("12"));

        assertThat(xml.getRoot().nodePath("inner").getAttribute("value"), is("first"));
        assertThat(xml.getRoot().nodePath("deep.nested").getElementData(), is("value"));
        assertThat(xml.getRoot().nodePath("deep.nested").getAttribute("attribute"), is("12"));

    }

    @Test public void test_node_path_indexing() {

        Xml xml = new Xml("<html><head></head><body><table id='rounded-corner'><thead ><tr><th>Name</th><th>Total Runs</th></tr></thead><tbody><tr><td><a href='test/MyTest'>MyTest</a></td><td>1</td></tr></tbody></table></body></html>");

        assertThat(xml.nodePath("html.body.table.tbody").find("tr").size(), is(1));
        assertThat(xml.nodePath("html.body.table.tbody.tr.td[0].a").getElementData(), is("MyTest"));
        assertThat(xml.nodePath("html.body.table.tbody.tr.td[1]").getElementData(), is("1"));
    }
    
    @Test public void testComments() {
        Xml xml = new Xml("<html><!-- comment --><value boo='foo'/></html>");
        assertThat(xml.path("html.value.boo"), is("foo"));
    }
    
    @Test public void test_comments_with_new_lines() {
        Xml xml = new Xml("<html>\r\n<!-- <value boo='noooo'/> -->\r\n<value boo='foo'/></html>");
        assertThat(xml.path("html.value.boo"), is("foo"));
    }
    
    @Test public void test_comments_no_space() {
        Xml xml = new Xml("<html><!--comment--><value boo='foo'/></html>");
        assertThat(xml.path("html.value.boo"), is("foo"));
    }
    
    @Test public void test_nested_empty_element() {
        
        Xml xml = new Xml("<html><developer><name>My name</name><organisation/></developer></html>");
        assertThat(xml.path("html.developer.organisation"), is(""));
    }
    
    @Test public void testProlog() {
        Xml xml = new Xml("<?xml version='1.0'?><settings xmlns='http://maven.apache.org/POM/4.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'><value>value</value></settings>");
        assertThat(xml.path("settings.value"), is("value"));
    }

    @Test public void test_leading_cr() {
        Tracer.enable();
        Xml xml = new Xml("\n<html><developer><name>My name</name><organisation/></developer></html>");
        assertThat(xml.getRoot().getTagName(), is("html"));
        assertThat(xml.path("html.developer.name"), is("My name"));
    }
    
}
