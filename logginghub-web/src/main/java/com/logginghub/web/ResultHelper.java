package com.logginghub.web;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class ResultHelper {

    public static void handleResult(Object result, HttpServletResponse response) throws IOException {

        if (result == null) {
            // Write an empty response
            Helper.ok(response, "");
            response.flushBuffer();
        }
        else {

            if (result instanceof byte[]) {
                byte[] bs = (byte[]) result;
                // Stopwatch start = Stopwatch.start("Writing byte array");
                response.getOutputStream().write(bs);
                response.flushBuffer();
                // start.stopAndDump();
            }
            else if (result instanceof String) {
                if (response.getContentType() == null) {
                    response.setContentType("text/html");
                }
                response.getOutputStream().write(((String) result).getBytes());
                response.flushBuffer();
            }
            else if (result instanceof Throwable) {
                Throwable throwable = (Throwable) result;
                if (response.getContentType() == null) {
                    response.setContentType("text/html");
                }
                // TODO : security issues.
                response.setStatus(500);

                String message;
                if (throwable.getCause() != null) {
                    message = "An exception occured : " + throwable.getCause().getClass().getName() + " : " + throwable.getCause().getMessage();
                }
                else {
                    message = "An exception occured : " + throwable.getClass().getName() + " : " + throwable.getMessage();
                }
                response.getOutputStream().write(message.getBytes());
                response.flushBuffer();
            }
            else {
                ObjectMapper mapper = new ObjectMapper();
                SerializationConfig serializationConfig = mapper.getSerializationConfig();
                mapper.setSerializationConfig(serializationConfig.without(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS));

                String jacksonString = mapper.writeValueAsString(result);
                response.setContentType("application/json");
                // StringUtils.out("Returning json string {}",
                // jacksonString);

                response.getOutputStream().write(jacksonString.getBytes());
                response.flushBuffer();
            }
        }

    }

}
