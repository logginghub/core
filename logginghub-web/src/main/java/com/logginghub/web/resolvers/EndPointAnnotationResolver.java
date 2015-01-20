package com.logginghub.web.resolvers;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logginghub.utils.ReflectionUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.EndPoint;
import com.logginghub.web.Resolution;
import com.logginghub.web.Resolver;
import com.logginghub.web.ResultHelper;

public class EndPointAnnotationResolver implements Resolver {

    private static final Logger logger = Logger.getLoggerFor(EndPointAnnotationResolver.class);
    private Object instance;

    public EndPointAnnotationResolver(Object instance) {
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

            String type = null;
            Object result = null;

            Method[] methods = instance.getClass().getMethods();
            for (Method method : methods) {
                EndPoint annotation = method.getAnnotation(EndPoint.class);
                if (annotation != null) {
                    if (annotation.path().equals(path)) {
                        result = ReflectionUtils.invoke(method, instance);
                        type = annotation.mime();
                        resolution = Resolution.success;
                        break;
                    }
                }
            }

            if (resolution == Resolution.success) {
                if (type != null) {
                    response.setContentType(type);
                }
                
                ResultHelper.handleResult(result, response);
            }
        }
        catch (Exception e) {
            ResultHelper.handleResult(e, response);
            logger.fine(e, "Failed to resolve '{}'", url);
        }

        return resolution;
    }

}
