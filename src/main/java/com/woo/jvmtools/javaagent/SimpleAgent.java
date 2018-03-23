package com.woo.jvmtools.javaagent;

import java.lang.instrument.Instrumentation;

public class SimpleAgent
{
    private static String logFile;
    private static long flushIntervalSeconds;

    public static void premain(String agentArgs, Instrumentation inst)
    {
        System.out.println("[SimpleAgent.premain] agentArgs:" + agentArgs);
        System.out.println("[SimpleAgent.premain] Instrumentation:" + inst);
        System.out.println("[SimpleAgent.premain] isRedefineClassesSupported:" + inst.isRedefineClassesSupported());

        parseOptions(agentArgs);
        Logger logger = Logger.getLogger(logFile);
        ClassLoadingInspector inspector = new ClassLoadingInspector(logger, inst.getAllLoadedClasses());
        inspector.setFlushInterval(flushIntervalSeconds * 1000L);

        Class[] bootstrapClasses = inst.getInitiatedClasses(null);
        logger.info("[SimpleAgent.premain] total loaded classes: " + inst.getAllLoadedClasses().length);
        logger.info("[SimpleAgent.premain] total bootstrap loaded class: " + bootstrapClasses.length);
        for (Class c : inst.getAllLoadedClasses())
        {
            logger.info("[SimpleAgent.premain] " + c + " loaded by [" + c.getClassLoader() + "]");
        }

        inst.addTransformer(inspector);
    }

    private static void parseOptions(String agentArgs)
    {
        if ((agentArgs == null) && (agentArgs.length() == 0))
        {
            return;
        }
        String[] options = agentArgs.split(",");
        for (String option : options)
        {
            if (option.startsWith("outputfile="))
            {
                logFile = option.substring("outputfile=".length());
            } else {
                if (!option.startsWith("flushIntervalSecond="))
                    continue;
                flushIntervalSeconds = Long.parseLong(option.substring("flushIntervalSecond=".length()));
            }
        }
    }

    public static void main(String[] args)
    {
        System.out.println("=== main ===");
    }
}