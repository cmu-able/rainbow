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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.DisconnectedRainbowManagementPort;
import org.sa.rainbow.core.ports.DisconnectedRainbowMasterConnectionPort;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IDelegateMasterConnectionPort;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBDelegateConfigurationProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBDelegateConfigurationRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBEffectorExecutionProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBEffectorExecutionRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeConfigurationProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeConfigurationRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeQueryProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBGaugeQueryRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBMasterCommandProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBMasterCommandRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBModelsManagerProviderPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBModelsManagerRequirerPort;
import org.sa.rainbow.core.ports.eseb.rpc.ESEBProbeConfigurationProviderPort;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBRainbowPortFactory implements IRainbowConnectionPortFactory {

    private static ESEBRainbowPortFactory m_instance;

    private ESEBRainbowPortFactory () {

    }

    @Override
    public IDelegateMasterConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
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


    @Override
    public IModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        try {
            return new ESEBModelManagerModelUpdatePort (m);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IModelUSBusPort createModelsManagerClientUSPort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBGaugeModelUSBusPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        try {
            return new ESEBGaugeSideLifecyclePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException {
        try {
            return new ESEBChangeBusAnnouncePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


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


    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        try {
            return new ESEBGaugeConfigurationRequirerPort (gauge);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IGaugeQueryPort createGaugeQueryPortClient (IGaugeIdentifier gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeQueryRequirerPort (gauge);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPort (IGauge gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeConfigurationProviderPort (gauge);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IGaugeQueryPort createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException {
        try {
            return new ESEBGaugeQueryProviderPort (gauge);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException {
        try {
            return new ESEBProbeReportingPortSender (probe);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }

    }


    @Override
    public IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe, IProbeConfigurationPort callback)
            throws RainbowConnectionException {
        try {
            return new ESEBProbeConfigurationProviderPort (probe, callback);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException {
        try {
            return new ESEBDelegateConfigurationProviderPort (rainbowDelegate);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException {
        try {
            return new ESEBDelegateConfigurationRequirerPort (delegateID);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException {
        try {
            return new ESEBProbeLifecyclePort (probe);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


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


    @Override
    public IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException {
        try {
            return new ESEBEffectorSideLifecyclePort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


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


    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffector effector) throws RainbowConnectionException {
        try {
            return new ESEBEffectorExecutionProviderPort (effector);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffectorIdentifier effector)
            throws RainbowConnectionException {
        try {
            return new ESEBEffectorExecutionRequirerPort (effector);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException {
        try {
            return new ESEBMasterReportingPort ();
        }
        catch (Exception e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort () throws RainbowConnectionException {
        try {
            return new ESEBModelChangeBusSubscriptionPort ();
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


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


    @Override
    public IModelDSBusPublisherPort createModelDSPublishPort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBModelDSPublishPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable client) throws RainbowConnectionException {
        try {
            return new ESEBModelDSPublishPort (client);
        }
        catch (IOException e) {
            throw new RainbowConnectionException ("Failed to connect", e);

        }
    }


    @Override
    public IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException {
        try {
            return new ESEBModelsManagerProviderPort (modelsManager);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IModelsManagerPort createModeslManagerRequirerPort () throws RainbowConnectionException {
        try {
            return new ESEBModelsManagerRequirerPort ();
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

    private final Map<String, ESEBAdaptationQConnector> m_adaptationConnectors = new HashMap<> ();

    @Override
    public <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S> createAdaptationEnqueuePort (ModelReference model) {
        return getAdaptationConnectorForModel (model);
    }

    private <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S>
    getAdaptationConnectorForModel (ModelReference model) {
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
    public <S extends IEvaluable> IRainbowAdaptationDequeuePort<S> createAdaptationDequeuePort (ModelReference model) {
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
    public IMasterCommandPort createMasterCommandProviderPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException {
        try {
            return new ESEBMasterCommandProviderPort (rainbowMaster);
        } catch (IOException | ParticipantException e) {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }


    @Override
    public IMasterCommandPort createMasterCommandRequirerPort () throws RainbowConnectionException {
        try {
            return new ESEBMasterCommandRequirerPort ();
        } catch (IOException |

                ParticipantException e)

        {
            throw new RainbowConnectionException ("Failed to connect", e);
        }
    }

}
