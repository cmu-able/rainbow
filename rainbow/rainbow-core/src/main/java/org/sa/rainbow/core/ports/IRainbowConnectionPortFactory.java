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
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
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
    IDelegateMasterConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate)
            throws RainbowConnectionException;

    /**
     * Create the connection port on the master, which processes connection requests from delegates
     * 
     * @param rainbowMaster
     *            The master that has this port
     * @return
     */
    IMasterConnectionPort createMasterSideConnectionPort (final RainbowMaster rainbowMaster)
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
    IDelegateManagementPort
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
    IDelegateManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) throws RainbowConnectionException;

    IModelUSBusPort createModelsManagerUSPort (IModelsManager m)
            throws RainbowConnectionException;

    IModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException;


    IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException;


    IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException;


    IGaugeLifecycleBusPort
    createManagerGaugeLifecyclePort (IGaugeLifecycleBusPort manager) throws RainbowConnectionException;


    IGaugeConfigurationPort createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;


    IGaugeQueryPort createGaugeQueryPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException;


    IGaugeConfigurationPort createGaugeConfigurationPort (IGauge gauge)
            throws RainbowConnectionException;


    IGaugeQueryPort createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException;


    IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException;


    IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe,
            IProbeConfigurationPort callback) throws RainbowConnectionException;


    IDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException;


    IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException;


    IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException;


    IProbeReportSubscriberPort createProbeReportingPortSubscriber (IProbeReportPort callback)
            throws RainbowConnectionException;


    IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException;


    IEffectorLifecycleBusPort
    createSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort delegate)
            throws RainbowConnectionException;


    IEffectorExecutionPort createEffectorExecutionPort (IEffector effector)
            throws RainbowConnectionException;


    IEffectorExecutionPort createEffectorExecutionPort (IEffectorIdentifier effector)
            throws RainbowConnectionException;


    IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException;


    IModelChangeBusSubscriberPort
    createModelChangeBusSubscriptionPort ()
            throws RainbowConnectionException;


    IRainbowReportingSubscriberPort
    createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportT)
            throws RainbowConnectionException;


    IModelDSBusPublisherPort createModelDSPublishPort (Identifiable id)
            throws RainbowConnectionException;


    IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable component)
            throws RainbowConnectionException;


    IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException;


    IModelsManagerPort createModeslManagerRequirerPort () throws RainbowConnectionException;

    <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S>
    createAdaptationEnqueuePort (ModelReference model);

    <S extends IEvaluable> IRainbowAdaptationDequeuePort<S>
    createAdaptationDequeuePort (ModelReference model);

    IMasterCommandPort createMasterCommandProviderPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException;

    IMasterCommandPort createMasterCommandRequirerPort () throws RainbowConnectionException;

}
