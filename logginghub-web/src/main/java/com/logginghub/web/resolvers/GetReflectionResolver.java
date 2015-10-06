package com.logginghub.web.resolvers;

import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.Param;
import com.logginghub.web.Resolution;
import com.logginghub.web.Resolver;
import com.logginghub.web.ResponseHints;
import com.logginghub.web.ResultHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

public class GetReflectionResolver implements Resolver {

    private static final Logger logger = Logger.getLoggerFor(GetReflectionResolver.class);
    private Class<? extends Object> c;
    private Object instance;

    public GetReflectionResolver(Class<? extends Object> c, Object instance) {
        this.c = c;
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

            String[] split = path.split("/");

            String methodName = split[0];

            Object result = null;

            int restfulParams = split.length - 1;

            Method method = null;

            if (restfulParams > 0) {
                Class[] params = new Class[restfulParams];
                for (int i = 0; i < restfulParams; i++) {
                    params[i] = String.class;
                }
                method = c.getMethod(methodName, params);

                Object[] arguments = new Object[restfulParams];
                for (int i = 0; i < restfulParams; i++) {
                    arguments[i] = split[i + 1];
                }
                result = method.invoke(instance, arguments);
            }
            else {

                // int contentLength = request.getContentLength();
                // String contentType = request.getContentType();
                // ServletInputStream inputStream = request.getInputStream();
                // String content = FileUtils.read(inputStream);

                Map parameterMap = request.getParameterMap();
                Method[] methods = c.getMethods();
                for (Method classMethod : methods) {

                    if (classMethod.getName().equals(methodName)) {
                        int params = classMethod.getParameterTypes().length;
                        if (params == parameterMap.size()) {
                            method = classMethod;
                            Object[] args = processParameters(method, request);
                            result = method.invoke(instance, args);
                            break;
                        }
                        else if (params == 0) {
                            // Will be using RequestContext to get access to the params
                            method = classMethod;
                            result = method.invoke(instance, (Object[]) null);
                            break;
                        }
                    }
                }
            }

            if (method != null) {
                ResponseHints annotation = method.getAnnotation(ResponseHints.class);
                if (annotation != null) {
                    if (annotation.cache()) {
                        response.setHeader("Expires", "Thu, 15 Apr 2015 20:00:00 GMT");
                    }
                }
            }

            if (method != null) {
                ResultHelper.handleResult(result, response);
                resolution = Resolution.success;
            }
        }
        catch (NoSuchMethodException nsme) {
            resolution = Resolution.failedToResolve;
        }
        catch (Exception e) {
            ResultHelper.handleResult(e, response);
            logger.warning(e, "Failed to resolve '{}'", url);
        }

        return resolution;
    }

    private Object[] processParameters(Method method, HttpServletRequest request) {

        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameterValues = new Object[parameterTypes.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int index = 0;

        for (Annotation[] annotations : parameterAnnotations) {

            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    Param param = (Param) annotation;
                    String name = param.name();
                    String parameter = request.getParameter(name);

                    if (parameterTypes[index] == Integer.TYPE) {
                        parameterValues[index] = Integer.valueOf(parameter);
                    }else if (parameterTypes[index] == Boolean.TYPE) {
                        parameterValues[index] = Boolean.valueOf(parameter);
                    }
                    else {
                        parameterValues[index] = parameter;
                    }
                    
                    index++;
                    break;
                }
            }
        }

        return parameterValues;

    }

}
