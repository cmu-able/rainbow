/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.sa.rainbow.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.health.IRainbowHealthProtocol;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.translator.probes.IBashBasedScript;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    static final Logger LOGGER = Logger.getLogger (Util.class);

    public static final String            FILESEP                  = "/";
    public static final String            PATHSEP                  = ":";
    public static final String            LOGEXT                   = ".log";
    /** Usual newline sequence of character. */
    public static final String            NEWLINE                  = "\n";
    /** Windows newline sequence of characters. */
    public static final String            NEWLINE_WIN              = "\r\n";
    public static final String            AT                       = "@";
    public static final String            DOT                      = ".";
    public static final String            DASH                     = "-";
    public static final String            SIZE_SFX                 = ".size";
    /** Sets the byte arrays to the usual buffer size of 8 KB. */
    public static final int               MAX_BYTES                = 8 * 1024;
    /**
     * Sets the limit on StringBuffer to a threshold max, above which a new StringBuffer should be constructed using
     * less than 1/8 of original.
     */
    public static final int               MAX_STRING_BUFFER_LENGTH = 8 * 64000;                                        // max of 500KB

    // Patterns for tokens to be replaced in properties
    public static final String            TOKEN_BEGIN              = "${";
    public static final String            TOKEN_END                = "}";
    private static final String           LB_ESC                   = "\\$\\{";                                         // make sure this corresponds
    private static final String           RB_ESC                   = "\\}";                                            // make sure this corresponds
    private static final Pattern m_substitutePattern = Pattern.compile ("[^\\$]*" + LB_ESC + "(.+?)"
            + RB_ESC + "[^\\$]*");


    public static File computeBasePath (final String configPath) {
        // determine path to target config dir
        File basePath = new File (System.getProperty ("user.dir"));
        LOGGER.debug (MessageFormat.format ("CWD: {0}, looking for config \"{1}\"", basePath, configPath));
        FilenameFilter configFilter = new FilenameFilter () {
            @Override
            public boolean accept (File dir, String name) {
                return name.equals (configPath);
            }
        };
        // try for the CONFIG path in this path (if invoked from Jar), or parent path
        File[] list = basePath.listFiles (configFilter);
        if (list.length == 0) { // try parent
            list = basePath.getParentFile ().listFiles (configFilter);
        }
        if (list.length > 0) {
            basePath = list[0];
        }
        else {
            basePath = null;
        }
        return basePath;
    }

    public static File getRelativeToPath (File parent, String relPath) {
        try {
            return new File (parent, relPath).getCanonicalFile ();
        }
        catch (IOException e) {
            LOGGER.error (MessageFormat.format ("Failed to get relative to path {0}{2}{1}", parent.getAbsolutePath (),
                    relPath, File.separator), e);
        }
        return null;
    }

    public static String unifyPath (String path) {
        if (File.separator != FILESEP) return path.replace (File.separator, FILESEP);
        return path;
    }


    public static String evalTokens (String str, Properties props) {
        if (str == null) return str;
        if (props == null) return str;
        String result = str;
        Matcher m = m_substitutePattern.matcher (str);
        while (m.find ()) {
            String subsKey = m.group (1);
            String tgt = TOKEN_BEGIN + subsKey + TOKEN_END;
            String tgtVal = props.getProperty (subsKey);
            if (tgtVal == null) {
                LOGGER.error (MessageFormat.format (
                        "Undefined Rainbow property for token substitution: \"{0}\" in \"{1}\"", subsKey, str));
            }
            else {
                result = result.replace (tgt, tgtVal);
            }
        }
        return result;
    }


    public static String evalTokens (String str) {
        if (str == null) return str;
        return evalTokens (str, Rainbow.instance ().allProperties ());
    }


    public static String[] evalCommandParameters (String value) {
        String regex = ",|\\.|\\(|\\)";
        String[] split = value.split (regex);

        String[] ret = new String[split.length - 1];
        ret[0] = split[0];
        // The command name is the second in the split, so skip over it.
        System.arraycopy (split, 2, ret, 1, split.length - 1 - 1);
        return ret;
    }


    public static String[] evalCommand (String value) {
        String[] split = value.split (",|\\.|\\(|\\|");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim ();
        }
        return split;
    }



    //////////////////////////////////////////////
    //Type manipulation utility
    //

    private static Map<String, Class<?>>   m_primName2Class  = null;

    private static Map<Class<?>, Class<?>> m_primClass2Class = null;

    private static void lazyInitMaps () {
        if (m_primName2Class != null) return; // already init'd
        // Map of primitive type names to Java Class
        m_primName2Class = new HashMap<> ();
        m_primName2Class.put ("int", Integer.class);
        m_primName2Class.put ("short", Short.class);
        m_primName2Class.put ("long", Long.class);
        m_primName2Class.put ("float", Float.class);
        m_primName2Class.put ("double", Double.class);
        m_primName2Class.put ("boolean", Boolean.class);
        m_primName2Class.put ("byte", Byte.class);
        m_primName2Class.put ("char", Character.class);
        // Map of primitive type class to Java Class
        m_primClass2Class = new HashMap<> ();
        m_primClass2Class.put (int.class, Integer.class);
        m_primClass2Class.put (short.class, Short.class);
        m_primClass2Class.put (long.class, Long.class);
        m_primClass2Class.put (float.class, Float.class);
        m_primClass2Class.put (double.class, Double.class);
        m_primClass2Class.put (boolean.class, Boolean.class);
        m_primClass2Class.put (byte.class, Byte.class);
        m_primClass2Class.put (char.class, Character.class);
    }

    public static Object parseObject (String val, String classStr) {
        Object valObj = val; // by default, return just the String value
        try {
            Class<?> typeClass = Class.forName (classStr);
            // try first parsing a primitive value
            valObj = parsePrimitiveValue (val, typeClass);
            if (valObj == val) { // now trying instantiating the class
                valObj = typeClass.newInstance ();
            }
        }
        catch (ClassNotFoundException e) {
            // see if it's one of the primitive types
            lazyInitMaps ();
            valObj = parsePrimitiveValue (val, m_primName2Class.get (classStr.intern ()));
        } catch (InstantiationException | IllegalAccessException e) {
        }
        return valObj;
    }

    public static Object parsePrimitiveValue (String val, Class<?> clazz) {
        if (clazz == null) return val;

        Object primVal = val; // by default, return the String value
        if (clazz.isPrimitive ()) { // get the Java Class
            lazyInitMaps ();
            clazz = m_primClass2Class.get (clazz);
        }
        // look for a valueOf(String) method, since the eight Classes that
        // represent the primitives each has such a static method
        Class<?>[] paramTypes = new Class<?>[1];
        paramTypes[0] = String.class;
        try { // catch and ignore any exception, which we treat as failing to "convert"
            Method m = clazz.getDeclaredMethod ("valueOf", paramTypes);
            Object[] args = new Object[1];
            args[0] = val;
            primVal = m.invoke (null, args);
        } catch (SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException |
                NoSuchMethodException e) {
        }
        return primVal;
    }

    public static Logger logger () {
        return LOGGER;
    }

    /**
     * Generates and returns a unique identifier composed of name@target, target converted to lowercase.
     * 
     * @param name
     *            the element name
     * @param target
     *            the element's location in lowercase
     * @return String the concatenated string forming the unique identifier
     */

    public static String genID (String name, String target) {
        return name + AT + target.toLowerCase ();
    }


    public static Pair<String, String> decomposeID (String id) {
        String name;
        String loc = null;
        int atIdx = id.indexOf (AT);
        if (atIdx > -1) { // got both name and target location
            name = id.substring (0, atIdx);
            loc = id.substring (atIdx + AT.length ()).toLowerCase ();
        }
        else { // name only
            name = id;
        }
        return new Pair<> (name, loc);
    }


    public static ModelReference decomposeModelReference (String modelRef) {
        String name;
        String type = null;
        int atIdx = modelRef.indexOf (':');
        if (atIdx > -1) { // got both name and target location
            name = modelRef.substring (0, atIdx);
            type = modelRef.substring (atIdx + ":".length ());
        }
        else { // name only
            name = modelRef;
        }
        return new ModelReference (name, type);
    }


    public static String genModelRef (String modelName, String modelType) {
        return modelName + ":" + modelType;
    }

    /**
     * Reads and returns all available output from a given Process.
     * 
     * @param p
     *            the process to read output from
     * @return String the textual output, with any MS-Dos newline replaced
     */
    public static String getProcessOutput (Process p) {
        String str = "";
        try {
            StringBuilder buf = new StringBuilder ();
            byte[] bytes = new byte[Util.MAX_BYTES];
            BufferedInputStream bis = new BufferedInputStream (p.getInputStream ());
            while (bis.available () > 0) {
                int cnt = bis.read (bytes);
                buf.append (new String (bytes, 0, cnt));
            }
            str = buf.toString ().replace (NEWLINE_WIN, NEWLINE);
        }
        catch (IOException e) {
            logger ().error ("Get process output failed!", e);
        }
        return str;
    }

    public static void setExecutablePermission (String path) {
        String[] cmds = { IBashBasedScript.LINUX_CHMOD, IBashBasedScript.CHMOD_OPT, path };
        ProcessBuilder pb = new ProcessBuilder (cmds);
        pb.redirectErrorStream (true);
        try {
            Process p = pb.start ();
            try {
                p.waitFor ();
            }
            catch (InterruptedException e) {
                logger ().error ("chmod interrupted?", e);
            }
            String pOut = Util.getProcessOutput (p);
            if (pOut.length () > 2) { // probably some useful output
                logger ().info ("- Chmod output: " + pOut);
            }
        }
        catch (IOException e) {
            logger ().error ("Process I/O failed!", e);
        }
    }

