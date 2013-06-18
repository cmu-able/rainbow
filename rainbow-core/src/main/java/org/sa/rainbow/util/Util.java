package org.sa.rainbow.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Util {

    static Logger               LOGGER              = Logger.getLogger (Util.class);

    private static final String FILESEP = "/";
    public static final String  DOT     = ".";
    public static final String  SIZE_SFX = ".size";

    // Patterns for tokens to be replaced in properties
    public static final String  TOKEN_BEGIN         = "${";
    public static final String  TOKEN_END           = "}";
    private static final String LB_ESC              = "\\$\\{";          // make sure this corresponds
    private static final String RB_ESC              = "\\}";             // make sure this corresponds
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
            LOGGER.error (
                    MessageFormat.format ("Failed to get relative to path {0}{2}{1}", parent.getAbsolutePath (),
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
                LOGGER.error (
                        MessageFormat.format ("Undefined Rainbow property for token substitution: \"{0}\" in \"{1}\"",
                                subsKey, str));
            }
            else {
                result = result.replace (tgt, tgtVal);
            }
        }
        return result;
    }

    private static final SimpleDateFormat m_timelogFormat = new SimpleDateFormat ("yyyy.MM.dd-HH:mm:ss.SSSZ");

    /** Returns current timestamp string of the form yyyy.MM.dd-HH:mm:ss.SSSZ */
    public static String timelog () {
        return m_timelogFormat.format (Calendar.getInstance ().getTime ());
    }
}
