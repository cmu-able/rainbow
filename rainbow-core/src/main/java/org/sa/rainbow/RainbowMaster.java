package org.sa.rainbow;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.management.ports.IRainbowDeploymentPort;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.management.ports.RainbowDeploymentPortFactory;
import org.sa.rainbow.util.Beacon;

public class RainbowMaster extends AbstractRainbowRunnable {
    static Logger                       LOGGER       = Logger.getLogger (Rainbow.class.getCanonicalName ());

    Map<String, IRainbowDeploymentPort> m_delegates  = new HashMap<> ();

    IRainbowMasterConnectionPort        m_delegateConnection;

    private Map<String, Beacon>         m_heartbeats = new HashMap<> ();

    public RainbowMaster () {
        super ("Rainbow Master");
        m_delegateConnection = RainbowDeploymentPortFactory.createDelegateConnectionPort (this);
    }

    /**
     * Connects a new delegate and sends the appropriate configuration information to the delegate
     * 
     * @param delegateID
     * @param connectionProperties
     * @param delegateIP
     *            Drop the ip address
     */
    public IRainbowDeploymentPort connectDelegate (String delegateID, Properties connectionProperties) {
        LOGGER.debug (MessageFormat.format ("Master received connection request from: {0}", delegateID));
        IRainbowDeploymentPort delegatePort = RainbowDeploymentPortFactory.createMasterDeploymentPort (this, delegateID,
                connectionProperties);
        // Check to see if there is already a registered delegate running on the machine
        m_delegates.put (delegateID, delegatePort);
        // Add a second to the heartbeat to allow for communication time
        // TODO: Must be a better way to do this...
        Beacon beacon = new Beacon (Long.parseLong (Rainbow.properties ().getProperty (
                RainbowConstants.PROPKEY_DELEGATE_BEACONPERIOD, "1000")) + 1000);
        m_heartbeats.put (delegatePort.getDelegateId (), beacon);
        beacon.mark ();
        LOGGER.info (MessageFormat.format ("Master created management connection with delegate {0}", delegateID));
        return delegatePort;
    }

    /**
     * Called by a delegate port to request information be sent to it
     * 
     * @param delegateID
     */
    public void requestDelegateConfiguration (String delegateID) {
        IRainbowDeploymentPort delegate = m_delegates.get (delegateID);
        if (delegate != null) {
            LOGGER.info (MessageFormat.format ("Sending configuration information to {0}.", delegateID));
            delegate.sendConfigurationInformation (filterPropertiesForDelegate (delegateID));
        }
        else {
            LOGGER.error (MessageFormat
                    .format ("Received configuration request from unknown delegate {0}.", delegateID));
        }
    }

    /**
     * Called when a delegate sends a heartbeat message
     * 
     * @param delegateID
     *            The IP of the delegate
     */
    public void processHeartbeat (String delegateID) {
        IRainbowDeploymentPort delegate = m_delegates.get (delegateID);
        if (delegate != null) {
            Beacon hb = m_heartbeats.get (delegate.getDelegateId ());
            if (hb == null) {
                LOGGER.error (MessageFormat.format ("Received heartbeat from unknown delegate at {0}.", delegateID));
            }
            else {
                LOGGER.debug (MessageFormat.format ("Received heartbeat from known delegate: {0}", delegateID));
                hb.mark ();
            }
        }
        else {
            LOGGER.error (MessageFormat.format ("Received heartbeat from unknown delegate at {0}.", delegateID));
        }
    }

    /**
     * Filters the properties to only report those properties that are relevant to the delegate
     * 
     * @param delegateID
     * @return
     */
    private Properties filterPropertiesForDelegate (String delegateID) {
        return Rainbow.properties ();
    }

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

    @Override
    protected void log (String txt) {
        LOGGER.info (MessageFormat.format ("RM: {0}", txt));
    }

    @Override
    protected void runAction () {
        checkHeartbeats ();
    }

    private void checkHeartbeats () {
        Set<Entry<String, Beacon>> entrySet = m_heartbeats.entrySet ();
        for (Entry<String, Beacon> entry : entrySet) {
            if (entry.getValue ().periodElapsed ()) {
                LOGGER.error (MessageFormat.format ("Delegate {0} has not given a heartbeat withing the right time",
                        entry.getKey ()));
                entry.getValue ().mark ();
            }
        }
    }

    public void disconnectDelegate (String id) {
        LOGGER.info (MessageFormat.format ("RM: Disconnecting delegate: {0}", id));
        m_heartbeats.remove (id);
        IRainbowDeploymentPort deploymentPort = m_delegates.remove (id);
        deploymentPort.dispose ();
    }

    @Override
    public void terminate () {
        for (Entry<String, IRainbowDeploymentPort> entry : m_delegates.entrySet ()) {
            entry.getValue ().terminateDelegate ();
        }
        m_delegateConnection.dispose ();
        try {
            Thread.sleep (4000);
        }
        catch (InterruptedException e) {
        }
        super.terminate ();
    }

}
