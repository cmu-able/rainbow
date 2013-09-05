package org.sa.rainbow.core;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IRainbowDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IRainbowManagementPort;
import org.sa.rainbow.core.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.translator.probes.IProbe;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

public class RainbowDelegate extends AbstractRainbowRunnable implements RainbowConstants {

    static Logger LOGGER = Logger.getLogger (RainbowDelegate.class);

    static enum ConnectionState {
        UNKNOWN, CONNECTING, CONNECTED, CONFIGURED
    };

    protected static String                   NAME            = "Rainbow Delegate";

    private IRainbowManagementPort            m_masterPort;
    private String                            m_id;
    private String                            m_name          = null;

    private Beacon                            m_beacon;

    private IRainbowMasterConnectionPort      m_masterConnectionPort;
    private IRainbowDelegateConfigurationPort m_configurationPort;

    private Properties                        m_configurationInformation;
    private ConnectionState                   m_delegateState = ConnectionState.UNKNOWN;

    private ProbeDescription                  m_localProbeDesc;

    private EffectorDescription               m_localEffectorDesc;

    public RainbowDelegate () {
        super (NAME);
        // Generate an ID 
        m_id = UUID.randomUUID ().toString ();

    }

    public void initialize () throws RainbowConnectionException {
        // Create the connection to the master
        m_masterConnectionPort = RainbowPortFactory.createDelegateMasterConnectionPort (this);
        log ("Attempting to connecto to master.");
        m_delegateState = ConnectionState.CONNECTING;
        m_masterPort = m_masterConnectionPort.connectDelegate (m_id, getConnectionProperties ());
        m_delegateState = ConnectionState.CONNECTED;
        // Request configuration information

        m_configurationPort = RainbowPortFactory.createDelegateConfigurationPort (this);

        m_masterPort.requestConfigurationInformation ();
    }

