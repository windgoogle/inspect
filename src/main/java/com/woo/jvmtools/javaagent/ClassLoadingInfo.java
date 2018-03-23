package com.woo.jvmtools.javaagent;

import java.net.URL;
import java.security.ProtectionDomain;

public class ClassLoadingInfo {
    private String name;
    private ClassLoader loader;
    private URL location;
    private int order;

    public ClassLoadingInfo() {
    }

    public ClassLoadingInfo(String name, ClassLoader loader, URL location, int order) {
        this.name = name;
        this.loader = loader;
        this.location = location;
        this.order = order;
    }

    public ClassLoadingInfo(String name, ClassLoader loader, ProtectionDomain protectionDomain, int order) {
        URL classLocation =
                protectionDomain.getCodeSource() == null ? null : protectionDomain.getCodeSource().getLocation();

        this.name = name;
        this.loader = loader;
        this.location = classLocation;
        this.order = order;
    }

    public ClassLoader getLoader() {
        return this.loader;
    }

    public void setLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public URL getLocation() {
        return this.location;
    }

    public void setLocation(URL location) {
        this.location = location;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}