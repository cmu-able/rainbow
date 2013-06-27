package org.sa.rainbow.management.ports;

import org.sa.rainbow.RainbowMaster;

/**
 * * This class represents the common methods for master deployment port. These methods correspond to those that are
 * sent to the master from a delegate.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractMasterDeploymentPort implements IRainbowDeploymentPort {

    private RainbowMaster m_master;
    private String        m_delegateID;

    /**
     * Construct a deployment port for the Rainbow Master for processing communication with the identified delegate
     * 
     * @param master
     *            The master that will contain this port
     * @param delegateID
     *            The id of the delegate that is being communicated with
     */
    protected AbstractMasterDeploymentPort (RainbowMaster master, String delegateID) {
        m_master = master;
        m_delegateID = delegateID;
    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void receiveHeartbeat () {
        m_master.processHeartbeat (m_delegateID);
    }

    @Override
    public void requestConfigurationInformation () {
        m_master.requestDelegateConfiguration (m_delegateID);
    }

}
