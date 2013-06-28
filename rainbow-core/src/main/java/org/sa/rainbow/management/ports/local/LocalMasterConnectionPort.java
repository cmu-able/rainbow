package org.sa.rainbow.management.ports.local;

import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.management.ports.AbstractMasterConnectionPort;

final class LocalMasterConnectionPort extends AbstractMasterConnectionPort {

    private LocalDelegateConnectionPort m_connectedPort;

    LocalMasterConnectionPort (RainbowMaster rainbowMaster) {
        super (rainbowMaster);
    }

    @Override
    public void dispose () {
    }

    public void connect (LocalDelegateConnectionPort port) {
        m_connectedPort = port;
    }

    @Override
    public void disconnectDelegate (String delegateId) {
//        m_connectedPort.disconnectDelegate (delegateId);
    }

}