package org.sa.rainbow;

import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.management.ports.IRainbowDeploymentPort;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.management.ports.RainbowDeploymentPortFactory;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

public class RainbowDelegate extends AbstractRainbowRunnable implements RainbowConstants {

    static Logger           LOGGER = Logger.getLogger (RainbowDelegate.class);

    protected static String NAME = "Rainbow Delegate";

    final IRainbowDeploymentPort m_masterPort;
    private String          m_id;
    private String          m_name = null;

    private Beacon          m_beacon;

    private IRainbowMasterConnectionPort m_masterConnectionPort;

    private Properties                   m_configurationInformation;

    public RainbowDelegate () {
        super (NAME);
        // Generate an ID 
        m_id = UUID.randomUUID ().toString ();

        // Create the connection to the delegate
        m_masterConnectionPort = RainbowDeploymentPortFactory
                .createDelegateMasterConnectionPort (this);
        log ("Attempting to connecto to master.");
        m_masterPort = m_masterConnectionPort.connectDelegate (m_id, new Properties ());
        // Request configuration information
        m_masterPort.requestConfigurationInformation ();
    }

    /**
     * Called when configuration information is received from the master.
     * 
     * @param props
     *            The configuration information, as a set of properties
     */
    public void receiveConfigurationInformation (Properties props) {
        m_configurationInformation = props;
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
    }

    @Override
    public void dispose () {

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
            m_masterPort.receiveHeartbeat ();
            m_beacon.mark ();
        }
    }

    /** 
     * 
     */
    @Override
    protected void doTerminate () {
        log ("Terminating.");
        m_masterConnectionPort.disconnectDelegate (getId ());
        m_masterPort.dispose ();
        m_masterConnectionPort.dispose ();
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

    public static void main (String[] args) {
        new RainbowDelegate ();
    }
}
