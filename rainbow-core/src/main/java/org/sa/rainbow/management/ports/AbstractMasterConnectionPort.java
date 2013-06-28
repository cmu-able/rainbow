package org.sa.rainbow.management.ports;

import java.util.Properties;

import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;

public abstract class AbstractMasterConnectionPort implements IRainbowMasterConnectionPort {

    final protected RainbowMaster m_master;

    public AbstractMasterConnectionPort (RainbowMaster master) {
        m_master = master;
    }

    @Override
    public IRainbowManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        IRainbowManagementPort port = m_master.connectDelegate (delegateID, connectionProperties);
        return port;
    }



}
