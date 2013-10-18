package org.sa.rainbow.core.ports;

import java.util.Collections;
import java.util.Properties;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

/**
 * This class represents the common methods for delegate deployment ports. These methods correspond to those that are
 * sent to the delegate from the master.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractDelegateManagementPort implements IDelegateManagementPort {

    private RainbowDelegate m_delegate;

    /**
     * Create a new DeploymentPort for the delegate
     * 
     * @param delegate
     *            The delegate for this port
     */
    public AbstractDelegateManagementPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }

    @Override
    public String getDelegateId () {
        return m_delegate.getId ();
    }

    @Override
    //TODO: Delete this interfaces
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration, Collections.<ProbeAttributes> emptyList (),
                Collections.<EffectorAttributes> emptyList (), Collections.<GaugeInstanceDescription> emptyList ());

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

    @Override
    public void startProbes () throws IllegalStateException {
        m_delegate.startProbes ();
    }

    @Override
    public void killProbes () throws IllegalStateException {
        m_delegate.killProbes ();
    }

}
