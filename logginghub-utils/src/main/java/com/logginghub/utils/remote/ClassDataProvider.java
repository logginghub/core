package com.logginghub.utils.remote;

import java.util.Collection;

public interface ClassDataProvider
{
    byte[] getClassBytes(String classname);
    byte[] getResourceBytes(String resourcePath);
    Collection<String> enumerateClasses();
}
