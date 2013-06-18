package org.sa.rainbow.ports.local;

import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.ports.IRainbowDeploymentPort;

public class LocalDelegateDeploymentPort implements IRainbowDeploymentPort {

    static Logger           LOGGER = Logger.getLogger (LocalDelegateDeploymentPort.class);

    private RainbowDelegate m_delegate;
    private String          m_delegateID;
    LocalMasterDeploymentPort m_connectedTo;

    public LocalDelegateDeploymentPort (RainbowDelegate delegate, String delegateID) {
        m_delegate = delegate;
        m_delegateID = delegateID;

    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration);
    }

    @Override
    public void receiveHeartbeat () {
        checkConnected ();
        m_connectedTo.receiveHeartbeat ();
    }

    private void checkConnected () {
        if (m_connectedTo == null) {
            String errMsg = MessageFormat.format ("Local delegate port not connected to a master: {0}", m_delegateID);
            LOGGER.error (errMsg);
            throw new IllegalStateException (errMsg);
        }
    }

    @Override
    public void requestConfigurationInformation () {
        checkConnected ();
        m_connectedTo.requestConfigurationInformation ();
    }

    public void connect (LocalMasterDeploymentPort port) {
        m_connectedTo = port;
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        m_delegate.start ();
        return true;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        m_delegate.stop ();
        return true;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        m_delegate.terminate ();
        return true;
    }

}
