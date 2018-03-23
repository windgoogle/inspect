package com.woo.jvmtools.javaagent;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.woo.util.Tree;
import com.woo.util.TreeImpl;

public class HtmlFlusher
        implements Flusher
{
    private static final String SEP = ", ";
    private static final String bracket1 = "[";
    private static final String bracket2 = "]";
    private static final String nullClassloaderName = "BootStrapLoader";
    private static final String nullUrlName = "BootStrapLoaderLocation";
    private static final int MAX_ITEM_COUNT_NOT_HIERARCHICAL = 6;
    private static final MessageFormat packageTemplate = new MessageFormat("{0}, {1}, {2}, {3}");
    private static final MessageFormat locationTemplate = new MessageFormat("{0}, {2}, {3}");
    private static final MessageFormat loaderTemplate = new MessageFormat("{0}, {1}, {3}");

    private Map<String, String> stringAlias = new TreeMap();
    private Map<ClassLoader, String> loaderAlias = new HashMap();
    private Map<URL, String> urlAlias = new HashMap();
    private int libNumber = 0;
    private int loaderNumber = 0;
    private int pkgNumber = 0;

    private Map<URL, List<ClassLoadingInfo>> sortedLocations = new TreeMap(new Comparator()
    {
        public int compare(URL o1, URL o2)
        {
            return ((String)HtmlFlusher.this.urlAlias.get(o1)).compareTo((String)HtmlFlusher.this.urlAlias.get(o2));
        }
    });

    private boolean enableHyperlink = "true".equals(System.getProperty("HtmlFlusher.enableHyperlink"));

    private static final Comparator<Tree<ClassLoadingInfo, String>> comparator = new Comparator() {
        public int compare(Tree<ClassLoadingInfo, String> o1, Tree<ClassLoadingInfo, String> o2) {
            if ((o1 == null) || (o2 == null)) {
                return 0;
            }
            return ((String)o1.getId()).compareTo((String)o2.getId());
        }
    };

    private void fillAlias(Map<ClassLoader, List<ClassLoadingInfo>> classLoaders, Map<URL, List<ClassLoadingInfo>> locations)
    {
        this.libNumber = 0;
        this.loaderNumber = 0;
        this.pkgNumber = 0;
        for (Map.Entry entry : classLoaders.entrySet()) {
            fillAlias((ClassLoader)entry.getKey());
        }
        for (Map.Entry entry : locations.entrySet()) {
            fillAlias((URL)entry.getKey());
            this.sortedLocations.put((URL)entry.getKey(), (List)entry.getValue());
        }
    }

    private void fillAlias(String propPreFix)
    {
        for (int i = 0; ; i++)
        {
            String alias = System.getProperty(propPreFix + i);
            if (alias == null)
                break;
            int index = alias.indexOf("=");
            if (index == -1)
                break;
            this.stringAlias.put(alias.substring(0, index), alias.substring(index + 1));
        }
    }

    private void fillAlias(ClassLoader loader) {
        String shortStr = this.loaderNumber + "-" + loader.getClass().getSimpleName();
        this.loaderAlias.put(loader, "[" + shortStr + "]");
        this.loaderNumber += 1;
    }

    private void fillAlias(URL url)
    {
        if (url == null)
        {
            this.urlAlias.put(url, "[BootStrapLoaderLocation]");
            return;
        }
        String path = url.getPath();
        if (path == null)
        {
            this.urlAlias.put(url, url.toString());
            return;
        }

        int index = path.lastIndexOf('/');
        if (index == -1)
        {
            this.urlAlias.put(url, url.toString());
            return;
        }

        String libPath = path.substring(0, index);
        String libShort = (String)this.stringAlias.get(libPath);
        if (libShort == null)
        {
            libShort = "[" + getLibShort(libPath) + "]";
            this.stringAlias.put(libPath, libShort);
        }

        String urlAliasValue =
                libShort + path.substring(index);
        this.urlAlias.put(url, urlAliasValue);
    }

    private String getLibShort(String libPath)
    {
        String libShort = "lib";
        String[] libs = libPath.split("/");
        int len = libs.length;
        if (len >= 2)
            libShort = libs[(len - 2)] + "/" + libs[(len - 1)];
        else if (len == 1)
            libShort = libs[0];
        int number = this.libNumber++;
        return (number < 10 ? "0" : "") + number + "-" + libShort;
    }

    private void outputAliasTable(PrintWriter pw)
    {
        pw.println("<br><table border=1>");
        pw.println("<tr><td>Alias</td><td>Content</td></tr>");
        for (Map.Entry entry : this.stringAlias.entrySet())
        {
            pw.println(
                    "<tr><td>" + (String)entry.getValue() + "</td><td>" + (String)entry.getKey() + "</td></tr>");
        }
        for (Map.Entry entry : this.loaderAlias.entrySet())
        {
            pw.println(
                    "<tr><td>" + (String)entry.getValue() + "</td><td>" + entry.getKey() + "</td></tr>");
        }

        pw.println("</table>");
    }

    private String toShort(ClassLoader loader)
    {
        if (loader == null)
            return "BootStrapLoader";
        String shortStr = (String)this.loaderAlias.get(loader);
        if (shortStr == null)
            shortStr = loader.toString();
        return this.enableHyperlink ? "<a href=\"#a" + shortStr + "\" title=\"" + loader.getClass().getName() + "\">" + shortStr + "</a>" : shortStr;
    }

    private String toShort(URL url) {
        return (String)this.urlAlias.get(url);
    }

    private String toShort(Object obj)
    {
        if (obj == null) return null;
        String str = obj.toString();
        for (Map.Entry entry : this.stringAlias.entrySet())
        {
            if (str.startsWith((String)entry.getKey()))
                return "[" + (String)entry.getValue() + "]" + str.substring(((String)entry.getKey()).length());
            int index = str.indexOf((String)entry.getKey());
            if (index != -1)
                return str.substring(0, index) + "[" + (String)entry.getValue() + "]" +
                        str.substring(index + ((String)entry.getKey()).length());
        }
        return str;
    }

    public void flush(String fileName, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders, Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class[] preLoadClasses)
            throws IOException
    {
        PrintWriter pw = new PrintWriter(fileName, "utf-8");
        flush(pw, classLoaders, locations, allClasses, preLoadClasses);
    }

    public void flush(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders, Map<URL, List<ClassLoadingInfo>> locations, List<ClassLoadingInfo> allClasses, Class[] preLoadClasses)
            throws IOException
    {
        pw.println("<head><meta http-equiv=\"content-type\" content=\"text/html\"; charset=\"UTF-8\"></head>");
        pw.println("<script>");
        pw.println("function onItemClick(idSuffix)");
        pw.println("{");
        pw.println("   subs = window.eval('ul_'+idSuffix)");
        pw.println("   if(subs.style.display=='none')");
        pw.println("      subs.style.display='block';");
        pw.println("   else");
        pw.println("      subs.style.display='none';");
        pw.println("   event.cancelBubble=true;");
        pw.println("}");
        pw.println("function expendAll(flag)");
        pw.println("{");
        pw.println("   var uls = document.getElementsByTagName('UL')");
        pw.println("   var display = flag==0? 'none' :'block';");
        pw.println("   for(var i=0; i<uls.length; i++)");
        pw.println("      uls[i].style.display = display;");
        pw.println("}");
        pw.println("</script>");
        pw.println("<style type=\"text/css\">");
        pw.println("   li{list-style-type:none; margin-top:0px; margin-bottom:0px; margin-left:5}");
        pw.println("</style>");
        pw.println("<input type=button value=\"全部展开\" onclick=\"expendAll(1)\" />");
        pw.println("<input type=button value=\"全部折叠\" onclick=\"expendAll(0)\" />");

        fillAlias(classLoaders, locations);

        flushLoaderViewHierarchical(pw, classLoaders, preLoadClasses);

        flushJarViewHierarchical(pw, this.sortedLocations);
        flushPackageView(pw, allClasses);

        outputAliasTable(pw);

        pw.flush();
        pw.close();
    }

    private void flushLoaderView(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders, Class[] preLoadClasses) {
        pw.println("<p><li id=li_loaderview onclick=\"onItemClick('loaderview')\" style=\"{list-style-type:circle;}\">ClassLoader View</li>");
        pw.println("<ul id=ul_loaderview>");

        pw.println("<li id=li_preload onclick=\"onItemClick('preload')\">PreLoadedClasses, " +
                preLoadClasses.length + " classes</li>");
        pw.println("<ul id=ul_preload style=\"{display:none}\">");
        for (Class c : preLoadClasses) {
            CodeSource cs = c.getProtectionDomain().getCodeSource();
            pw.println("<li>" + c.getName() + ", " + "Loader:" + toShort(c.getClassLoader()) + ", " +
                    "Location:" + (cs == null ? "null" : cs.getLocation()) + "</li>");
        }
        pw.println("</ul>");

        this.loaderNumber = 0;
        for (Map.Entry entry : classLoaders.entrySet()) {
            ClassLoader loader = (ClassLoader)entry.getKey();

            String idSuffix = "loader" + this.loaderNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\">" + toShort(loader) + ", " + (
                    entry.getValue() == null ? 0 : ((List)entry.getValue()).size()) + " classes</li>");
            if ((entry.getValue() != null) && (!((List)entry.getValue()).isEmpty())) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                for (ClassLoadingInfo info : (List)entry.getValue()) {
                    pw.println("<li>" + info.getName() + ", " + toShort(info.getLocation()) + ", " + info.getOrder() + "</li>");
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }

    private void flushLoaderViewHierarchical(PrintWriter pw, Map<ClassLoader, List<ClassLoadingInfo>> classLoaders, Class[] preLoadClasses) {
        pw.println("<p><li id=li_loaderview onclick=\"onItemClick('loaderview')\" style=\"{list-style-type:circle;}\">ClassLoader View</li>");
        pw.println("<ul id=ul_loaderview>");

        pw.println("<li id=li_preload onclick=\"onItemClick('preload')\">PreLoadedClasses, " +
                preLoadClasses.length + " classes</li>");
        pw.println("<ul id=ul_preload style=\"{display:none}\">");

        List preLoadInfos = new ArrayList();
        for (Class c : preLoadClasses) {
            preLoadInfos.add(new ClassLoadingInfo(c.getName(), c.getClassLoader(), c.getProtectionDomain(), -1));
        }
        flushPackageView0(pw, preLoadInfos, locationTemplate);
        pw.println("</ul>");

        this.loaderNumber = 0;
        for (Map.Entry entry : classLoaders.entrySet()) {
            ClassLoader loader = (ClassLoader)entry.getKey();

            String idSuffix = "loader" + this.loaderNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\">" + toShort(loader) + ", " + (
                    entry.getValue() == null ? 0 : ((List)entry.getValue()).size()) + " classes</li>");
            if ((entry.getValue() != null) && (!((List)entry.getValue()).isEmpty())) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                if (((List)entry.getValue()).size() <= 6) {
                    for (ClassLoadingInfo info : (List)entry.getValue())
                        pw.println("<li>" + info.getName() + ", " + toShort(info.getLocation()) + ", " + info.getOrder() + "</li>");
                }
                else {
                    flushPackageView0(pw, (Collection)entry.getValue(), loaderTemplate);
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }

    private void flushJarView(PrintWriter pw, Map<URL, List<ClassLoadingInfo>> locations) {
        pw.println("<p><li id=li_jarview onclick=\"onItemClick('jarview')\" style=\"{list-style-type:circle;}\">Physical Location View</li>");
        pw.println("<ul id=ul_jarview>");

        this.libNumber = 0;
        for (Map.Entry entry : locations.entrySet()) {
            URL url = (URL)entry.getKey();

            String idSuffix = "url" + this.libNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\" >" + toShort(url) + ", " + (
                    entry.getValue() == null ? 0 : ((List)entry.getValue()).size()) + " classes</li>");
            if ((entry.getValue() != null) && (!((List)entry.getValue()).isEmpty())) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                for (ClassLoadingInfo info : (List)entry.getValue()) {
                    pw.println("<li>" + info.getName() + ", " + toShort(info.getLoader()) + ", " + info.getOrder() + "</li>");
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }

    private void flushJarViewHierarchical(PrintWriter pw, Map<URL, List<ClassLoadingInfo>> locations) {
        pw.println("<p><li id=li_jarview onclick=\"onItemClick('jarview')\" style=\"{list-style-type:circle;}\">Physical Location View</li>");
        pw.println("<ul id=ul_jarview>");

        this.libNumber = 0;
        for (Map.Entry entry : locations.entrySet()) {
            URL url = (URL)entry.getKey();

            String idSuffix = "url" + this.libNumber++;
            pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\" >" + toShort(url) + ", " + (
                    entry.getValue() == null ? 0 : ((List)entry.getValue()).size()) + " classes</li>");
            if ((entry.getValue() != null) && (!((List)entry.getValue()).isEmpty())) {
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                if (((List)entry.getValue()).size() <= 6) {
                    for (ClassLoadingInfo info : (List)entry.getValue())
                        pw.println("<li>" + info.getName() + ", " + toShort(info.getLoader()) + ", " + info.getOrder() + "</li>");
                }
                else {
                    flushPackageView0(pw, (Collection)entry.getValue(), locationTemplate);
                }
                pw.println("</ul>");
            }
        }
        pw.println("</ul>");
    }

    private void flushPackageView(PrintWriter pw, Collection<ClassLoadingInfo> allClasses) {
        pw.println("<p><li id=li_packageview onclick=\"onItemClick('packageview')\" style=\"{list-style-type:circle;}\">Package View</li>");
        pw.println("<ul id=ul_packageview>");
        flushPackageView0(pw, allClasses, packageTemplate);
        pw.println("</ul>");
    }

    private void flushPackageView0(PrintWriter pw, Collection<ClassLoadingInfo> allClasses, MessageFormat msgTemplate)
    {
        Tree pkgTree = new TreeImpl(null, "");
        for (ClassLoadingInfo info : allClasses) {
            String name = info.getName();
            String[] dirs = name.split("\\.");
            Tree classleaf = pkgTree.mkdirs(dirs);
            if (classleaf.getEntity() == null) {
                classleaf.setEntity(info);
            }
            else {
                classleaf.getParent().addChild(new TreeImpl(info, dirs[(dirs.length - 1)]));
            }
        }
        passHierarchy(pw, pkgTree, msgTemplate);
    }

    private void passHierarchy(PrintWriter pw, Tree<ClassLoadingInfo, String> pkgTree, MessageFormat msgTemplate)
    {
        Collections.sort(pkgTree.getChildren(), comparator);

        for (Tree tree : pkgTree.getChildren()) {
            String item = (String)tree.getId();
            if (tree.isLeaf()) {
                if (tree.getEntity() != null)
                {
                    item = msgTemplate.format(new Object[] { tree.getId(), toShort(((ClassLoadingInfo)tree.getEntity()).getLocation()),
                            toShort(((ClassLoadingInfo)tree.getEntity()).getLoader()), Integer.valueOf(((ClassLoadingInfo)tree.getEntity()).getOrder()) });
                }
                pw.println("<li>" + item + "</li>");
            }
            else {
                String idSuffix = "pkg" + this.pkgNumber++;
                pw.println("<li id=li_" + idSuffix + " onclick=\"onItemClick('" + idSuffix + "')\" style=\"{list-style-type:circle;}\">" + item + "</li>");
                pw.println("<ul id=ul_" + idSuffix + " style=\"{display:none}\">");
                passHierarchy(pw, tree, msgTemplate);
                pw.println("</ul>");
            }
        }
    }

    private static String getPath(Tree<ClassLoadingInfo, String> tree)
    {
        StringBuilder path = new StringBuilder((String)tree.getId());
        while ((tree = tree.getParent()) != null)
            path.insert(0, "_").insert(0, (String)tree.getId());
        return path.toString();
    }

    public static void main(String[] args) throws MalformedURLException
    {
        List infos = new ArrayList();
        infos.add(
                new ClassLoadingInfo("org.hqm.common.MD5", ClassLoader.getSystemClassLoader(),
                        new URL("file:///c:/object.jar"), 0));
        infos.add(
                new ClassLoadingInfo("org.hqm.common.util.StringUtil", ClassLoader.getSystemClassLoader(),
                        new URL("file:///c:/object.jar"), 1));
        infos.add(
                new ClassLoadingInfo("org.hqm.tool.JvmInspector", ClassLoader.getSystemClassLoader(),
                        new URL("file:///c:/object.jar"), 2));
        infos.add(
                new ClassLoadingInfo("org.hqm.tool.uml.UmlTool", ClassLoader.getSystemClassLoader(),
                        new URL("file:///c:/object.jar"), 3));

        Map map1 = new HashMap();
        Map map2 = new HashMap();
        try {
            new HtmlFlusher().flush(new PrintWriter(System.out), map1, map2, infos, new Class[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnableHyperlink()
    {
        return this.enableHyperlink;
    }

    public void setEnableHyperlink(boolean enableHyperlink)
    {
        this.enableHyperlink = enableHyperlink;
    }
}