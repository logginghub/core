package com.logginghub.web.resolvers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.Resolution;
import com.logginghub.web.Resolver;
import com.logginghub.web.ResultHelper;

public class PostReflectionResolver implements Resolver {

    private Class<? extends Object> c;
    private Object instance;

    private static final Logger logger = Logger.getLoggerFor(PostReflectionResolver.class);

    public PostReflectionResolver(Class<? extends Object> c, Object instance) {
        this.c = c;
        this.instance = instance;
    }

    public Resolution resolve(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //
        // Enumeration attributeNames = request.getAttributeNames();
        // while (attributeNames.hasMoreElements()) {
        // String name = attributeNames.nextElement().toString();
        // Object attribute = request.getAttribute(name);
        // Out.out("name {} value {}", name, attribute);
        // }

        Resolution resolution = Resolution.failedToResolve;
        if (request.getMethod().equalsIgnoreCase("post")) {

            String after = StringUtils.after(url, "/");
            Class[] params = new Class[] { Map.class };

            Method method = null;

            try {
                String methodName = after + "_post";
                method = c.getMethod(methodName, params);
            }
            catch (NoSuchMethodException nsme) {
                // Ignore this one
                String methodName = after;
                try {
                    method = c.getMethod(methodName, params);
                }
                catch (NoSuchMethodException e) {
                    logger.info("Couldn't find post method for target '{}'", after);
                    resolution = Resolution.failedToResolve;
                }
            }

            if (method != null) {
                try {
                    Object result = null;
                    if (method.getParameterTypes().length > 0) {
                        @SuppressWarnings("unchecked") Map<String, String[]> parameters = request.getParameterMap();
                        result = method.invoke(instance, new Object[] { parameters });
                    }
                    else {
                        // Must be using the RequestContext to get the params
                        result = method.invoke(instance, (Object[])null);
                    }

                    if (method.getReturnType() == Void.class) {
                        // Void result means the method must have handled everything itself
                    }
                    else {
                        ResultHelper.handleResult(result, response);
                    }
                    resolution = Resolution.success;

                }
                catch (Exception e) {
                    logger.warning(e);
                    resolution = Resolution.failedToResolve;
                }
            }
        }

        return resolution;
    }
}
