package com.woo.jvmtools.sample;

import java.net.URL;
import java.net.URLClassLoader;

public class ChildFirstClassLoader extends URLClassLoader
{
    public ChildFirstClassLoader(URL[] urls)
    {
        super(urls);
    }

    public ChildFirstClassLoader(URL[] urls, ClassLoader parent)
    {
        super(urls, parent);
    }

    public void addURL(URL url)
    {
        super.addURL(url);
    }

    public Class<?> loadClass(String name)
            throws ClassNotFoundException
    {
        return loadClass(name, false);
    }

    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        Class c = findLoadedClass(name);

        if (c == null) {
            try {
                c = findClass(name);
            }
            catch (ClassNotFoundException localClassNotFoundException)
            {
            }

        }

        if (c == null) {
            if (getParent() != null) {
                c = getParent().loadClass(name);
            }
            else {
                c = getSystemClassLoader().loadClass(name);
            }
        }

        if (resolve) {
            resolveClass(c);
        }

        return c;
    }
}