package org.sa.rainbow.ports;

import org.sa.rainbow.RainbowDelegate;

public abstract class AbstractDelegateConnectionPort implements IRainbowMasterConnectionPort {

    protected RainbowDelegate m_delegate;

    public AbstractDelegateConnectionPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }



}
