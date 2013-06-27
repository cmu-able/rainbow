package org.sa.rainbow.management.ports.local;

import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.management.ports.AbstractMasterConnectionPort;

final class LocalMasterConnectionPort extends AbstractMasterConnectionPort {

    LocalMasterConnectionPort (RainbowMaster rainbowMaster) {
        super (rainbowMaster);
    }

    @Override
    public void dispose () {
    }

}