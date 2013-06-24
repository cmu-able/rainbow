package org.sa.rainbow.ports.local;

import java.util.Properties;

import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.ports.IRainbowDeploymentPort;

public class LocalDelegateConnectionPort extends AbstractDelegateConnectionPort {

    private LocalMasterConnectionPort m_masterPort;
    private LocalRainbowDeploymentPortFactory m_factory;

    public LocalDelegateConnectionPort (RainbowDelegate delegate, LocalRainbowDeploymentPortFactory factory) {
        super (delegate);
        m_factory = factory;
    }

    @Override
    public IRainbowDeploymentPort connectDelegate (String delegateID, Properties connectionProperties) {
        if (m_masterPort != null) {
            IRainbowDeploymentPort ddp = m_factory.createDelegateDeploymentPortPort (m_delegate, m_delegate.getId ());
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