//////////////////////////////////////////////
//Date and Time utility methods
//
    private static final SimpleDateFormat m_timestampFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    /** Returns current timestamp string of the form yyyyMMddHHmmssSSS */
    public static String timestamp () {
        return m_timestampFormat.format(Calendar.getInstance().getTime());
    }
    private static final SimpleDateFormat m_timestampShortFormat = new SimpleDateFormat("MMddHHmmssSSS");
    /** Returns current timestamp string of the form MMddHHmmssSSS */
    public static String timestampShort () {
        return m_timestampShortFormat.format(Calendar.getInstance().getTime());
    }
    private static final SimpleDateFormat m_timelogFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.SSSZ");
    /** Returns current timestamp string of the form yyyy.MM.dd-HH:mm:ss.SSSZ */
    public static String timelog () {
        return m_timelogFormat.format(Calendar.getInstance().getTime());
    }
    private static final SimpleDateFormat m_probeLogFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
    /** Returns current timestamp string of the form EEE MMM d HH:mm:ss yyyy */
    public static String probeLogTimestamp () {
        return m_probeLogFormat.format(Calendar.getInstance().getTime());
    }
    /** Returns timestamp string of the form EEE MMM d HH:mm:ss yyyy for the supplied time in milliseconds */
    public static String probeLogTimestampFor (long milliseconds) {
        return m_probeLogFormat.format(milliseconds);
    }

    public static void pause (long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // intentionally ignore
        }
    }
