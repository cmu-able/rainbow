package org.sa.rainbow.ports;

import org.sa.rainbow.RainbowDelegate;

/**
 * This interface represents a port through which delegates connect to the master
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IRainbowMasterConnectionPort {
    public IRainbowDeploymentPort connectDelegate (RainbowDelegate delegate, String delegateID);

    public void disconnectDelegate (RainbowDelegate delegate);
}
