package org.sa.rainbow.management.ports.local;

import java.util.Properties;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.management.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.management.ports.IRainbowManagementPort;

public class LocalDelegateConnectionPort extends AbstractDelegateConnectionPort {

    private LocalMasterConnectionPort m_masterPort;
    private LocalRainbowManagementPortFactory m_factory;

    public LocalDelegateConnectionPort (RainbowDelegate delegate, LocalRainbowManagementPortFactory factory) {
        super (delegate);
        m_factory = factory;
    }

    @Override
    public IRainbowManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        if (m_masterPort != null) {
            IRainbowManagementPort ddp = m_factory.createDelegateSideManagementPort (m_delegate, m_delegate.getId ());
            m_masterPort.connectDelegate (delegateID, connectionProperties);
            return ddp;
        }
        else
            throw new IllegalStateException ("The delegate port has not been connected to a master!");
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        if (m_masterPort != null) {
            m_masterPort.disconnectDelegate (delegateId);
        }
    }

    void connect (LocalMasterConnectionPort masterPort) {
        m_masterPort = masterPort;
    }

    RainbowDelegate getDelegate () {
        return m_delegate;
    }

    @Override
    public void dispose () {
    }

}
