package org.sa.rainbow;

import java.text.MessageFormat;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.ports.IRainbowDeploymentPort;
import org.sa.rainbow.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.ports.RainbowDeploymentPortFactory;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

public class RainbowDelegate extends AbstractRainbowRunnable implements RainbowConstants {

    static Logger           LOGGER = Logger.getLogger (RainbowDelegate.class);

    protected static String NAME = "Rainbow Delegate";

    final IRainbowDeploymentPort m_masterPort;
    private String          m_id;
    private String          m_name = null;

    private Beacon          m_beacon;

    public RainbowDelegate () {
        super (NAME);
        m_id = UUID.randomUUID ().toString ();
        IRainbowMasterConnectionPort masterConnectionPort = RainbowDeploymentPortFactory
                .getDelegateMasterConnectionPort ();
        m_masterPort = masterConnectionPort.connectDelegate (this, m_id);
//        m_masterPort = LocalRainbowDelegatePortFactory.createDelegateDelegatePort (this, m_id);
//        m_masterPort.requestConfigurationInformation ();
    }

    public void receiveConfigurationInformation (Properties props) {
        long period = Long.parseLong (props.getProperty (PROPKEY_DELEGATE_BEACONPERIOD, "10000"));
        if (m_beacon != null) {
            if (m_beacon.period () != period) {
                m_beacon.setPeriod (period);
            }
        }
        else {
            m_beacon = new Beacon (period);

        }
        m_beacon.mark ();
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
            m_masterPort.receiveHeartbeat ();
            m_beacon.mark ();
        }
    }

    @Override
    protected void doTerminate () {
        IRainbowMasterConnectionPort masterConnectionPort = RainbowDeploymentPortFactory
                .getDelegateMasterConnectionPort ();
        masterConnectionPort.disconnectDelegate (this);
        super.doTerminate ();
    }

    public String getId () {
        return m_id;
    }

    public static void main (String[] args) {
        new RainbowDelegate ();
    }
}
