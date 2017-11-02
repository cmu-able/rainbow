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

import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.globals.Environment;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.util.Util;

/**
 * A singleton class that provides utilities for reading properties, and getting access to important Rainbow Framework
 * services.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class Rainbow implements IRainbowEnvironment {


    private static final String PROPKEY_PROPFILENAME = "rainbow.properties";

    /**
     * Exit status that Rainbow would report when it exits, default to sleeping.
     */
    private static ExitState m_exitState = ExitState.SLEEP;

    private static Map<String, IGauge> m_id2Gauge;

    private static Environment m_env = Environment.UNKNOWN;

    private boolean m_shouldTerminate = false;

    private static Rainbow _instance = null;

    public static synchronized Rainbow instance () {
        if (_instance == null) {
            _instance = new Rainbow ();
        }
        return _instance;
    }

    /**
     * Returns whether the Rainbow runtime infrastructure should terminate.
     *
     * @return <code>true</code> if Rainbow should terminate, <code>false</code> otherwise.
     */
    @Override
    public boolean shouldTerminate () {
        return m_shouldTerminate;
    }

    /**
     * Sets the shouldTerminate flag so that the Rainbow Runtime Infrastructure parts know to terminate. This method is
     * intended primarily for {@link org.sa.rainbow.core.RainbowMaster <code>RainbowMaster</code>}, but may be used by
     * the UpdateService to signal termination.
     */
    @Override
    public void signalTerminate () {
        if (m_shouldTerminate) { // log once the signalling to
            // terminate
            LOGGER.info ("*** Signalling Terminate ***");
        }
        m_shouldTerminate = true;
    }

    @Override
    public void signalTerminate (ExitState exitState) {
        setExitState (exitState);
        signalTerminate ();
    }

    /**
     * Returns the exit value given the Rainbow exit state; this is stored statically since it would only be used once.
     *
     * @return int the exit value to return on System exit.
     */
    @Override
    public int exitValue () {
        return m_exitState.exitValue ();
    }

    @Override
    public void setExitState (ExitState state) {
        m_exitState = state;
    }

    @Override
    public boolean isMaster () {
        return m_rainbowMaster != null;
    }


    private final Properties m_props;
    private       File       m_basePath;

    private File m_targetPath;


    private final ThreadGroup m_threadGroup;

    private RainbowMaster m_rainbowMaster;

    private Properties m_defaultProps;

    private Rainbow () {
        m_props = new Properties ();
        m_id2Gauge = new HashMap<> ();
        m_threadGroup = new ThreadGroup (NAME);
        establishPaths ();
        loadConfigFiles ();
        canonicalizeHost2IPs ();
        evalPropertySubstitution ();
    }

    @Override
    public String getProperty (String key, String defaultProperty) {
        return m_props.getProperty (key, m_defaultProps.getProperty (key, defaultProperty));
    }

    @Override
    public String getProperty (String key) {
        return m_props.getProperty (key, m_defaultProps.getProperty (key));
    }

    @Override
    public boolean getProperty (String key, boolean b) {
        String value = m_props.getProperty (key);
        if (value != null)
            return Boolean.valueOf (value);
        else
            return b;
    }

    @Override
    public long getProperty (String key, long default_) {
        String value = m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Long.parseLong (value);
        } catch (NumberFormatException e) {
            return default_;
        }
    }

    @Override
    public short getProperty (String key, short default_) {
        String value = m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Short.parseShort (value);
        } catch (NumberFormatException e) {
            return default_;
        }
    }

    @Override
    public int getProperty (String key, int default_) {
        String value = m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Integer.parseInt (value);
        } catch (NumberFormatException e) {
            return default_;
        }
    }

    @Override
    public double getProperty (String key, double default_) {
        String value = m_props.getProperty (key);
        if (value == null) return default_;
        try {
            return Double.parseDouble (value);
        } catch (NumberFormatException e) {
            return default_;
        }
    }

    @Override
    public void setProperty (String key, short val) {
        m_props.setProperty (key, Short.toString (val));
    }

    @Override
    public void setProperty (String key, long val) {
        m_props.setProperty (key, Long.toString (val));
    }

    @Override
    public void setProperty (String key, boolean val) {
        m_props.setProperty (key, Boolean.toString (val));
    }

    @Override
    public void setProperty (String key, String val) {
        m_props.setProperty (key, val);
    }

    @Override
    public void setProperty (String key, double val) {
        m_props.setProperty (key, Double.toString (val));
    }

    @Override
    public void setProperty (String key, int val) {
        m_props.setProperty (key, Integer.toString (val));
    }


    @Override
    public Properties allProperties () {
        return m_props;
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
        } catch (IOException e) {
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
        } catch (IOException e1) {
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

        // Load the properties in each cfgFile into the m_props of this method. This constitutes the properties of
        // Rainbow
        // for this host.
        for (String cfg : cfgFiles) {
            try (FileInputStream pfIn = new FileInputStream (Util.getRelativeToPath (m_targetPath, cfg))) {
                m_props.load (pfIn);
            } catch (IOException e) {
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
        List<String> triedHosts = new ArrayList<> ();

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
        LOGGER.info (MessageFormat.format (
                "Unable to find host-specific property file! Will use general file insted. Tried: {0}",
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


    @Override
    public ThreadGroup getThreadGroup () {
        return m_threadGroup;
    }


    @Override
    public File getTargetPath () {
        return m_targetPath;
    }

    @Override
    public void setMaster (RainbowMaster rainbowMaster) {
        m_rainbowMaster = rainbowMaster;
    }

    @Override
    public RainbowMaster getRainbowMaster () {
        return m_rainbowMaster;
    }

    @Override
    public void registerGauge (IGauge gauge) {
        ;
        Rainbow.m_id2Gauge.put (gauge.id (), gauge);
    }

    @Override
    public IGauge lookupGauge (String id) {
        ;
        return Rainbow.m_id2Gauge.get (id);
    }

    @Override
    public Environment environment () {
        ;
        if (Rainbow.m_env == Environment.UNKNOWN) {
            Rainbow.m_env = Environment.valueOf (getProperty (PROPKEY_DEPLOYMENT_ENVIRONMENT).toUpperCase ());
        }
        return Rainbow.m_env;
    }





}
