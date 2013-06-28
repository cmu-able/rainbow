package org.sa.rainbow.management.ports;

import java.util.Properties;

import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;

public interface IRainbowManagementPortFactory {

    /**
     * Called by a delegate to get the master connection port
     * @param delegate TODO
     * 
     * @return
     */
    public abstract IRainbowMasterConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate)
            throws RainbowConnectionException;

    /**
     * Create the connection port on the master, which processes connection requests from delegates
     * 
     * @param rainbowMaster
     *            The master that has this port
     * @return
     */
    public abstract IRainbowMasterConnectionPort createMasterSideConnectionPort (final RainbowMaster rainbowMaster)
            throws RainbowConnectionException;

    /**
     * Create a delegate port of the delegate that forwards requests to the master
     * 
     * @param delegate
     *            The delegate that is connected to this port
     * @param delegateID
     *            The delegate id of the delegate
     * @return the port associated with deployment and lifecycle information to the delegate
     */
    public abstract IRainbowManagementPort
            createDelegateSideManagementPort (RainbowDelegate delegate, String delegateID)
                    throws RainbowConnectionException;

    /**
     * Create a delegate port of the rainbowMaster that will forward requests to the delegate indicated by delegateID
     * 
     * @param rainbowMaster
     *            The Rainbow Master component of this port
     * @param delegateID
     *            The ID of the delegate to connect the master to
     * @return a new port to be used by the master to communicate deployment and configuration information to the
     *         delegate, and manager the lifecycle
     */
    public abstract IRainbowManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) throws RainbowConnectionException;

}
