package com.logginghub.logging.log4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for a null pointer in the SocketConnector that was stopping the log4j reloading working
 * @author James
 *
 */
public class TestReloadingLogging
{
    private PrintStream out;
    private ByteArrayOutputStream baos;

    @Before
    public void redirectStdOut(){
        out = System.out;
        baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos); 
        System.setOut(newOut);
    }
    
    @Test public void test() throws InterruptedException
    {        
        System.setProperty("log4j.debug", "true");
        PropertyConfigurator.configureAndWatch("src/test/resources/__log4j.properties__", 1000);       
        assertThat(new String(baos.toByteArray()), is(not(containsString("log4j: [src/test/resources/log4j.properties] does not exist."))));
        Logger.getRootLogger().getAppender("socket").close();
        
    }       
    
    @After
    public void restoreOut(){
        System.setOut(out);
    }
}
