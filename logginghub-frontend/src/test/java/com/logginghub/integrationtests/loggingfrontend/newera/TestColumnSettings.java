package com.logginghub.integrationtests.loggingfrontend.newera;

import com.logginghub.integrationtests.loggingfrontend.helpers.BaseSwing;
import com.logginghub.integrationtests.loggingfrontend.helpers.SwingFrontEndDSL;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.LogEventBuilder;
import com.logginghub.logging.frontend.configuration.LoggingFrontendConfiguration;
import org.junit.After;
import org.junit.Test;

import java.util.logging.Level;

import static com.logginghub.logging.frontend.configuration.LoggingFrontendConfigurationBuilder.*;

public class TestColumnSettings {

    private SwingFrontEndDSL dsl;

    @After
    public void cleanup() {
        if (dsl != null) {
            dsl.getFrameFixture().cleanUp();
        }
    }


    @Test
    public void test_default_columns() throws InterruptedException {

        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()).toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertColumnIndex("Time", 0);
        dsl.assertColumnIndex("Source", 1);
        dsl.assertColumnIndex("Host", 2);
        dsl.assertColumnIndex("Level", 3);
        dsl.assertColumnIndex("Thread", 4);
        dsl.assertColumnIndex("Method", 5);
        dsl.assertColumnIndex("Message", 6);
        dsl.assertColumnIndex("DC", 7);
        dsl.assertColumnIndex("Locked", 8);
        dsl.assertColumnIndex("PID", 9);
        dsl.assertColumnIndex("Channel", 10);

    }

    @Test
    public void test_hide_all_columns() throws InterruptedException {

        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()
                                                                                                             .columnSettings("Time", 0, 0)
                                                                                                             .columnSettings("Source", 0, 0)
                                                                                                             .columnSettings("Host", 0, 0)
                                                                                                             .columnSettings("Level", 0, 0)
                                                                                                             .columnSettings("Thread", 0, 0)
                                                                                                             .columnSettings("Method", 0, 0)
                                                                                                             .columnSettings("Message", 0, 0)
                                                                                                             .columnSettings("DC", 0, 0)
                                                                                                             .columnSettings("Locked", 0, 0)
                                                                                                             .columnSettings("PID", 0, 0)
                                                                                                             .columnSettings("Channel", 0, 0))
                                                                       .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertColumnHidden("Time");
        dsl.assertColumnHidden("Source");
        dsl.assertColumnHidden("Host");
        dsl.assertColumnHidden("Level");
        dsl.assertColumnHidden("Thread");
        dsl.assertColumnHidden("Method");
        dsl.assertColumnHidden("Message");
        dsl.assertColumnHidden("DC");
        dsl.assertColumnHidden("Locked");
        dsl.assertColumnHidden("PID");
        dsl.assertColumnHidden("Channel");

    }

    @Test
    public void test_hide_first_column() throws InterruptedException {

        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()
                                                                                                             .columnSettings("Time", 0, 0))
                                                                       .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertColumnHidden("Time");

        dsl.assertColumnVisible("Source");
        dsl.assertColumnVisible("Host");
        dsl.assertColumnVisible("Level");
        dsl.assertColumnVisible("Thread");
        dsl.assertColumnVisible("Method");
        dsl.assertColumnVisible("Message");
        dsl.assertColumnVisible("DC");
        dsl.assertColumnVisible("Locked");
        dsl.assertColumnVisible("PID");
        dsl.assertColumnVisible("Channel");

        dsl.assertColumnIndex("Source", 0);
        dsl.assertColumnIndex("Host", 1);
        dsl.assertColumnIndex("Level", 2);
        dsl.assertColumnIndex("Thread", 3);
        dsl.assertColumnIndex("Method", 4);
        dsl.assertColumnIndex("Message", 5);
        dsl.assertColumnIndex("DC", 6);
        dsl.assertColumnIndex("Locked", 7);
        dsl.assertColumnIndex("PID", 8);
        dsl.assertColumnIndex("Channel", 9);

    }

    @Test
    public void test_message_and_metadata_hide_all_columns() throws InterruptedException {

        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()
                                                                                                             .columnSettings("Time", 0, 0)
                                                                                                             .columnSettings("Source", 0, 0)
                                                                                                             .columnSettings("Host", 0, 0)
                                                                                                             .columnSettings("Level", 0, 0)
                                                                                                             .columnSettings("Thread", 0, 0)
                                                                                                             .columnSettings("Method", 0, 0)
                                                                                                             .columnSettings("DC", 0, 0)
                                                                                                             .columnSettings("Locked", 0, 0)
                                                                                                             .columnSettings("PID", 0, 0)
                                                                                                             .columnSettings("Channel", 0, 0)
                                                                                                             .columnSettings("Message", 0, 100)
                                                                                                             .columnSettings("Metadata",
                                                                                                                             1,
                                                                                                                             100,
                                                                                                                             "metadata"))
                                                                       .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertColumnHidden("Time");
        dsl.assertColumnHidden("Source");
        dsl.assertColumnHidden("Host");
        dsl.assertColumnHidden("Level");
        dsl.assertColumnHidden("Thread");
        dsl.assertColumnHidden("Method");
        dsl.assertColumnHidden("DC");
        dsl.assertColumnHidden("Locked");
        dsl.assertColumnHidden("PID");
        dsl.assertColumnHidden("Channel");

        dsl.assertColumnVisible("Message");
        dsl.assertColumnVisible("Metadata");

        dsl.assertColumnIndex("Message", 0);
        dsl.assertColumnIndex("Metadata", 1);

    }

    @Test
    public void test_metadata_extraction_and_column_display() {
        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()
                                                                                                             .columnSettings("Time", 0, 0)
                                                                                                             .columnSettings("Source", 0, 0)
                                                                                                             .columnSettings("Host", 0, 0)
                                                                                                             .columnSettings("Level", 0, 0)
                                                                                                             .columnSettings("Thread", 0, 0)
                                                                                                             .columnSettings("Method", 0, 0)
                                                                                                             .columnSettings("DC", 0, 0)
                                                                                                             .columnSettings("Locked", 0, 0)
                                                                                                             .columnSettings("PID", 0, 0)
                                                                                                             .columnSettings("Channel", 0, 0)
                                                                                                             .columnSettings("Message", 0, 500)
                                                                                                             .columnSettings("Metadata",
                                                                                                                             1,
                                                                                                                             500,
                                                                                                                             "key"))
                                                                       .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        LogEvent event1 = LogEventBuilder.start()
                                         .setLevel(Level.INFO.intValue())
                                         .setMessage("Message 1 : Info")
                                         .metadata("key", "event1")
                                         .toLogEvent();

        LogEvent event2 = LogEventBuilder.start()
                                         .setLevel(Level.WARNING.intValue())
                                         .setMessage("Message 2 : Warning")
                                         .metadata("key", "event2")
                                         .toLogEvent();

        LogEvent event3 = LogEventBuilder.start()
                                         .setLevel(Level.INFO.intValue())
                                         .setMessage("Message 3 : Info")
                                         .metadata("key", "event3")
                                         .toLogEvent();

        dsl.publishEvent(event1);
        dsl.publishEvent(event2);
        dsl.publishEvent(event3);

        dsl.waitForBatch();

        dsl.assertLogEventTableSize(3);

        dsl.assertColumnHidden("Time");
        dsl.assertColumnHidden("Source");
        dsl.assertColumnHidden("Host");
        dsl.assertColumnHidden("Level");
        dsl.assertColumnHidden("Thread");
        dsl.assertColumnHidden("Method");
        dsl.assertColumnHidden("DC");
        dsl.assertColumnHidden("Locked");
        dsl.assertColumnHidden("PID");
        dsl.assertColumnHidden("Channel");

        dsl.assertColumnVisible("Message");
        dsl.assertColumnVisible("Metadata");

        dsl.assertColumnIndex("Message", 0);
        dsl.assertColumnIndex("Metadata", 1);


        dsl.assertTableCell(0, 0, "Message 1 : Info");
        dsl.assertTableCell(0, 1, "event1");

        dsl.assertTableCell(1, 0, "Message 2 : Warning");
        dsl.assertTableCell(1, 1, "event2");

        dsl.assertTableCell(2, 0, "Message 3 : Info");
        dsl.assertTableCell(2, 1, "event3");

    }


    @Test
    public void test_custom_configuration_1() {

        //    <columnSetting name="Result" width="110" order="6" metadata="Result"/>
        //    <columnSetting name="Currency" width="150" order="7" metadata="Currency"/>
        //    <columnSetting name="Instrument" width="150" order="3" metadata="Instrument"/>
        //    <columnSetting name="Direction" width="50" order="2" metadata="Direction"/>
        //    <columnSetting name="Order" width="50" order="9" metadata="Order"/>
        //    <columnSetting name="TimeStamp" width="150" order="8" metadata="Time"/>
        //    <columnSetting name="Market" width="100" order="0" metadata="Market"/>
        //    <columnSetting name="Quantity" width="100" order="4" metadata="Quantity" alignment="Right"/>
        //    <columnSetting name="Product" width="150" order="4" metadata="Product"/>
        //    <columnSetting name="Customer" width="280" order="5" metadata="Customer"/>
        //
        //    <columnSetting name="Method" width="0"/>
        //    <columnSetting name="Product" width="0"/>
        //    <columnSetting name="DC" width="0"/>
        //    <columnSetting name="PID" width="0"/>
        //    <columnSetting name="Locked" width="0"/>
        //    <columnSetting name="Channel" width="0"/>
        //    <columnSetting name="Message" width="0"/>


        LoggingFrontendConfiguration configuration = newConfiguration().environment(newEnvironment("default").disableColumnsFile()
                                                                                                             .columnSettings("Market", 0, 100, "Market")
                                                                                                             .columnSettings("Quantity", 1, 100, "Quantity")
                                                                                                             .columnSettings("Direction", 2, 50, "Direction")
                                                                                                             .columnSettings("Instrument", 3, 150, "Instrument")
                                                                                                             .columnSettings("Product", 4, 150, "Product")
                                                                                                             .columnSettings("Customer", 5, 280, "Customer")
                                                                                                             .columnSettings("Result", 6, 110, "Result")
                                                                                                             .columnSettings("Currency", 7, 150, "Currency")
                                                                                                             .columnSettings("TimeStamp", 8, 150, "Time")
                                                                                                             .columnSettings("Order", 9, 50, "Order")

                                                                                                             .columnSettings("Method", 0, 0)
                                                                                                             .columnSettings("DC", 0, 0)
                                                                                                             .columnSettings("PID", 0, 0)
                                                                                                             .columnSettings("Locked", 0, 0)
                                                                                                             .columnSettings("Channel", 0, 0)
                                                                                                             .columnSettings("Message", 0, 0)
                                                                                                             .columnSettings("Time", 0, 0)
                                                                                                             .columnSettings("Source", 0, 0)
                                                                                                             .columnSettings("Host", 0, 0)
                                                                                                             .columnSettings("Level", 0, 0)
                                                                                                             .columnSettings("Thread", 0, 0)
        )

                                                                       .toConfiguration();

        dsl = BaseSwing.createDSL(configuration);

        dsl.assertColumnIndex("Market", 0);
        dsl.assertColumnIndex("Quantity", 1);
        dsl.assertColumnIndex("Direction", 2);
        dsl.assertColumnIndex("Instrument", 3);
        dsl.assertColumnIndex("Product", 4);
        dsl.assertColumnIndex("Customer", 5);
        dsl.assertColumnIndex("Result", 6);
        dsl.assertColumnIndex("Currency", 7);
        dsl.assertColumnIndex("TimeStamp", 8);
        dsl.assertColumnIndex("Order", 9);

    }



}
