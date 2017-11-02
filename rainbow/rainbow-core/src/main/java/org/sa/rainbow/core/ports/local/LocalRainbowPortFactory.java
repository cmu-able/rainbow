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
package org.sa.rainbow.core.ports.local;


import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.Rainbow;
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
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LocalRainbowPortFactory implements IRainbowConnectionPortFactory {

    /**
     * Singleton instance
     */
    private static IRainbowConnectionPortFactory       m_instance;

    Map<String, LocalMasterSideManagementPort>         m_masterPorts             = new HashMap<> ();
    LocalMasterConnectionPort                          m_masterConnectionPort;

    Map<String, LocalDelegateManagementPort>           m_delegatePorts           = new HashMap<> ();

    Map<String, LocalDelegateConnectionPort>           m_delegateConnectionPorts = new HashMap<> ();
    private LocalModelsManagerUSPort                   m_localModelsManagerUSPort;

    private Map<String, LocalModelsManagerClientUSPort> m_mmClientUSPorts         = new HashMap<> ();

    private LocalRainbowPortFactory () {
    }

    @Override
    public IDelegateManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {

        LocalMasterSideManagementPort mdp = m_masterPorts.get (delegateID);
        if (mdp == null) {
            mdp = new LocalMasterSideManagementPort (rainbowMaster, delegateID);
            m_masterPorts.put (delegateID, mdp);
            connectMasterAndDelegate (delegateID);
        }
        return mdp;
    }

    @Override
    public IDelegateManagementPort createDelegateSideManagementPort (RainbowDelegate delegate, String delegateID) {
        LocalDelegateManagementPort ddp = m_delegatePorts.get (delegateID);
        if (ddp == null) {
            ddp = new LocalDelegateManagementPort (delegate, delegateID);
            m_delegatePorts.put (delegateID, ddp);
            connectMasterAndDelegate (delegateID);
        }
        return ddp;
    }

    private void connectMasterAndDelegate (String delegateID) {
        LocalMasterSideManagementPort mdp = m_masterPorts.get (delegateID);
        LocalDelegateManagementPort ddp = m_delegatePorts.get (delegateID);
        if (mdp != null && ddp != null) {
            mdp.connect (ddp);
            ddp.connect (mdp);
        }
    }

    @Override
    public IMasterConnectionPort createMasterSideConnectionPort (final RainbowMaster rainbowMaster) {
        if (m_masterConnectionPort == null) {
            try {
                m_masterConnectionPort = new LocalMasterConnectionPort (rainbowMaster);
            }
            catch (IOException e) {
                // Should never happen
                e.printStackTrace ();
            }
        }
        return m_masterConnectionPort;
    }

    @Override
    public AbstractDelegateConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
        LocalDelegateConnectionPort ldcp = m_delegateConnectionPorts.get (delegate.getId ());
        if (ldcp == null) {
            try {
                ldcp = new LocalDelegateConnectionPort (delegate, this);
            }
            catch (IOException e) {
                // Should never happen
            }
            assert ldcp != null;
            ldcp.connect (m_masterConnectionPort);
            m_delegateConnectionPorts.put (delegate.getId (), ldcp);
        }
        return ldcp;
    }

    public static IRainbowConnectionPortFactory getFactory () {
        if (m_instance == null) {
            m_instance = new LocalRainbowPortFactory ();
        }
        return m_instance;
    }

    @Override
    public IModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        if (m_localModelsManagerUSPort == null) {
            m_localModelsManagerUSPort = new LocalModelsManagerUSPort (m);
            for (LocalModelsManagerClientUSPort p : m_mmClientUSPorts.values ()) {
                p.connect (m_localModelsManagerUSPort);
            }
        }
        return m_localModelsManagerUSPort;
    }

    @Override
    public IModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
            throws RainbowConnectionException {
        LocalModelsManagerClientUSPort port = m_mmClientUSPorts.get (client.id ());
        if (port == null) {
            port = new LocalModelsManagerClientUSPort (client);
            port.connect (m_localModelsManagerUSPort);
            m_mmClientUSPorts.put (client.id (), port);
        }
        return port;
    }


    @Override
    public IGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");
    }


    @Override
    public IModelChangeBusPort createChangeBusAnnouncePort () throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IGaugeLifecycleBusPort createManagerGaugeLifecyclePort (IGaugeLifecycleBusPort manager) {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");
    }


    @Override
    public IGaugeQueryPort createGaugeQueryPortClient (IGaugeIdentifier gauge)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IGaugeConfigurationPort createGaugeConfigurationPort (IGauge gauge) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");
    }


    @Override
    public IGaugeQueryPort createGaugeQueryPort (IGauge gauge) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IProbeReportPort createProbeReportingPortSender (IProbe probe) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IProbeConfigurationPort createProbeConfigurationPort (Identifiable probe, IProbeConfigurationPort callback)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPort (RainbowDelegate rainbowDelegate)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IDelegateConfigurationPort createDelegateConfigurationPortClient (String delegateID)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IProbeLifecyclePort createProbeManagementPort (IProbe probe) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IProbeReportSubscriberPort createProbeReportingPortSubscriber (IProbeReportPort callback)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IEffectorLifecycleBusPort createEffectorSideLifecyclePort () throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IEffectorLifecycleBusPort createSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort delegate)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffector effector) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffectorIdentifier effector)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");
    }


    @Override
    public IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort ()
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IRainbowReportingSubscriberPort createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportT)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IModelDSBusPublisherPort createModelDSPublishPort (Identifiable id) throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable id) {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IModelsManagerPort createModelsManagerProviderPort (IModelsManager modelsManager)
            throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public IModelsManagerPort createModeslManagerRequirerPort () throws RainbowConnectionException {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public <S extends IEvaluable> IRainbowAdaptationEnqueuePort<S> createAdaptationEnqueuePort (ModelReference model) {
        throw new UnsupportedOperationException ("NYS");

    }


    @Override
    public <S extends IEvaluable> IRainbowAdaptationDequeuePort<S> createAdaptationDequeuePort (ModelReference model) {
        throw new UnsupportedOperationException ("NYS");

    }

    @Override
    public IMasterCommandPort createMasterCommandProviderPort (RainbowMaster rainbowMaster)
            throws RainbowConnectionException {
        return rainbowMaster;
    }

    @Override
    public IMasterCommandPort createMasterCommandRequirerPort () throws RainbowConnectionException {
        return Rainbow.instance ().getRainbowMaster ();
    }

}
