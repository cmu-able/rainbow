package org.sa.rainbow.core.management.ports;

import java.util.Properties;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeConfigurationInterface;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeQueryInterface;
import org.sa.rainbow.core.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ports.IRainbowModelChangeBusPort;
import org.sa.rainbow.core.models.ports.IRainbowModelUSBusPort;
import org.sa.rainbow.translator.probes.IProbe;
import org.sa.rainbow.translator.probes.ports.IProbeConfigurationPort;
import org.sa.rainbow.translator.probes.ports.IProbeReportPort;

public interface IRainbowConnectionPortFactory {

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

    public abstract IRainbowModelUSBusPort createModelsManagerUSPort (IModelsManager m)
            throws RainbowConnectionException;

    public abstract IRainbowModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException;

    public abstract IRainbowGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException;

    public abstract IRainbowModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException;

    public abstract IRainbowGaugeLifecycleBusPort
    createManagerGaugeLifecyclePort (IRainbowGaugeLifecycleBusPort manager) throws RainbowConnectionException;

    public abstract IGaugeConfigurationInterface createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;

    public abstract IGaugeQueryInterface createGaugeQueryPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;

    public abstract IGaugeConfigurationInterface createGaugeConfigurationPort (IGauge gauge)
            throws RainbowConnectionException;

    public abstract IGaugeQueryInterface createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException;

    public abstract IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException;

    public abstract IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe,
            IProbeConfigurationPort callback) throws RainbowConnectionException;

    public abstract IRainbowDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException;

    public abstract IRainbowDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException;

}
