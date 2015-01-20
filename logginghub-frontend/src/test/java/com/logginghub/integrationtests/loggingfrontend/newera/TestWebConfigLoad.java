package com.logginghub.integrationtests.loggingfrontend.newera;

import java.io.File;
import java.io.IOException;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.junit.Test;

import com.logginghub.logging.frontend.SwingFrontEnd;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.NetUtils;
import com.logginghub.utils.httpd.NanoHTTPD;

public class TestWebConfigLoad {

    @Test public void test_url_config() throws IOException {
        FailOnThreadViolationRepaintManager.install();
        File folder = FileUtils.createRandomFolder("target/test/webconfig");
        
        File configFolder = new File(folder, "config");
        configFolder.mkdirs();
        
        FileUtils.copy(new File("logging.frontend.xml"), configFolder);
        
        final int webPort = NetUtils.findFreePort();
        NanoHTTPD httpd = new NanoHTTPD(webPort, folder);
        
        SwingFrontEnd swingFrontEnd = GuiActionRunner.execute(new GuiQuery<SwingFrontEnd>() {
            @Override protected SwingFrontEnd executeInEDT() throws Throwable {
                return SwingFrontEnd.mainInternal(new String[] { "http://localhost:" + webPort + "/config/logging.frontend.xml"});
            }
        });
                
        httpd.stop();
    }

}
