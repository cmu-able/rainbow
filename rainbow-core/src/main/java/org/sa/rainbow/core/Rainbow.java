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
package org.sa.rainbow.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.util.Util;

/**
 * A singleton class that provides utilities for reading properties, and getting access to important Rainbow Framework
 * services.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class Rainbow implements RainbowConstants {
    static Logger LOGGER = Logger.getLogger (Rainbow.class);

    /**
     * States used to track the target deployment environment of Rainbow component.
     * 
     * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
     */
    public static enum Environment {
        /** We don't yet know what deployment environment. */
        UNKNOWN,
        /** We're in a Linux environment. */
        LINUX,
        /** We're in a Cygwin environment. */
        CYGWIN,
        /** We're in a Mac FreeBSD environment. */
        MAC,
        /** We're in a Windows environment without Cygwin. */
        WINDOWS
    };

    /**
     * States used to help the Rainbow daemon process determine what to do after this Rainbow component exits.
     * 
     * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
     */
    public static enum ExitState {
        /**
         * Completely clear out (daemon dies) after the Rainbow component exits (default).
         */
        DESTRUCT,
        /** Restart the Rainbow component after exits. */
        RESTART,
        /**
         * After the Rainbow component exits, sleep and await awake command to restart Rainbow.
         */
        SLEEP,
        /** Abort of operation. */
        ABORT;

        public static ExitState parseState (int val) {
            ExitState st = ExitState.DESTRUCT;
            switch (val) {
            case EXIT_VALUE_DESTRUCT:
                st = ExitState.DESTRUCT;
                break;
            case EXIT_VALUE_RESTART:
                st = ExitState.RESTART;
                break;
            case EXIT_VALUE_SLEEP:
                st = ExitState.SLEEP;
                break;
            case EXIT_VALUE_ABORT:
                st = ExitState.ABORT;
                break;
            }
            return st;
        }

        public int exitValue () {
            int ev = 0;
            switch (this) {
            case DESTRUCT:
                ev = EXIT_VALUE_DESTRUCT;
                break;
            case RESTART:
                ev = EXIT_VALUE_RESTART;
                break;
            case SLEEP:
                ev = EXIT_VALUE_SLEEP;
                break;
            case ABORT:
                ev = EXIT_VALUE_ABORT;
                break;
            }
            return ev;
        }
    }

    /** The thread name */
    public static final String NAME        = "Rainbow Runtime Infrastructure";



    private static final String PROPKEY_PROPFILENAME = "rainbow.properties";

    /**
     * Exit status that Rainbow would report when it exits, default to sleeping.
     */
    private static ExitState   m_exitState = ExitState.SLEEP;

    /**
     * Singleton instance of Rainbow
     */
    private static Rainbow m_instance = null;

    private static Map<String, IGauge> m_id2Gauge;

    private static Environment         m_env             = Environment.UNKNOWN;

    private boolean            m_shouldTerminate = false;

    public synchronized static Rainbow instance () {
        if (m_instance == null) {
            m_instance = new Rainbow ();
        }
        return m_instance;
    }

    /**
     * Returns whether the Rainbow runtime infrastructure should terminate.
     * 
     * @return <code>true</code> if Rainbow should terminate, <code>false</code> otherwise.
     */
    public static boolean shouldTerminate () {
        return instance ().m_shouldTerminate;
    }

    /**
     * Sets the shouldTerminate flag so that the Rainbow Runtime Infrastructure parts know to terminate. This method is
     * intended primarily for {@link org.sa.rainbow.core.Oracle <code>Oracle</code>}, but may be used by the
     * UpdateService to signal termination.
     */
    public static void signalTerminate () {
        if (!instance ().m_shouldTerminate) { // log once the signalling to
            // terminate
            LOGGER.info ("*** Signalling Terminate ***");
        }
        instance ().m_shouldTerminate = true;
    }

    public static void signalTerminate (ExitState exitState) {
        setExitState (exitState);
        signalTerminate ();
    }

    /**
     * Returns the exit value given the Rainbow exit state; this is stored statically since it would only be used once.
     * 
     * @return int the exit value to return on System exit.
     */
    public static int exitValue () {
        return m_exitState.exitValue ();
    }

    public static void setExitState (ExitState state) {
        m_exitState = state;
    }

    public static boolean isMaster () {
        return instance ().m_isMaster;
    }

    private Properties m_props;
    private File       m_basePath;
    private File       m_targetPath;

    private ThreadGroup m_threadGroup;

    /** Indicates whether this instance is the master or a delegate **/
    private boolean     m_isMaster = false;

    private RainbowMaster m_rainbowMaster;

    private Properties    m_defaultProps;

    private Rainbow () {
        m_props = new Properties ();
        m_id2Gauge = new HashMap<> ();
        m_threadGroup = new ThreadGroup (NAME);
        establishPaths ();
        loadConfigFiles ();
        canonicalizeHost2IPs ();
        evalPropertySubstitution ();
    }

    public static String getProperty (String key, String defaultProperty) {
        return instance ().m_props.getProperty (key, instance ().m_defaultProps.getProperty (key, defaultProperty));
    }

    public static String getProperty (String key) {
        return instance ().m_props.getProperty (key, instance ().m_defaultProps.getProperty (key));
    }

    public static boolean getProperty (String key, boolean b) {
        String value = instance ().m_props.getProperty (key);
        if (value != null)
            return Boolean.valueOf (value);
        else
            return b;
    }

    public static long getProperty (String key, long default_) {
        String value = instance ().m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Long.parseLong (value);
        }
        catch (NumberFormatException e) {
            return default_;
        }
    }

    public static short getProperty (String key, short default_) {
        String value = instance ().m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Short.parseShort (value);
        }
        catch (NumberFormatException e) {
            return default_;
        }
    }

    public static int getProperty (String key, int default_) {
        String value = instance ().m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Integer.parseInt (value);
        }
        catch (NumberFormatException e) {
            return default_;
        }
    }

    public static double getProperty (String key, double default_) {
        String value = instance ().m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Double.parseDouble (value);
        }
        catch (NumberFormatException e) {
            return default_;
        }
    }

    public static void setProperty (String key, short val) {
        instance ().m_props.setProperty (key, Short.toString (val));
    }

    public static void setProperty (String key, long val) {
        instance ().m_props.setProperty (key, Long.toString (val));
    }

    public static void setProperty (String key, boolean val) {
        instance ().m_props.setProperty (key, Boolean.toString (val));
    }

    public static void setProperty (String key, String val) {
        instance ().m_props.setProperty (key, val);
    }

    public static void setProperty (String key, double val) {
        instance ().m_props.setProperty (key, Double.toString (val));
    }

    public static void setProperty (String key, int val) {
        instance ().m_props.setProperty (key, Integer.toString (val));
    }

    public static Properties allProperties () {
        return instance ().m_props;
    }


    /**
     * Determines and configures the paths to the Rainbow base installation and target configuration files
     */
    private void establishPaths () {
        String cfgPath = System.getProperty (PROPKEY_CONFIG_PATH, RAINBOW_CONFIG_PATH); // The location of targets
        String target = System.getProperty (PROPKEY_TARGET_NAME, DEFAULT_TARGET_NAME); // The target to use
        String propFile = System.getProperty (PROPKEY_CONFIG_FILE, null);

        m_props.setProperty (PROPKEY_CONFIG_PATH, cfgPath);
        m_props.setProperty (PROPKEY_TARGET_NAME, target);
        if (propFile != null) {
            m_props.setProperty (PROPKEY_CONFIG_FILE, propFile);
        }
        m_basePath = Util.computeBasePath (cfgPath);
        if (m_basePath == null) {
            String errorMsg = MessageFormat.format ("Configuration path {0} NOT found,  bailing.", cfgPath);
            LOGGER.error (errorMsg);
            throw new RainbowAbortException (errorMsg);
        }

        m_targetPath = Util.getRelativeToPath (m_basePath, target);
        try {
            m_props.setProperty (PROPKEY_TARGET_PATH, Util.unifyPath (m_targetPath.getCanonicalPath ()));
        }
        catch (IOException e) {
            LOGGER.error (e);
            if (m_targetPath == null) {
                String errMsg = MessageFormat.format ("Target configuration ''{0}'' NOT found, bailing!", target);
                LOGGER.error (errMsg);
                throw new RainbowAbortException (errMsg);
            }
        }

    }

    /**
     * Determine and load the appropriate sequence of Rainbow's config files
     */
    private void loadConfigFiles () {

        InputStream propStream = this.getClass ().getClassLoader ()
                .getResourceAsStream ("org/sa/rainbow/core/default.properties");

        m_defaultProps = new Properties ();
        try {
            m_defaultProps.load (propStream);
        }
        catch (IOException e1) {
            LOGGER.error (e1);
        }

        LOGGER.debug (MessageFormat.format ("Rainbow config path: {0}", m_targetPath.getAbsolutePath ()));

        computeHostSpecificConfig ();
        String cfgFile = m_props.getProperty (PROPKEY_CONFIG_FILE, DEFAULT_CONFIG_FILE);
        List<String> cfgFiles = new ArrayList<> ();
//        if (!cfgFile.equals (DEFAULT_CONFIG_FILE)) { 
//            // load commong config file first
//            cfgFiles.add (DEFAULT_CONFIG_FILE);
//        }
        cfgFiles.add (cfgFile);
        LOGGER.debug (
                MessageFormat.format ("Loading Rainbow config file(s): {0}", Arrays.toString (cfgFiles.toArray ())));

        // Load the properties in each cfgFile into the m_props of this method. This constitutes the properties of Rainbow
        // for this host.
        for (String cfg : cfgFiles) {
            try (FileInputStream pfIn = new FileInputStream (Util.getRelativeToPath (m_targetPath, cfg))) {
                m_props.load (pfIn);
            }
            catch (FileNotFoundException e) {
                LOGGER.error (e);
            }
            catch (IOException e) {
                LOGGER.error (e);
            }
        }
    }

    /**
     * Sanitizes the master, deployment, and all target location hostnames to their IP addresses. This method does value
     * substitution for the master and deployment hosts.
     */
    private void canonicalizeHost2IPs () {
        String masterLoc = m_props.getProperty (PROPKEY_MASTER_LOCATION);
        canonicalizeHost2IP ("Master Location", masterLoc, PROPKEY_MASTER_LOCATION);

        String deployLoc = m_props.getProperty (PROPKEY_DEPLOYMENT_LOCATION);
        canonicalizeHost2IP ("Deployment Location", deployLoc, PROPKEY_DEPLOYMENT_LOCATION);

        Properties customizedLocations = Util.propertiesByRegex (
                RainbowConstants.PROPKEY_TARGET_LOCATION + "\\.[^.]*$", m_props);
        customizedLocations.remove (RainbowConstants.PROPKEY_TARGET_LOCATION + Util.SIZE_SFX);
        for (Object o : customizedLocations.keySet ()) {
            String key = (String )o;
            String hostLoc = m_props.getProperty (key);
            canonicalizeHost2IP ("Target host location", hostLoc, key);
        }

//        // Resolve all of the mentioned target locations
//        int cnt = Integer.parseInt (m_props.getProperty (PROPKEY_TARGET_LOCATION + Util.SIZE_SFX, "0"));
//        for (int i = 0; i < cnt; i++) {
//            String propName = RainbowConstants.PROPKEY_TARGET_LOCATION + Util.DOT + i;
//            String hostLoc = m_props.getProperty (propName);
//            canonicalizeHost2IP ("Target host location", hostLoc, propName);
//        }

    }

    /**
     * Substitutes property values containing the pattern ${x} with the value that is mapped to the key "x".
     */
    private void evalPropertySubstitution () {
        for (Object kObj : m_props.keySet ()) {
            String key = (String )kObj;
            String val = m_props.getProperty (key);
            while (val.contains (Util.TOKEN_BEGIN)) {
                m_props.setProperty (key, Util.evalTokens (val, m_props));
                val = m_props.getProperty (key);
            }
        }
    }

    public static String canonicalizeHost2IP (String host) {
        try {
            host = InetAddress.getByName (host).getHostAddress ();
        }
        catch (UnknownHostException e) {
            LOGGER.error (MessageFormat.format ("{0} could not be resolved to an IP using the given name.", host));
        }
        return host;
    }

    private void canonicalizeHost2IP (String string, String masterLoc, String key) {
        masterLoc = Util.evalTokens (masterLoc, m_props);
        try {
            masterLoc = InetAddress.getByName (masterLoc).getHostAddress ();
            m_props.setProperty (key, masterLoc);
        }
        catch (UnknownHostException e) {
            LOGGER.warn (
                    MessageFormat.format (
                            "{1} ''{0}'' could not be resolved to an IP using the given name.",
                            masterLoc, string),
                    e);
        }
    }

    /**
     * Uses hostname on which this Rainbow component resides to determine if a host-specific Rainbow config file exists.
     * Sets the config file name to point to that file if yes. The attempts, in order, are:
     * <ol>
     * <li>lowercased OS-known (remebered) localhost name
     * <li>first segment (before first dot) of the remebered hostname
     * <li>IP number
     * <li>lowercased canonical hostname
     * <li>first segment of the canonical
     * </ol>
     * 
     * @throws SocketException
     */
    private void computeHostSpecificConfig () {
        List<String> triedHosts = new ArrayList<String> ();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces ();
            while (networkInterfaces.hasMoreElements ()) {
                NetworkInterface ni = networkInterfaces.nextElement ();
                Enumeration<InetAddress> addresses = ni.getInetAddresses ();
                while (addresses.hasMoreElements ()) {
                    InetAddress ia = addresses.nextElement ();
                    if (checkInetAddressConfigFile (ia, triedHosts)) return;
                }
            }
        }
        catch (SocketException e) {
        }
        LOGGER.error (MessageFormat.format ("Unable to find host-specific property file! Tried: {0}",
                Arrays.toString (triedHosts.toArray ())));
    }

    /**
     * Checks through all the possible host specific combinations: IP address, host.name, host, name
     * 
     * @param ia
     *            the InetAdress to check
     * @param triedHosts
     *            The combinations that have been tried
     * @return true after the the first properties file that matches is found
     */
    private boolean checkInetAddressConfigFile (InetAddress ia, List<String> triedHosts) {
        if (ia instanceof Inet6Address) return false;
        // check with the remembered hostname (preferred)
        String hostname = ia.getHostName ().toLowerCase ();
        triedHosts.add (hostname);
        if (checkSetConfig (hostname)) return true;
        // try part before first dot
        int dotIdx = hostname.indexOf (Util.DOT);
        if (dotIdx > -1) {
            hostname = hostname.substring (0, dotIdx);
            if (!triedHosts.contains (hostname)) {
                triedHosts.add (hostname);
                if (checkSetConfig (hostname)) return true;
            }
        }
        // then try IP number
        hostname = ia.getHostAddress ();
        if (!triedHosts.contains (hostname)) {
            triedHosts.add (hostname);
            if (checkSetConfig (hostname)) return true;
        }
        // otherwise try canonical hostname
        hostname = ia.getCanonicalHostName ().toLowerCase ();
        if (!triedHosts.contains (hostname)) {
            triedHosts.add (hostname);
            if (checkSetConfig (hostname)) return true;
        }
        // finally, try first part of canonical
        dotIdx = hostname.indexOf (Util.DOT);
        if (dotIdx > -1) {
            hostname = hostname.substring (0, dotIdx);
            if (!triedHosts.contains (hostname)) {
                triedHosts.add (hostname);
                if (checkSetConfig (hostname)) return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if there is a property file that for this hostname, and sets the config file property if there is.
     * It is checking whether rainbow-&lt;hostname&gt;.properties exists.
     * 
     * @param hostname
     *            the hostname to check
     * @return true if the file hostname specific properties file exists
     */
    private boolean checkSetConfig (String hostname) {
        boolean good = false;
        String cfgFileName = RainbowConstants.CONFIG_FILE_TEMPLATE.replace (CONFIG_FILE_STUB_NAME, hostname);
        if (Util.getRelativeToPath (m_targetPath, cfgFileName).exists ()) {
            m_props.setProperty (RainbowConstants.PROPKEY_CONFIG_FILE, cfgFileName);
            good = true;
        }
        return good;
    }

    public ThreadGroup getThreadGroup () {
        return m_threadGroup;
    }

    public File getTargetPath () {
        return m_targetPath;
    }

    void setIsMaster (boolean b) {
        instance ().m_isMaster = b;
    }

    public void setMaster (RainbowMaster rainbowMaster) {
        m_rainbowMaster = rainbowMaster;
    }

    public RainbowMaster getRainbowMaster () {
        return m_rainbowMaster;
    }

    public static void registerGauge (IGauge gauge) {
        instance ();
        Rainbow.m_id2Gauge.put (gauge.id (), gauge);
    }

    public static IGauge lookupGauge (String id) {
        instance ();
        return Rainbow.m_id2Gauge.get (id);
    }

    public static Environment environment () {
        instance ();
        if (Rainbow.m_env == Environment.UNKNOWN) {
            Rainbow.m_env = Environment.valueOf (getProperty (PROPKEY_DEPLOYMENT_ENVIRONMENT).toUpperCase ());
        }
        return Rainbow.m_env;
    }





}
