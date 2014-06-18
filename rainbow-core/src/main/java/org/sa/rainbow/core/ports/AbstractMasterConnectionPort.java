package org.sa.rainbow.core.ports;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

public abstract class AbstractMasterConnectionPort extends AbstractESEBDisposablePort implements IMasterConnectionPort {

    final protected RainbowMaster m_master;

    public AbstractMasterConnectionPort (RainbowMaster master, short port, ChannelT channel) throws IOException {
        super (port, channel);
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
