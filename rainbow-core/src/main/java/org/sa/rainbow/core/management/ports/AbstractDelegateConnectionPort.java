package org.sa.rainbow.core.management.ports;

import org.sa.rainbow.core.RainbowDelegate;

public abstract class AbstractDelegateConnectionPort implements IRainbowMasterConnectionPort {

    protected RainbowDelegate m_delegate;

    public AbstractDelegateConnectionPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }



}