//    public static void waitUntilDisposed (IDisposable disposable) {
//        while (! disposable.isDisposed()) {
//            try {
//                Thread.sleep(IRainbowRunnable.SLEEP_TIME);
//            } catch (InterruptedException e) {
//                // intentional ignore
//            }
//        }
//
//
//    }

//////////////////////////////////////////////
//Debug settings and utility methods
//
    public static final String   DEFAULT_LEVEL       = "WARN";
    /** ISO8601 prints the format "yyyy-MM-dd HH:mm:ss,SSS" */
    public static final String   DEFAULT_PATTERN     = "%d{ISO8601} [%t] %p %c %x - %m%n";
    public static final int      DEFAULT_MAX_SIZE    = 1024;
    public static final int      DEFAULT_MAX_BACKUPS = 5;
    public static final String   CONSOLE_PATTERN     = "[%d{ISO8601}] %m%n";
    public static final String   DATA_LOGGER_NAME    = "DataLogger";
    public static final String   DATA_PATTERN        = "%d{ISO8601} %m%n";


    public static Properties defineLoggerProperties () {
        String filepath = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_PATH);
        if (!filepath.startsWith ("/")) {
            filepath = getRelativeToPath (Rainbow.instance ().getTargetPath (), filepath).toString ();
        }
        String datapath = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_DATA_LOG_PATH);
        if (!datapath.startsWith ("/")) {
            datapath = getRelativeToPath (Rainbow.instance ().getTargetPath (), datapath).toString ();
        }

