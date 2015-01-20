package com.logginghub.web.resolvers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.Resolution;
import com.logginghub.web.Resolver;
import com.logginghub.web.ResultHelper;

public class DynamicHandlerResolver implements Resolver {

    private static final Logger logger = Logger.getLoggerFor(DynamicHandlerResolver.class);
    private Object instance;

    public DynamicHandlerResolver(Object instance) {
        this.instance = instance;
    }

    public Resolution resolve(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {

        Resolution resolution = Resolution.failedToResolve;
        try {
            String path;

            if (url.startsWith("/")) {
                path = StringUtils.after(url, "/");
            }
            else {
                path = url;
            }

            Object result = null;

            result = ReflectionUtils.invoke(instance, "handle", path);

            ResultHelper.handleResult(result, response);
            resolution = Resolution.success;
        }
        catch (Exception e) {
            ResultHelper.handleResult(e, response);
            logger.fine(e, "Failed to resolve '{}'", url);
        }

        return resolution;
    }


}
