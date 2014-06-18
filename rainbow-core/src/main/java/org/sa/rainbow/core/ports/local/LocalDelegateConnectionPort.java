package org.sa.rainbow.core.ports.local;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;

public class LocalDelegateConnectionPort extends AbstractDelegateConnectionPort {

    private LocalMasterConnectionPort m_masterPort;
    private LocalRainbowPortFactory m_factory;

    public LocalDelegateConnectionPort (RainbowDelegate delegate, LocalRainbowPortFactory factory) throws IOException {
        super (delegate, null, (short )-1, null);
        m_factory = factory;
    }

    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        if (m_masterPort != null) {
            IDelegateManagementPort ddp = m_factory.createDelegateSideManagementPort (m_delegate, m_delegate.getId ());
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

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        if (m_masterPort != null) {
            m_masterPort.report (delegateID, type, compT, msg);
        }
    }

    @Override
    public void trace (RainbowComponentT type, String msg) {

    }

}