// setup logging
        Properties props = new Properties ();
        props.setProperty ("log4j.appender.FileLog", "org.apache.log4j.RollingFileAppender");
        props.setProperty ("log4j.appender.FileLog.layout", "org.apache.log4j.PatternLayout");
        props.setProperty ("log4j.appender.FileLog.layout.ConversionPattern",
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_PATTERN, DEFAULT_PATTERN));
        props.setProperty ("log4j.appender.FileLog.MaxFileSize",
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_MAX_SIZE, String.valueOf (DEFAULT_MAX_SIZE)) + "KB");
        props.setProperty ("log4j.appender.FileLog.MaxBackupIndex",
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_MAX_BACKUPS, String.valueOf (DEFAULT_MAX_BACKUPS)));
        props.setProperty ("log4j.appender.FileLog.File", filepath);
        props.setProperty ("log4j.appender.ConsoleLog", "org.apache.log4j.ConsoleAppender");
        props.setProperty ("log4j.appender.ConsoleLog.Target", "System.out");
        props.setProperty ("log4j.appender.ConsoleLog.layout", "org.apache.log4j.PatternLayout");
        props.setProperty ("log4j.appender.ConsoleLog.layout.ConversionPattern", CONSOLE_PATTERN);
        String rootSetting = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_LEVEL, DEFAULT_LEVEL) + ",FileLog,ConsoleLog";
        props.setProperty ("log4j.rootLogger", rootSetting);
        // setup data logging, using trace level
        props.setProperty ("log4j.appender.DataLog", "org.apache.log4j.RollingFileAppender");
        props.setProperty ("log4j.appender.DataLog.layout", "org.apache.log4j.PatternLayout");
        props.setProperty ("log4j.appender.DataLog.layout.ConversionPattern", DATA_PATTERN);
        props.setProperty ("log4j.appender.DataLog.MaxFileSize",
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_MAX_SIZE, String.valueOf (DEFAULT_MAX_SIZE)) + "KB");
        props.setProperty ("log4j.appender.DataLog.MaxBackupIndex",
                Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_LOG_MAX_BACKUPS, String.valueOf (DEFAULT_MAX_BACKUPS)));
        props.setProperty ("log4j.appender.DataLog.File", datapath);
        props.setProperty ("log4j.logger." + DATA_LOGGER_NAME, "INFO,DataLog");
        // don't invoke ancester appenders
        props.setProperty ("log4j.additivity." + DATA_LOGGER_NAME, "false");

        return props;
    }


    private static Logger m_dataLogger = null;


    public static Logger dataLogger () {
        if (m_dataLogger == null) {
//            PropertyConfigurator.configure (defineLoggerProperties ());
            m_dataLogger = Logger.getLogger (DATA_LOGGER_NAME);
        }
        return m_dataLogger;
    }

    /**
     * Throws and catches a {@link Throwable}, and then reports the stack. This is useful for finding call traces
     * 
     * @param message
     *            the message to report
     */
    public static void reportStack (String message) {
        logger ().trace (message);
        try {
            throw new Throwable (message);
        }
        catch (Throwable t) {
            logger ().trace ("", t);
        }
    }

    public static void reportMemUsage () {
        dataLogger ().info (IRainbowHealthProtocol.DATA_MEMORY_USE + Runtime.getRuntime ().freeMemory () + " " + Runtime.getRuntime ().totalMemory () + " " + Runtime.getRuntime ().maxMemory () + " ");
    }

    /**
     * Lists Classes inside a given package.
     * 
     * @author Jon Peck http://jonpeck.com (adapted from http://www.javaworld.com/javaworld/javatips/jw-javatip113.html)
     * @param pkgname
     *            String name of a Package, e.g., "java.lang"
     * @return Class[] classes inside the root of the given package
     * @throws ClassNotFoundException
     *             if the package name points to an invalid package
     */

    public static Class[] getClasses (String pkgname) throws ClassNotFoundException {
        List<ClassLoader> classLoaderList = new LinkedList<> ();
        classLoaderList.add (ClasspathHelper.contextClassLoader ());
        classLoaderList.add (ClasspathHelper.staticClassLoader ());

        Reflections reflections = new Reflections (new ConfigurationBuilder ()
        .setScanners (new SubTypesScanner (false /* don't exclude Object.class */), new ResourcesScanner ())
                .setUrls (ClasspathHelper.forClassLoader (classLoaderList.toArray (new ClassLoader[classLoaderList.size ()])))
        .filterInputsBy (new FilterBuilder ().include (FilterBuilder.prefix (pkgname))));
        Set<Class<?>> classes = reflections.getSubTypesOf (Object.class);
        if (classes != null) {
            LinkedList<Class<?>> ll = new LinkedList<> (classes);
            return ll.toArray (new Class[ll.size ()]);
        }
        return new Class[0];
//        List<Class<?>> classes = new ArrayList<Class<?>> ();
//        // either get a File or JarFile object for the package
//        String pkgdir = pkgname.replace ('.', '/');
//        File directory = null;
//        JarFile jarFile = null;
//        try {
//            URL pkgURL = Thread.currentThread ().getContextClassLoader ().getResource (pkgdir);
//            logger ().debug ("*~* Got resource: " + pkgURL.toExternalForm ());
//            // see if file is inside a JAR
//            if (pkgURL.getProtocol ().equalsIgnoreCase ("jar")) {
//                // obtain the Jar URL connection to get the JAR file
//                JarURLConnection jarConn = (JarURLConnection )pkgURL.openConnection ();
//                jarFile = jarConn.getJarFile ();
//            }
//            else {
//                directory = new File (pkgURL.getFile ());
//            }
//        }
//        catch (Exception e) { // expect NullPointerException or IOException
//            String msg = pkgname + " does not appear to be a valid package!";
//            logger ().error (msg, e);
//            throw new ClassNotFoundException (pkgname + " does not appear to be a valid package!", e);
//        }
//        if (directory != null && directory.exists ()) { // search ordinary directory
//            // get the list of files contained in the package
//            String[] files = directory.list (new FilenameFilter () {
//                @Override
//                public boolean accept (File dir, String name) {
//                    // we're only interested in .class files
//                    return name.endsWith (".class");
//                }
//            });
//            for (String fname : files) { // remove .class extension and add class
//                classes.add (Class.forName (pkgname + '.' + fname.substring (0, fname.length () - 6)));
//            }
//        }
//        else if (jarFile != null) { // sift through a jar file
//            // look for files that has 'pkgname' after ! and ends with class
//            Pattern p = Pattern.compile ("[!]/" + pkgname + "/(.+)[.]class");
//            Enumeration<JarEntry> entries = jarFile.entries ();
//            while (entries.hasMoreElements ()) {
//                JarEntry e = entries.nextElement ();
//                Matcher m = p.matcher (e.getName ());
//                if (m.matches ()) { // found one
//                    classes.add (Class.forName (pkgname + '.' + m.group (1)));
//                }
//            }
//        }
//        else
//            throw new ClassNotFoundException (pkgname + " package does not correspond to an existent directory!");
//
//        Class<?>[] classArray = new Class<?>[classes.size ()];
//        classes.toArray (classArray);
//        return classArray;
    }

    public static Level reportTypeToPriority (ReportType type) {
        switch (type) {
        case INFO:
            return Level.INFO;
        case ERROR:
            return Level.ERROR;
        case FATAL:
            return Level.FATAL;
        case WARNING:
            return Level.WARN;
        }
        return Level.ERROR;
    }


    public static Properties propertiesByPrefix (String prefix, Properties props) {
        Properties result = new Properties ();
        for (Object o : props.keySet ()) {
            String key = (String )o;
            if (!key.startsWith (prefix) || key.equals (prefix)) {
                continue;
            }
            result.put (key, props.getProperty (key));
        }
        return result;
    }


    public static Properties propertiesByRegex (String regex, Properties props) {
        Properties result = new Properties ();
        for (Object o : props.keySet ()) {
            String key = (String )o;
            if (key.matches (regex)) {
                result.put (key, props.getProperty (key));
            }
        }
        return result;
    }

    public static boolean safeEquals (Object a, Object b) {
        return a != null ? a.equals (b) : b == null;
    }
}