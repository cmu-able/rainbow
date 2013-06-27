package org.sa.rainbow.management.ports;

import java.util.Properties;

import org.sa.rainbow.RainbowMaster;

public abstract class AbstractMasterConnectionPort implements IRainbowMasterConnectionPort {

    final protected RainbowMaster m_master;

    public AbstractMasterConnectionPort (RainbowMaster master) {
        m_master = master;
    }

    @Override
    public IRainbowDeploymentPort connectDelegate (String delegateID, Properties connectionProperties) {
        IRainbowDeploymentPort port = m_master.connectDelegate (delegateID, connectionProperties);
        return port;
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        m_master.disconnectDelegate (delegateId);
    }

}
