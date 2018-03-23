package com.woo.jvmtools.javaagent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Logger
{
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static final String LINE_SEP = System.getProperty("line.separator");

    private static Map<File, Logger> loggers = new HashMap();
    private BufferedWriter bw;

    public static Logger getLogger(File logFile, String logHeader)
    {
        logFile = logFile == null ? new File("jvm.inspect") : logFile;
        synchronized (loggers) {
            Logger logger = (Logger)loggers.get(logFile);
            if (logger == null) {
                logger = new Logger(logFile, logHeader);
                loggers.put(logFile, logger);
            }
            return logger;
        }
    }

    public static Logger getLogger(File logFile, Class clazz)
    {
        logFile = logFile == null ? new File("jvm.inspect") : logFile;
        return getLogger(logFile, clazz.getName());
    }

    public static Logger getLogger(String logFile)
    {
        logFile = logFile == null ? "jvm.inspect" : logFile;
        return getLogger(new File(logFile), "");
    }

    private Logger(File logFile, String logHeader)
    {
        try
        {
            this.bw = new BufferedWriter(new FileWriter(logFile));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void info(String msg)
    {
        log(msg, " [info] ");
    }

    public void debug(String msg)
    {
        log(msg, " [debug] ");
    }

    public void newLine()
    {
        try
        {
            this.bw.write(LINE_SEP);
            this.bw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg, String level)
    {
        try
        {
            this.bw.write(DF.format(new Date()) + level + msg + LINE_SEP);
            this.bw.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy()
    {
        System.out.println("[Logger.destroy] called.");
        try {
            this.bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void finalize()
            throws Throwable
    {
        System.out.println("[Logger.finalize] called.");
        super.finalize();
        destroy();
    }
}