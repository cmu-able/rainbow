package org.sa.rainbow.core.management.ports.local;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.management.ports.IRainbowManagementPort;

public class LocalDelegateManagementPort implements IRainbowManagementPort {

    static Logger           LOGGER = Logger.getLogger (LocalDelegateManagementPort.class);

    private RainbowDelegate m_delegate;
    private String          m_delegateID;
    LocalMasterSideManagementPort m_connectedTo;

    public LocalDelegateManagementPort (RainbowDelegate delegate, String delegateID) {
        m_delegate = delegate;
        m_delegateID = delegateID;

    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    @Override
    public void heartbeat () {
        checkConnected ();
        m_connectedTo.heartbeat ();
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

    public void connect (LocalMasterSideManagementPort port) {
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

    @Override
    public void dispose () {
    }

}
