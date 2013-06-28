package org.sa.rainbow.management.ports.local;

import java.util.Properties;

import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.management.ports.IRainbowManagementPort;

public class LocalMasterSideManagementPort implements IRainbowManagementPort {

    private String m_delegateID;
    private RainbowMaster m_master;
    private IRainbowManagementPort m_connectedPort;

    public LocalMasterSideManagementPort (RainbowMaster rainbowMaster, String delegateID) {
        m_delegateID = delegateID;
        m_master = rainbowMaster;
    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }


    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_connectedPort.sendConfigurationInformation (configuration);
    }

    @Override
    public void heartbeat () {
        m_master.processHeartbeat (m_delegateID);
    }

    @Override
    public void requestConfigurationInformation () {
        m_master.requestDelegateConfiguration (m_delegateID);
    }

    public void connect (LocalDelegateManagementPort port) {
        m_connectedPort = port;
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        return m_connectedPort.startDelegate ();
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        return m_connectedPort.pauseDelegate ();
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        return m_connectedPort.terminateDelegate ();
    }

    @Override
    public void dispose () {
    }

}
