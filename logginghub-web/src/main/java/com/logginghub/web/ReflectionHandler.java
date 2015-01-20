package com.logginghub.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.resolvers.DynamicHandlerResolver;
import com.logginghub.web.resolvers.EndPointAnnotationResolver;
import com.logginghub.web.resolvers.GetReflectionResolver;
import com.logginghub.web.resolvers.PostReflectionResolver;
import com.logginghub.web.resolvers.StaticResolver;

public class ReflectionHandler extends AbstractHandler implements WebSocketSupport {

    private String magicWebSocketsString = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private Object target;
    private String staticFilePath;
    private Class<? extends Object> c;
    private String defaultUrl;

    private static final Logger logger = Logger.getLoggerFor(ReflectionHandler.class);

    public ReflectionHandler(Object target) {
        this.target = target;

        c = target.getClass();
        if (c.isAnnotationPresent(WebController.class)) {
            WebController annotation = c.getAnnotation(WebController.class);
            String staticFiles = annotation.staticFiles();
            if (staticFiles != null) {
                this.staticFilePath = staticFiles;
                if (staticFilePath.endsWith("/")) {
                    staticFilePath = StringUtils.trimFromEnd(staticFilePath, 1);
                }
            }

            defaultUrl = annotation.defaultUrl();
        }
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, final HttpServletResponse response) throws IOException,
                    ServletException {

        Stopwatch stopwatch = Stopwatch.start(target);
        logger.debug("Handling target {} method {}", target, request.getMethod());

        if (target.equals("/")) {
            target = defaultUrl;
        }

        RequestContext requestContext = RequestContext.getRequestContext();
        requestContext.setup(target, request, response);

        List<Resolver> resolvers = new ArrayList<Resolver>();

        resolvers.add(new GetReflectionResolver(c, this.target));
        resolvers.add(new PostReflectionResolver(c, this.target));
        if (staticFilePath != null) {
            resolvers.add(new StaticResolver(staticFilePath));
        }

        // This one stops the favicon request hitting the dynamic resolver
        resolvers.add(new Resolver() {
            public Resolution resolve(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
                if (url.equals("/favicon.ico")) {
                    return Resolution.success;
                }
                return Resolution.failedToResolve;
            }
        });

        resolvers.add(new EndPointAnnotationResolver(this.target));
        
        resolvers.add(new DynamicHandlerResolver(this.target));
        
        

        boolean handled = false;
        for (Resolver resolver : resolvers) {
            Stopwatch sw = Stopwatch.start("Resolver {}", resolver.getClass().getSimpleName());
            Resolution resolution = resolver.resolve(target, request, response);
            logger.fine(sw);
            
            if (resolution == Resolution.success) {
                handled = true;
                break;
            }

        }

        if (!handled) {
            logger.warning("Request for target {} was not handled by any of the resolvers, returning error", target);
        }

        logger.info("Handling target {} method {} complete in {}", target, request.getMethod(), stopwatch.stopAndFormat());

    }

    @Override public void setWebSocketHelper(WebSocketHelper helper) {
        if(target instanceof WebSocketSupport) {
            WebSocketSupport webSocketSupport = (WebSocketSupport) target;
            webSocketSupport.setWebSocketHelper(helper);
        }
    }

}