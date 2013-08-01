package org.sa.rainbow.management.ports.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.gauges.IRainbowGaugeLifecycleBusPort;
import org.sa.rainbow.management.ports.IRainbowConnectionPortFactory;
import org.sa.rainbow.management.ports.IRainbowManagementPort;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;
import org.sa.rainbow.models.IModelsManager;
import org.sa.rainbow.models.ports.IRainbowModelUSBusPort;

public class LocalRainbowManagementPortFactory implements IRainbowConnectionPortFactory {

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

    private LocalRainbowManagementPortFactory () {
    };

    @Override
    @NonNull
    public IRainbowManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
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
    @NonNull
    public IRainbowManagementPort createDelegateSideManagementPort (RainbowDelegate delegate, String delegateID) {
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
    @NonNull
    public IRainbowMasterConnectionPort createMasterSideConnectionPort (final RainbowMaster rainbowMaster) {
        if (m_masterConnectionPort == null) {
            m_masterConnectionPort = new LocalMasterConnectionPort (rainbowMaster);
        }
        return m_masterConnectionPort;
    }

    @Override
    @NonNull
    public IRainbowMasterConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
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
            m_instance = new LocalRainbowManagementPortFactory ();
        }
        return m_instance;
    }

    @Override
    public IRainbowModelUSBusPort createModelsManagerUSPort (IModelsManager m) throws RainbowConnectionException {
        if (m_localModelsManagerUSPort == null) {
            m_localModelsManagerUSPort = new LocalModelsManagerUSPort (m);
            for (LocalModelsManagerClientUSPort p : m_mmClientUSPorts.values ()) {
                p.connect (m_localModelsManagerUSPort);
            }
        }
        return m_localModelsManagerUSPort;
    }

    @Override
    public IRainbowModelUSBusPort createModelsManagerClientUSPort (Identifiable client)
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
    public IRainbowGaugeLifecycleBusPort createGaugeSideLifecyclePort () throws RainbowConnectionException {
        return null;
    }

}
