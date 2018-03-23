package com.woo.jvmtools.javaagent;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassLoadingInspector
        implements ClassFileTransformer
{
    private Logger logger;
    private long flushInterval = 180000L;
    private int classCount = 0;
    private Object lock = new Object();
    private Flusher flusher = new HtmlFlusher();

    private Map<ClassLoader, List<ClassLoadingInfo>> classLoaders = new ConcurrentHashMap();
    private Map<URL, List<ClassLoadingInfo>> locations = new ConcurrentHashMap();
    private List<ClassLoadingInfo> allClasses = new ArrayList(10240);
    private Class[] preLoadClasses;

    public ClassLoadingInspector(Logger logger, Class[] loadedClasses)
    {
        this.logger = logger;
        this.preLoadClasses = loadedClasses;
        startFlushThread();
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException
    {
        String classNameDot = className.replaceAll("/", ".");
        ClassLoadingInfo info = new ClassLoadingInfo(classNameDot, loader, protectionDomain, this.classCount);

        synchronized (this.lock) {
            this.allClasses.add(info);
        }

        List loaderView = (List)this.classLoaders.get(loader);
        if (loaderView == null) {
            loaderView = new ArrayList();
            this.classLoaders.put(loader, loaderView);
        }
        synchronized (this.lock) {
            loaderView.add(info);
        }

        List locationView = (List)this.locations.get(info.getLocation());
        if (locationView == null) {
            locationView = new ArrayList();
            this.locations.put(info.getLocation(), locationView);
        }
        synchronized (this.lock) {
            locationView.add(info);
        }

        this.classCount += 1;

        this.logger.info(
                loader.getClass().getName() + "@" + Integer.toHexString(loader.hashCode()) + " loaded class: " + classNameDot + " from: " +
                        info.getLocation());

        return null;
    }

    private void startFlushThread()
    {
        new Thread(new Runnable()
        {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(ClassLoadingInspector.this.flushInterval);
                    }catch (InterruptedException ite){
                    }
                    try {
                        synchronized (ClassLoadingInspector.this.lock) {
                            System.out.println("[ClassLoadingInspector.flushInfo] called. total classloaders:" +
                                    ClassLoadingInspector.this.classLoaders.size() + ", total locations:" + ClassLoadingInspector.this.locations.size() + ", total class:" +
                                    ClassLoadingInspector.this.classCount);

                            ClassLoadingInspector.this.flusher.flush("classloaders.html", ClassLoadingInspector.this.classLoaders, ClassLoadingInspector.this.locations, ClassLoadingInspector.this.allClasses, ClassLoadingInspector.this.preLoadClasses);
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
                , "ClassLoadingInspector-FlushThread").start();
    }

    public void setFlushInterval(long flushInterval)
    {
        this.flushInterval = flushInterval;
    }
}