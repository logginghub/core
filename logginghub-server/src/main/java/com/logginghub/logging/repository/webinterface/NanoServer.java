package com.logginghub.logging.repository.webinterface;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.logginghub.utils.httpd.NanoHTTPD;


public class NanoServer extends NanoHTTPD {

    public NanoServer(int port, File wwwroot) throws IOException {
        super(port, wwwroot);
    }

    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        Response response = new Response("Status", "mime type", "hello world");
        return response;
    };
    
}
