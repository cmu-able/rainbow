package org.sa.rainbow.ports;

import java.util.Properties;

import org.sa.rainbow.RainbowDelegate;

/**
 * This class represents the common methods for delegate deployment ports. These methods correspond to those that are
 * sent to the delegate from the master.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractDelegateDeploymentPort implements IRainbowDeploymentPort {

    private RainbowDelegate m_delegate;

    /**
     * Create a new DeploymentPort for the delegate
     * 
     * @param delegate
     *            The delegate for this port
     */
    public AbstractDelegateDeploymentPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }

    @Override
    public String getDelegateId () {
        return m_delegate.getId ();
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration);

    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        m_delegate.start ();
        return true;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        m_delegate.stop ();
        return true;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        m_delegate.terminate ();
        return true;
    }

}
