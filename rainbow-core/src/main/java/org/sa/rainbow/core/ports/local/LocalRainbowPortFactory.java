package org.sa.rainbow.core.ports.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.models.IModelInstanceProvider;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeConfigurationPort;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.IGaugeQueryPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort.IRainbowReportingSubscriberCallback;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.probes.IProbe;

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
    };

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
            m_masterConnectionPort = new LocalMasterConnectionPort (rainbowMaster);
        }
        return m_masterConnectionPort;
    }

    @Override
    public AbstractDelegateConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
        LocalDelegateConnectionPort ldcp = m_delegateConnectionPorts.get (delegate.getId ());
        if (ldcp == null) {
            ldcp = new LocalDelegateConnectionPort (delegate, this);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEffectorLifecycleBusPort createSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort delegate)
            throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffector effector) throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IEffectorExecutionPort createEffectorExecutionPort (IEffectorIdentifier effector)
            throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IRainbowReportingPort createMasterReportingPort () throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IModelChangeBusSubscriberPort createModelChangeBusSubscriptionPort (IModelInstanceProvider provider)
            throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IRainbowReportingSubscriberPort createReportingSubscriberPort (IRainbowReportingSubscriberCallback reportT)
            throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public IModelDSBusPublisherPort createModelDSPublishPort (Identifiable id) throws RainbowConnectionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IModelDSBusSubscriberPort createModelDSubscribePort (Identifiable id) {
        // TODO Auto-generated method stub
        return null;
    }

}
