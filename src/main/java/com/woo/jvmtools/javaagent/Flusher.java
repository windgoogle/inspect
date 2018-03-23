package com.woo.jvmtools.javaagent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract interface Flusher
{
    public abstract void flush(String paramString, Map<ClassLoader, List<ClassLoadingInfo>> paramMap, Map<URL, List<ClassLoadingInfo>> paramMap1, List<ClassLoadingInfo> paramList, Class[] paramArrayOfClass)
            throws IOException;

    public abstract void flush(PrintWriter paramPrintWriter, Map<ClassLoader, List<ClassLoadingInfo>> paramMap, Map<URL, List<ClassLoadingInfo>> paramMap1, List<ClassLoadingInfo> paramList, Class[] paramArrayOfClass)
            throws IOException;
}