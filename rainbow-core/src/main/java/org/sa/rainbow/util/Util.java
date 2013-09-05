package org.sa.rainbow.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.util.Pair;

public class Util {

    static Logger               LOGGER              = Logger.getLogger (Util.class);

    private static final String FILESEP             = "/";
    public static final String  DOT                 = ".";
    public static final String  SIZE_SFX            = ".size";

    // Patterns for tokens to be replaced in properties
    public static final String  TOKEN_BEGIN         = "${";
    public static final String  TOKEN_END           = "}";
    private static final String LB_ESC              = "\\$\\{";                     // make sure this corresponds
    private static final String RB_ESC              = "\\}";                        // make sure this corresponds
    private static Pattern      m_substitutePattern = Pattern.compile ("[^\\$]*" + LB_ESC + "(.+?)" + RB_ESC
            + "[^\\$]*");

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
            File newF = new File (parent, relPath).getCanonicalFile ();
            return newF;
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
        return evalTokens (str, null);
    }

    public static String[] evalCommandParameters (String value) {
        String regex = ",|\\.|\\(|\\)";
        String[] split = value.split (regex);

        String[] ret = new String[split.length - 1];
        ret[0] = split[0];
        // The command name is the second in the split, so skip over it.
        for (int i = 1; i < split.length - 1; i++) {
            ret[i] = split[i + 1];
        }
        return ret;
    }

    private static final SimpleDateFormat m_timelogFormat = new SimpleDateFormat ("yyyy.MM.dd-HH:mm:ss.SSSZ");

    private static final String           AT              = "@";

    /** Returns current timestamp string of the form yyyy.MM.dd-HH:mm:ss.SSSZ */
    public static String timelog () {
        return m_timelogFormat.format (Calendar.getInstance ().getTime ());
    }

    //////////////////////////////////////////////
    //Type manipulation utility
    //
    private static Map<String, Class<?>>   m_primName2Class  = null;
    private static Map<Class<?>, Class<?>> m_primClass2Class = null;

    private static void lazyInitMaps () {
        if (m_primName2Class != null) return; // already init'd
        // Map of primitive type names to Java Class
        m_primName2Class = new HashMap<String, Class<?>> ();
        m_primName2Class.put ("int", Integer.class);
        m_primName2Class.put ("short", Short.class);
        m_primName2Class.put ("long", Long.class);
        m_primName2Class.put ("float", Float.class);
        m_primName2Class.put ("double", Double.class);
        m_primName2Class.put ("boolean", Boolean.class);
        m_primName2Class.put ("byte", Byte.class);
        m_primName2Class.put ("char", Character.class);
        // Map of primitive type class to Java Class
        m_primClass2Class = new HashMap<Class<?>, Class<?>> ();
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
        }
        catch (InstantiationException e) {
        }
        catch (IllegalAccessException e) {
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
        }
        catch (SecurityException e) {
        }
        catch (NoSuchMethodException e) {
        }
        catch (IllegalArgumentException e) {
        }
        catch (IllegalAccessException e) {
        }
        catch (InvocationTargetException e) {
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
        String name = null;
        String loc = null;
        int atIdx = id.indexOf (AT);
        if (atIdx > -1) { // got both name and target location
            name = id.substring (0, atIdx);
            loc = id.substring (atIdx + AT.length ()).toLowerCase ();
        }
        else { // name only
            name = id;
        }
        return new Pair<String, String> (name, loc);
    }

}