    private Properties getConnectionProperties () {
        Properties props = new Properties ();
        props.setProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, Rainbow.canonicalizeHost2IP ("localhost"));
        return props;
    }

    /**
     * Called when configuration information is received from the master.
     * 
     * @param props
     *            The configuration information, as a set of properties
     */
    public synchronized void receiveConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors) {
        m_configurationInformation = props;
        initProbes (probes);

        m_localEffectorDesc = new EffectorDescription ();
        m_localEffectorDesc.effectors = new TreeSet<> (effectors);

        log ("Received configuration information.");

        // Process the period for sending the heartbeat
        long period = Long.parseLong (props.getProperty (PROPKEY_DELEGATE_BEACONPERIOD, "10000"));
        if (m_beacon != null) {
            // Reset beacon period, i it exists
            if (m_beacon.period () != period) {
                m_beacon.setPeriod (period);
            }
        }
        else {
            m_beacon = new Beacon (period);

        }
        m_beacon.mark ();
        // If the Master has a name of this delegate, use it as the name in logging and such
        String id = props.getProperty (PROPKEY_DELEGATE_ID);
        if (id != null) {
            m_name = id;
        }
        m_delegateState = ConnectionState.CONFIGURED;
    }



    @Override
    public void dispose () {
        m_masterPort.dispose ();
        m_masterConnectionPort.dispose ();
    }

    @Override
    protected void log (String txt) {
        LOGGER.info (MessageFormat.format ("{2}[{0}]: {1}", Util.timelog (), txt,
                m_name == null ? MessageFormat.format ("RD-{0}", m_id) : MessageFormat.format ("{0}-{1}", m_name, m_id)));
    }

    @Override
    protected void runAction () {
        manageHeartbeat ();
    }

    private void manageHeartbeat () {
        if (m_beacon != null && m_beacon.periodElapsed ()) {
            log ("Sending heartbeat.");
            m_masterPort.heartbeat ();
            m_beacon.mark ();
        }
    }

    /** 
     * 
     */
    @Override
    protected void doTerminate () {
        log ("Terminating.");
        m_beacon = null;
        m_masterConnectionPort.disconnectDelegate (getId ());

        super.doTerminate ();
    }

    @Override
    public void start () {
        log ("Starting.");
        super.start ();
    }

    @Override
    public void stop () {
        log ("Pausing.");
        super.stop ();
    }

    public String getId () {
        return m_id;
    }

    public Properties getConfigurationInformation () {
        return m_configurationInformation;
    }

    public static void main (String[] args) throws RainbowConnectionException {
        new RainbowDelegate ();
    }

    public void disconnectFromMaster () {
        terminate ();
    }

    @Override
    public void terminate () {
        super.terminate ();
        while (!isTerminated ()) {
            try {
                Thread.sleep (500);
            }
            catch (InterruptedException e) {
            }
        }
    }

    private void initProbes (List<ProbeAttributes> probes) {
        m_localProbeDesc = new ProbeDescription ();
        m_localProbeDesc.probes = new TreeSet<> (probes);
        // obtain the list of probes to create
        for (ProbeDescription.ProbeAttributes pbAttr : m_localProbeDesc.probes) {
            // see if probe is for my location
            if (!pbAttr.location.equals (Rainbow.getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION))) {
                continue;
            }
            try {
                IProbe probe = null;
                // prepare class info
                String probeClazz = null;
                Class<?>[] params = null;
                Object[] args = null;
                // collect argument values
                String refID = Util.genID (pbAttr.name, pbAttr.location);
                switch (pbAttr.kind) {
                case SCRIPT:
                    // get info for a script-based probe
                    String path = pbAttr.info.get ("path");
                    String argument = pbAttr.info.get ("argument");
                    if (!new File (path).exists ()) {
                        reportError (MessageFormat
                                .format ("Could not create probe {0}. The script \"{1}\" does not exist.",
                                        pbAttr.location, path), null);
                        continue; // don't create
                    }
                    probe = new GenericScriptBasedProbe (refID, pbAttr.alias, path, argument);
                    LOGGER.trace ("Script-based IProbe " + pbAttr.name + ": " + path + " " + argument);
                    break;
                case JAVA:
                    probeClazz = pbAttr.info.get ("class");
                    // get argument info for Java probe, including ID and possibly sleep period
                    List<Class<?>> paramList = new ArrayList<Class<?>> ();
                    List<Object> argsList = new ArrayList<Object> ();
                    paramList.add (String.class);
                    argsList.add (refID);
                    String periodStr = pbAttr.info.get ("period");
                    if (periodStr != null) { // assume long
                        paramList.add (long.class);
                        argsList.add (Long.parseLong (periodStr));
                    }
                    if (pbAttr.arrays.size () > 0) {
                        // get list of arguments for a pure Java probe
                        for (Object vObj : pbAttr.arrays.values ()) {
                            paramList.add (vObj.getClass ());
                            argsList.add (vObj);
                        }
                    }
                    params = paramList.toArray (new Class<?>[0]);
                    args = argsList.toArray ();
                    break;
                }
                if (probeClazz != null) {
                    Throwable et = null;
                    try {
                        Class<?> probeClass = Class.forName (probeClazz);
                        Constructor<?> cons = probeClass.getConstructor (params);
                        probe = (IProbe )cons.newInstance (args);
                    }
                    catch (ClassNotFoundException e) {
                        et = e;
                    }
                    catch (InstantiationException e) {
                        et = e;
                    }
                    catch (IllegalAccessException e) {
                        et = e;
                    }
                    catch (SecurityException e) {
                        et = e;
                    }
                    catch (NoSuchMethodException e) {
                        et = e;
                    }
                    catch (IllegalArgumentException e) {
                        et = e;
                    }
                    catch (InvocationTargetException e) {
                        et = e;
                    }
                    catch (Throwable t) {
                        et = t;
                    }
                    finally {
                        if (et != null) {
                            String msg = MessageFormat.format ("Could not instantiate probe: {0}", probeClazz);
                            reportError (msg, et);
                        }
                    }
                }

                if (probe != null) {
                    probe.create ();
                    // TODO: Report probe created on the probe bus
                }
            }
            catch (Exception e) {
                String msg = MessageFormat.format ("Could not instantiate probe: {0}", pbAttr.name);
                reportError (msg, e);
            }
        }
    }

    private void reportError (String msg, Throwable e) {
        if (e != null) {
            LOGGER.error (msg, e);
        }
        else {
            LOGGER.error (msg);
        }
    }

    // Methods after this are used for testing
    synchronized ConnectionState getConnectionState () {
        return m_delegateState;
    }

    synchronized Set<ProbeAttributes> getProbeConfiguration () {
        return m_localProbeDesc.probes;
    }

    synchronized Set<EffectorAttributes> getEffectorConfiguration () {
        return m_localEffectorDesc.effectors;
    }
}
