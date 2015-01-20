package com.logginghub.web;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import com.logginghub.utils.FormattedRuntimeException;
import com.logginghub.utils.logging.Logger;

public class JettyLauncher {

    private static final Logger logger = Logger.getLoggerFor(JettyLauncher.class);
    private Handler handler;
    private int httpPort = 8080;
    private Server server;

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void stop() {
        try {
            server.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() throws Exception {
        start();
        join();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void start() throws Exception {
        server = new Server();

        SelectChannelConnector httpConnector = new SelectChannelConnector();
        httpConnector.setPort(httpPort);
        httpConnector.setMaxIdleTime(30000);
        httpConnector.setRequestHeaderSize(8192);

        File keyStore = new File("mySrvKeystore");
        if (keyStore.exists()) {
            SslContextFactory sslContextFactory = new SslContextFactory("mySrvKeystore");
            sslContextFactory.setKeyStorePassword("password");
            SslSelectChannelConnector httpsConnector = new SslSelectChannelConnector(sslContextFactory);
            httpsConnector.setPort(httpPort + 1);

            server.setConnectors(new Connector[] { httpConnector, httpsConnector });
        }
        else {
            server.setConnectors(new Connector[] { httpConnector });
        }

        final WebSocketHelper helper = new WebSocketHelper();
        
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            @Override public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {                
                logger.info("Web sockets connect {} {}", request, protocol);                
                return new WebSocketWrapper(helper);
            }
        };
        
        if(handler instanceof WebSocketSupport) {
            WebSocketSupport webSocketSupport = (WebSocketSupport) handler;
            webSocketSupport.setWebSocketHelper(helper);
        }
        
        // Chain the handlers together
        webSocketHandler.setHandler(handler);
        server.setHandler(webSocketHandler);

        server.start();
        logger.info("Successfully bound to {}", httpPort);
    }

    public static JettyLauncher launchBlocking(Object controller, int httpPort) throws Exception {
        JettyLauncher launcher = new JettyLauncher();
        launcher.setHttpPort(httpPort);
        launcher.setHandler(new ReflectionHandler(controller));
        launcher.run();
        return launcher;
    }

    public static JettyLauncher launchNonBlocking(Object controller, int httpPort) throws Exception {
        JettyLauncher launcher = new JettyLauncher();
        launcher.setHttpPort(httpPort);
        launcher.setHandler(new ReflectionHandler(controller));
        launcher.start();
        return launcher;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void close() {
        try {
            server.stop();
        }
        catch (Exception e) {
            throw new FormattedRuntimeException(e, "Failed to stop jetty server");
        }
    }
}
