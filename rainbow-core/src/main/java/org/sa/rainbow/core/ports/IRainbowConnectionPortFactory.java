/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.core.ports;

import java.util.Properties;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

public interface IRainbowConnectionPortFactory {

    /**
     * Called by a delegate to get the master connection port
     * @param delegate TODO
     * 
     * @return
     */
    public abstract AbstractDelegateConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate)
            throws RainbowConnectionException;

    /**
     * Create the connection port on the master, which processes connection requests from delegates
     * 
     * @param rainbowMaster
     *            The master that has this port
     * @return
     */
    public abstract IMasterConnectionPort createMasterSideConnectionPort (final RainbowMaster rainbowMaster)
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
    public abstract IDelegateManagementPort
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
    public abstract IDelegateManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) throws RainbowConnectionException;

    public abstract IModelUSBusPort createModelsManagerUSPort (IModelsManager m)
            throws RainbowConnectionException;

    public abstract IModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException;

    public abstract IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException;

    public abstract IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException;

    public abstract IGaugeLifecycleBusPort
    createManagerGaugeLifecyclePort (IGaugeLifecycleBusPort manager) throws RainbowConnectionException;

    public abstract IGaugeConfigurationPort createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;

    public abstract IGaugeQueryPort createGaugeQueryPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;

    public abstract IGaugeConfigurationPort createGaugeConfigurationPort (IGauge gauge)
            throws RainbowConnectionException;

    public abstract IGaugeQueryPort createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException;

    public abstract IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException;

    public abstract IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe,
            IProbeConfigurationPort callback) throws RainbowConnectionException;

    public abstract IDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException;

    public abstract IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException;

    public abstract IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException;

    public abstract IProbeReportSubscriberPort createProbeReportingPortSubscriber (IProbeReportPort callback)
            throws RainbowConnectionException;

    public abstract IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException;


    public abstract IEffectorLifecycleBusPort
    createSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort delegate)
            throws RainbowConnectionException;

    public abstract IEffectorExecutionPort createEffectorExecutionPort (IEffector effector)
            throws RainbowConnectionException;

    public abstract IEffectorExecutionPort createEffectorExecutionPort (IEffectorIdentifier effector)
            throws RainbowConnectionException;

    public abstract IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException;

    public abstract IModelChangeBusSubscriberPort
    createModelChangeBusSubscriptionPort ()
            throws RainbowConnectionException;

    public abstract IRainbowReportingSubscriberPort
    createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportT)
            throws RainbowConnectionException;

    public abstract IModelDSBusPublisherPort createModelDSPublishPort (Identifiable id)
            throws RainbowConnectionException;

    public abstract IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable component)
            throws RainbowConnectionException;

    public abstract IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException;

    public abstract IModelsManagerPort createModeslManagerRequirerPort () throws RainbowConnectionException;

}
