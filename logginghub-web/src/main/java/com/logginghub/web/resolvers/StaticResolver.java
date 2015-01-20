package com.logginghub.web.resolvers;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logginghub.utils.FileUtils;
import com.logginghub.utils.ResourceUtils;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.logging.Logger;
import com.logginghub.web.Helper;
import com.logginghub.web.Resolution;
import com.logginghub.web.Resolver;

public class StaticResolver implements Resolver {

    private static final Logger logger = Logger.getLoggerFor(StaticResolver.class);
    private String staticFilePath;

    public StaticResolver(String staticFilePath) {
        this.staticFilePath = staticFilePath;
        if (!this.staticFilePath.endsWith("/")) {
            this.staticFilePath += "/";
        }
    }

    public Resolution resolve(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {

        Resolution resolution = new Resolution();

        String path;
        if (url.startsWith("/")) {
            path = staticFilePath + url.substring(1);
        }
        else {
            path = staticFilePath + url;
        }

        logger.fine("Searching for static file with path '{}'", path);

        String since = request.getHeader("If-Modified-Since");
        try {
            InputStream openStream = ResourceUtils.openStream(path);

            if (since != null) {
                response.setHeader("Cache-Control", "max-age=3600");
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                response.getOutputStream().print("");
                response.flushBuffer();
            }
            else {
                byte[] bytes = FileUtils.readFully(openStream);

                String extension = StringUtils.afterLast(url, ".");
                String type = Helper.getMimeType(extension);
                response.setContentType(type);
                response.setHeader("Cache-Control", "max-age=3600");
                response.setStatus(HttpServletResponse.SC_OK);

                // System.out.println("Sending " + bytes.length + " bytes");
                response.getOutputStream().write(bytes);
                response.flushBuffer();
            }

            resolution = Resolution.success;
        }
        catch (RuntimeException e) {
            resolution = Resolution.failedToResolve;
        }

        return resolution;
    }

}
