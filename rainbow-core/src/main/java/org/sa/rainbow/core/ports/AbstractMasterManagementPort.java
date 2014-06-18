package org.sa.rainbow.core.ports;

import java.io.IOException;

import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

/**
 * * This class represents the common methods for master deployment port. These methods correspond to those that are
 * sent to the master from a delegate.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractMasterManagementPort extends AbstractESEBDisposablePort implements IDelegateManagementPort {

    private RainbowMaster m_master;
    private String        m_delegateID;

    /**
     * Construct a deployment port for the Rainbow Master for processing communication with the identified delegate
     * 
     * @param master
     *            The master that will contain this port
     * @param delegateID
     *            The id of the delegate that is being communicated with
     * @param channel
     * @param port
     * @param host
     * @throws IOException
     */
    protected AbstractMasterManagementPort (RainbowMaster master, String delegateID, String host, Short port,
            ChannelT channel) throws IOException {
        super (host, port, channel);
        m_master = master;
        m_delegateID = delegateID;
    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void heartbeat () {
        m_master.processHeartbeat (m_delegateID);
    }

    @Override
    public void requestConfigurationInformation () {
        m_master.requestDelegateConfiguration (m_delegateID);
    }

}
