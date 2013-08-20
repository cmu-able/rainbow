package org.sa.rainbow.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.management.ports.IRainbowManagementPort;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.management.ports.RainbowPortFactory;
import org.sa.rainbow.models.ModelsManager;
import org.sa.rainbow.util.Beacon;

public class RainbowMaster extends AbstractRainbowRunnable {
    static Logger                       LOGGER       = Logger.getLogger (Rainbow.class.getCanonicalName ());

    Map<String, IRainbowManagementPort> m_delegates  = new HashMap<> ();

    IRainbowMasterConnectionPort        m_delegateConnection;

    private Map<String, Beacon>         m_heartbeats = new HashMap<> ();

    private ModelsManager               m_modelsManager;

    public RainbowMaster () throws RainbowConnectionException {
        super ("Rainbow Master");
        Rainbow.instance ().setIsMaster (true);
        Rainbow.instance ().setMaster (this);
    }

    public void initialize () throws RainbowException {
        initializeConnections ();
        initializeRainbowComponents ();
    }

    private void initializeRainbowComponents () throws RainbowException {
        m_modelsManager = new ModelsManager ();
        try {
            m_modelsManager.initialize ();
        }
        catch (IOException e) {
            throw new RainbowException ("Could not instantiate Models Manager", e);
        }
    }

    private IRainbowMasterConnectionPort initializeConnections () throws RainbowConnectionException {
        return m_delegateConnection = RainbowPortFactory.createDelegateConnectionPort (this);
    }

    public ModelsManager modelsManager () {
        return m_modelsManager;
    }

    /**
     * Connects a new delegate and sends the appropriate configuration information to the delegate
     * 
     * @param delegateID
     * @param connectionProperties
     * @param delegateIP
     *            Drop the ip address
     */
    public IRainbowManagementPort connectDelegate (String delegateID, Properties connectionProperties) {
        LOGGER.debug (MessageFormat.format ("Master received connection request from: {0} at {1}", delegateID,
                connectionProperties.getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "Unknown Location")));
        try {
            IRainbowManagementPort delegatePort = RainbowPortFactory.createMasterDeploymentPort (this,
                    delegateID, connectionProperties);
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
        catch (NumberFormatException | RainbowConnectionException e) {
            LOGGER.error (MessageFormat.format (
                    "Rainbow master could not create the management interface to the delegate {0}", delegateID));
            m_delegateConnection.disconnectDelegate (delegateID);
        }
        return null;
    }


    /**
     * Called by a delegate port to request information be sent to it
     * 
     * @param delegateID
     */
    public void requestDelegateConfiguration (String delegateID) {
        IRainbowManagementPort delegate = m_delegates.get (delegateID);
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
        IRainbowManagementPort delegate = m_delegates.get (delegateID);
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
//                entry.getValue ().mark ();
            }
        }
    }

    public void disconnectDelegate (String id) {
        LOGGER.info (MessageFormat.format ("RM: Disconnecting delegate: {0}", id));
        m_heartbeats.remove (id);
        IRainbowManagementPort deploymentPort = m_delegates.remove (id);
        deploymentPort.dispose ();
    }

    @Override
    public void terminate () {
        for (Entry<String, IRainbowManagementPort> entry : m_delegates.entrySet ()) {
            disconnectDelegate (entry.getKey ());
            entry.getValue ().terminateDelegate ();
        }
        m_delegateConnection.dispose ();
//        try {
//            Thread.sleep (4000);
//        }
//        catch (InterruptedException e) {
//        }
        super.terminate ();
        while (!isTerminated ()) {
            try {
                Thread.sleep (500);
            }
            catch (InterruptedException e) {
            }
        }
    }

    // Methods below this point are used for testing purposes, and so are package protected.
    Map<? extends String, ? extends Beacon> getHeartbeatInfo () {
        return m_heartbeats;
    }

}
