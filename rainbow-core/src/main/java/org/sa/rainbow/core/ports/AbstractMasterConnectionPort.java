package org.sa.rainbow.core.ports;

import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;

public abstract class AbstractMasterConnectionPort implements IMasterConnectionPort {

    final protected RainbowMaster m_master;

    public AbstractMasterConnectionPort (RainbowMaster master) {
        m_master = master;
    }

    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        IDelegateManagementPort port = m_master.connectDelegate (delegateID, connectionProperties);
        return port;
    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        m_master.report (delegateID, type, compT, msg);
    }



}
