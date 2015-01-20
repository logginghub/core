package com.logginghub.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.ThreadUtils;

public class TestJettyLauncher {

    private JettyLauncher launcher;
    private WebClient webClient;
    private Controller controller;

    @Before public void setup() throws Exception {
        int freePort = NetUtils.findFreePort();
        // Make sure the port gets freed up
        ThreadUtils.sleep(100);
        launcher = new JettyLauncher();
        launcher.setHttpPort(freePort);
        controller = new Controller();
        launcher.setHandler(new ReflectionHandler(controller));
        launcher.start();

        webClient = new WebClient();
    }

    @After public void teardown() {
        launcher.stop();
    }
    
    @Test public void test_end_point_annotation() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        TextPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/get/something");
        String content = page.getContent();
        assertThat(content, is("something"));
    }

    @Test public void test_static_file() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        TextPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/static.css");
        String content = page.getContent();
        assertThat(content, is("/* I'm a style sheet */"));
    }

    @Test public void test_method_invoke_void_void() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/simpleMethod");
        assertThat(page.asText(), is(""));
        assertThat(controller.simpleMethodCalls, is(1));
    }

    @Test public void test_method_invoke_string_void() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/simpleMethodThatReturns");
        assertThat(page.asText(), is("Return string"));
    }

    @Test public void test_method_invoke_object_void() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        // The result is encoded in json, which html unit doesn't try and decode
        // for us
        UnexpectedPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/simpleMethodThatReturnsObject");
        String result = FileUtils.read(page.getInputStream());
        assertThat(result, is("true"));
    }

    @Test public void test_method_invoke_string_string() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HtmlPage page = webClient.getPage("http://localhost:" + launcher.getHttpPort() + "/simpleMethodThatReturnsWithParam/something");
        assertThat(page.asText(), is("Return string : something"));

    }

}
