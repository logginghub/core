package com.logginghub.logging.frontend.analysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class StreamHelper
{
    public static InputStream openStream(String source) throws IOException
    {
        InputStream is;
        if (source.startsWith("http"))
        {
            is = new URL(source).openStream();
        }
        else
        {
            is = new FileInputStream(source);
        }

        return is;
    }
}
