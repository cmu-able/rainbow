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
package org.sa.rainbow.core.ports.eseb;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import org.jetbrains.annotations.NotNull;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.eseb.rpc.*;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ESEBRainbowPortFactory implements IRainbowConnectionPortFactory {

    private static ESEBRainbowPortFactory m_instance;

    private ESEBRainbowPortFactory () {

    }

    @Override
    public AbstractDelegateConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
        try {
            return new ESEBDelegateConnectionPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowDelegateConnectionPort.instance ();
        }

    }

    @Override
    public IMasterConnectionPort createMasterSideConnectionPort (RainbowMaster rainbowMaster) {

        try {
            return new ESEBMasterConnectionPort (rainbowMaster);
        }
        catch (IOException e) {
            return DisconnectedRainbowMasterConnectionPort.instance ();
        }
    }

    @Override
    public IDelegateManagementPort createDelegateSideManagementPort (RainbowDelegate delegate, String delegateID) {
        try {
            return new ESEBDelegateManagementPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowManagementPort.instance ();
        }
    }

    @Override
    public IDelegateManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {
        try {
            return new ESEBMasterSideManagementPort (rainbowMaster, delegateID, connectionProperties);
        }
        catch (Throwable t) {
            return DisconnectedRainbowManagementPort.instance ();
        }

    }

    public static IRainbowConnectionPortFactory getFactory () {
        if (m_instance == null) {
            m_instance = new ESEBRainbowPortFactory ();
        }
        return m_instance;
    }

    @NotNull
    @Override
    public IModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        try {
            return new ESEBModelManagerModelUpdatePort (m);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IModelUSBusPort createModelsManagerClientUSPort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBGaugeModelUSBusPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        try {
            return new ESEBGaugeSideLifecyclePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException {
        try {
            return new ESEBChangeBusAnnouncePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IGaugeLifecycleBusPort createManagerGaugeLifecyclePort (IGaugeLifecycleBusPort manager)
            throws RainbowConnectionException {
        try {
            return new ESEBReceiverSideGaugeLifecyclePort (manager);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPortClient (@NotNull IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        try {
            return new ESEBGaugeConfigurationRequirerPort (gauge);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IGaugeQueryPort createGaugeQueryPortClient (@NotNull IGaugeIdentifier gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeQueryRequirerPort (gauge);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPort (@NotNull IGauge gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeConfigurationProviderPort (gauge);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IGaugeQueryPort createGaugeQueryPort (@NotNull IGauge gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeQueryProviderPort (gauge);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException {
        try {
            return new ESEBProbeReportingPortSender (probe);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }

    }

    @NotNull
    @Override
    public IProbeConfigurationPort createProbeConfigurationPort (@NotNull Identifiable probe, IProbeConfigurationPort callback)
            throws RainbowConnectionException {
        try {
            return new ESEBProbeConfigurationProviderPort (probe, callback);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPort (@NotNull RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException {
        try {
            return new ESEBDelegateConfigurationProviderPort (rainbowDelegate);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException {
        try {
            return new ESEBDelegateConfigurationRequirerPort (delegateID);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException {
        try {
            return new ESEBProbeLifecyclePort (probe);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IProbeReportSubscriberPort createProbeReportingPortSubscriber (IProbeReportPort callback)
            throws RainbowConnectionException {
        try {
            return new ESEBProbeReportSubscriberPort (callback);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }

    }

    @NotNull
    @Override
    public IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException {
        try {
            return new ESEBEffectorSideLifecyclePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IEffectorLifecycleBusPort createSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort delegate)
            throws RainbowConnectionException {
        try {
            return new ESEBSubscriberSideEffectorLifecyclePort (delegate);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (@NotNull IEffector effector) throws RainbowConnectionException {
        try {
            return new ESEBEffectorExecutionProviderPort (effector);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (@NotNull IEffectorIdentifier effector)
            throws RainbowConnectionException {
        try {
            return new ESEBEffectorExecutionRequirerPort (effector);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException {
        try {
            return new ESEBMasterReportingPort ();
        }
        catch (Exception e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort () throws RainbowConnectionException {
        try {
            return new ESEBModelChangeBusSubscriptionPort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IRainbowReportingSubscriberPort createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportTo)
            throws RainbowConnectionException {
        try {
            return new ESEBRainbowReportingSubscriberPort (reportTo);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IModelDSBusPublisherPort createModelDSPublishPort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBModelDSPublishPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBModelDSPublishPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }

    @NotNull
    @Override
    public IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException {
        try {
            return new ESEBModelsManagerProviderPort (modelsManager);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IModelsManagerPort createModeslManagerRequirerPort () throws RainbowConnectionException {
        try {
            return new ESEBModelsManagerRequirerPort ();
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    private final Map<String, ESEBAdaptationQConnector> m_adaptationConnectors = new HashMap<> ();

    @Override
    public <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S> createAdaptationEnqueuePort (@NotNull ModelReference model) {
        return getAdaptationConnectorForModel (model);
    }

    private <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S>
    getAdaptationConnectorForModel (@NotNull ModelReference model) {
        synchronized (m_adaptationConnectors) {
            ESEBAdaptationQConnector<S> conn = m_adaptationConnectors.get (model.toString ());
            if (conn == null) {
                conn = new ESEBAdaptationQConnector<> ();
                m_adaptationConnectors.put (model.toString (), conn);
            }
            return conn;
        }
    }

    @Override
    public <S extends IEvaluable> IRainbowAdaptationDequeuePort<S> createAdaptationDequeuePort (@NotNull ModelReference model) {
        synchronized (m_adaptationConnectors) {

            ESEBAdaptationQConnector<S> conn = m_adaptationConnectors.get (model.toString ());
            if (conn == null) {
                conn = new ESEBAdaptationQConnector<> ();
                m_adaptationConnectors.put (model.toString (), conn);
            }
            return conn;
        }
    }

    @NotNull
    @Override
    public IMasterCommandPort createMasterCommandProviderPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException {
        try {
            return new ESEBMasterCommandProviderPort (rainbowMaster);
        } catch (@NotNull IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    @NotNull
    @Override
    public IMasterCommandPort createMasterCommandRequirerPort () throws RainbowConnectionException {
        try {
            return new ESEBMasterCommandRequirerPort ();
        } catch (@NotNull IOException |

                ParticipantException e)

        {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

}
