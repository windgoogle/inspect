package com.woo.jvmtools.mxbean;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Hashtable;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class SimpleMxbeanInspector
{
    private static MBeanServerConnection getMBeanServerConnector(String mbeanServerHost, String mbeanServerPort)
            throws IOException
    {
        int port = Integer.parseInt(mbeanServerPort);
        String protocol = "rmi";
        String jndiroot = new String("/jndi/iiop://" + mbeanServerHost + ":" + port +
                "/");
        String mserver = "weblogic/management/mbeanservers/runtime";

        JMXServiceURL serviceURL = new JMXServiceURL(protocol, mbeanServerHost, port,
                jndiroot + mserver);

        Hashtable h = new Hashtable();
        h.put("java.naming.security.principal", "weblogic");
        h.put("java.naming.security.credentials", "weblogic");

        JMXConnector c = JMXConnectorFactory.connect(serviceURL, h);
        return c.getMBeanServerConnection();
    }

    private static void inspect(MBeanServerConnection mbs) throws IOException
    {
        RuntimeMXBean proxyRuntimeMXBean = (RuntimeMXBean)ManagementFactory.newPlatformMXBeanProxy(
                mbs, "java.lang:type=Runtime", RuntimeMXBean.class);

        ClassLoadingMXBean proxyClassLoadingMXBean = (ClassLoadingMXBean)ManagementFactory.newPlatformMXBeanProxy(
                mbs, "java.lang:type=Runtime", ClassLoadingMXBean.class);

        System.out.println("LoadedClassCount=" + proxyClassLoadingMXBean.getLoadedClassCount());
        System.out.println("TotalLoadedClassCount=" + proxyClassLoadingMXBean.getTotalLoadedClassCount());
        System.out.println("UnloadedClassCount=" + proxyClassLoadingMXBean.getUnloadedClassCount());
        System.out.println("ClassLoadingMXBean.isVerbose=" + proxyClassLoadingMXBean.isVerbose());
        proxyClassLoadingMXBean.setVerbose(true);
    }

    public static void main(String[] args) throws IOException {
        String mbeanServerHost = args.length > 0 ? args[0] : "150.236.80.181";
        String mbeanServerPort = args.length > 1 ? args[1] : "9001";
        MBeanServerConnection mbs = getMBeanServerConnector(mbeanServerHost, mbeanServerPort);
        System.out.println(mbs);
    }
}