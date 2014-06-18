package org.sa.rainbow.core.ports.local;

import java.io.IOException;

import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.ports.AbstractMasterConnectionPort;

final class LocalMasterConnectionPort extends AbstractMasterConnectionPort {

    private LocalDelegateConnectionPort m_connectedPort;

    LocalMasterConnectionPort (RainbowMaster rainbowMaster) throws IOException {
        super (rainbowMaster, (short )-1, null);
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