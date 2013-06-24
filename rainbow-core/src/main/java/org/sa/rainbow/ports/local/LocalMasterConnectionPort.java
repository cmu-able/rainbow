package org.sa.rainbow.ports.local;

import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.ports.AbstractMasterConnectionPort;

final class LocalMasterConnectionPort extends AbstractMasterConnectionPort {

    LocalMasterConnectionPort (RainbowMaster rainbowMaster) {
        super (rainbowMaster);
    }

    @Override
    public void dispose () {
    }

}