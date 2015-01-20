package com.logginghub.logging.modules;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import com.logginghub.logging.LogEvent;
import com.logginghub.logging.container.LoggingContainer;
import com.logginghub.logging.utils.LogEventBucket;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.FileUtilsWriter;
import com.logginghub.utils.ThreadUtils;
import com.logginghub.utils.module.ConfigurableServiceDiscovery;

@Ignore // This needs to be moved to an integration test!
public class TestSQLExtractModule {

    @Test public void test_success() throws Exception {

        File file = FileUtils.createRandomTestFileForClass(getClass());
        FileUtilsWriter writer = new FileUtilsWriter(file);

        writer.appendLine("<container>");
        writer.appendLine("<sqlExtract>");
        writer.appendLine("<database name='db1' user='sa' password='' url='jdbc:hsqldb:mem:aname' properties='ifexists=true'/>");
        writer.appendLine("<query sql='select * from test_table' pattern='This is column 1 {column1} this is column 2 {column2}' database='db1' initialDelay='1 second' repeatDelay='1 second' />");
        writer.appendLine("</sqlExtract>");
        writer.appendLine("</container>");

        writer.close();

        LogEventBucket bucket = new LogEventBucket();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(Destination.class, LogEvent.class, bucket);

        LoggingContainer container = LoggingContainer.fromFile(file, discovery);

        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:aname", "sa", "");

        Statement statement = c.createStatement();

        statement.execute("CREATE TABLE test_table (column1 int, column2 varchar(255));");

        statement.execute("INSERT INTO test_table (column1, column2) values (1, 'test1')");

        container.start();

        bucket.waitForMessages(1);

        assertThat(bucket.get(0).getMessage(), is("This is column 1 1 this is column 2 test1"));

        statement.execute("INSERT INTO test_table (column1, column2) values (2, 'test2')");
        statement.execute("INSERT INTO test_table (column1, column2) values (3, 'test3')");

        bucket.waitForMessages(4);

        assertThat(bucket.get(0).getMessage(), is("This is column 1 1 this is column 2 test1"));
        assertThat(bucket.get(1).getMessage(), is("This is column 1 1 this is column 2 test1"));
        assertThat(bucket.get(2).getMessage(), is("This is column 1 2 this is column 2 test2"));
        assertThat(bucket.get(3).getMessage(), is("This is column 1 3 this is column 2 test3"));

        container.stop();

        statement.execute("SHUTDOWN");
    }

    @Test public void test_db_down() throws Exception {

        File file = FileUtils.createRandomTestFileForClass(getClass());
        FileUtilsWriter writer = new FileUtilsWriter(file);

        writer.appendLine("<container>");
        writer.appendLine("<sqlExtract>");
        writer.appendLine("<database name='db1' user='sa' password='' url='jdbc:hsqldb:mem:aname' properties='ifexists=true'/>");
        writer.appendLine("<query sql='select * from test_table' pattern='This is column 1 {column1} this is column 2 {column2}' database='db1' initialDelay='1 second' repeatDelay='1 second' />");
        writer.appendLine("</sqlExtract>");
        writer.appendLine("</container>");

        writer.close();

        LogEventBucket bucket = new LogEventBucket();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(Destination.class, LogEvent.class, bucket);

        LoggingContainer container = LoggingContainer.fromFile(file, discovery);

        // DO NOT START THE DB UP

        container.start();

        ThreadUtils.sleep(2000);

        assertThat(bucket.size(), is(0));

        container.stop();

    }

    @Test public void test_db_reconnect() throws Exception {

        File file = FileUtils.createRandomTestFileForClass(getClass());
        FileUtilsWriter writer = new FileUtilsWriter(file);

        writer.appendLine("<container>");
        writer.appendLine("<sqlExtract>");
        writer.appendLine("<database name='db1' user='sa' password='' url='jdbc:hsqldb:mem:test_db_reconnect' properties='ifexists=true'/>");
        writer.appendLine("<query sql='select * from test_table' pattern='This is column 1 {column1} this is column 2 {column2}' database='db1' initialDelay='1 second' repeatDelay='1 second' />");
        writer.appendLine("</sqlExtract>");
        writer.appendLine("</container>");

        writer.close();

        LogEventBucket bucket = new LogEventBucket();

        ConfigurableServiceDiscovery discovery = new ConfigurableServiceDiscovery();
        discovery.bind(Destination.class, LogEvent.class, bucket);

        LoggingContainer container = LoggingContainer.fromFile(file, discovery);

        Properties properties = new Properties();

        properties.setProperty("user", "sa");
        properties.setProperty("password", "");
        properties.setProperty("ifexists", "false");
        properties.setProperty("shutdown", "true");

        Connection connection = DriverManager.getConnection("jdbc:hsqldb:mem:test_db_reconnect", properties);

        Statement statement = connection.createStatement();

        statement.execute("CREATE TABLE test_table (column1 int, column2 varchar(255));");
        statement.execute("INSERT INTO test_table (column1, column2) values (1, 'test1')");

        container.start();

        bucket.waitForMessages(1);

        assertThat(bucket.get(0).getMessage(), is("This is column 1 1 this is column 2 test1"));

        // Shutdown the db
        statement.execute("SHUTDOWN");
        statement.close();

        // Wait a while, the extractor should fail
        Thread.sleep(2000);

        // Restart the db - the loop is necessary as the db creation will fail if the other thread
        // is trying to create a connection at the same time. Only this thread can actually create
        // the db, so it normally works after a few retries.
        int repeat = 0;
        int max = 10;
        connection = null;
        while (connection == null && repeat < max) {
            try {
                connection = DriverManager.getConnection("jdbc:hsqldb:mem:test_db_reconnect", properties);
                statement = connection.createStatement();
            }
            catch (SQLException e) {
                repeat++;
            }
        }

        statement.execute("CREATE TABLE test_table (column1 int, column2 varchar(255));");
        statement.execute("INSERT INTO test_table (column1, column2) values (1, 'test1')");
        statement.execute("INSERT INTO test_table (column1, column2) values (2, 'test2')");
        statement.execute("INSERT INTO test_table (column1, column2) values (3, 'test3')");

        bucket.waitForMessages(3);

        assertThat(bucket.get(0).getMessage(), is("This is column 1 1 this is column 2 test1"));
        assertThat(bucket.get(1).getMessage(), is("This is column 1 1 this is column 2 test1"));
        assertThat(bucket.get(2).getMessage(), is("This is column 1 2 this is column 2 test2"));
        assertThat(bucket.get(3).getMessage(), is("This is column 1 3 this is column 2 test3"));

        container.stop();

        statement.execute("SHUTDOWN");
    }

    
    
}
