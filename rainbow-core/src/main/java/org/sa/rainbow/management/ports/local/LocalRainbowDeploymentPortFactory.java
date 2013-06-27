package org.sa.rainbow.management.ports.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNull;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.management.ports.IRainbowDeploymentPort;
import org.sa.rainbow.management.ports.IRainbowDeploymentPortFactory;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;

public class LocalRainbowDeploymentPortFactory implements IRainbowDeploymentPortFactory {

    /**
     * Singleton instance
     */
    private static IRainbowDeploymentPortFactory m_instance;
    Map<String, LocalMasterDeploymentPort>           m_masterPorts   = new HashMap<> ();
    LocalMasterConnectionPort                    m_masterConnectionPort;
    Map<String, LocalDelegateDeploymentPort>         m_delegatePorts = new HashMap<> ();
    Map<String, LocalDelegateConnectionPort>     m_delegateConnectionPorts = new HashMap<> ();

    private LocalRainbowDeploymentPortFactory () {
    };


    @Override
    @NonNull
    public IRainbowDeploymentPort createMasterDeploymentePort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {

        LocalMasterDeploymentPort mdp = m_masterPorts.get (delegateID);
        if (mdp == null) {
            mdp = new LocalMasterDeploymentPort (rainbowMaster, delegateID);
            m_masterPorts.put (delegateID, mdp);
            connectMasterAndDelegate (delegateID);
        }
        return mdp;
    }


    @Override
    @NonNull
    public IRainbowDeploymentPort createDelegateDeploymentPortPort (RainbowDelegate delegate, String delegateID) {
        LocalDelegateDeploymentPort ddp = m_delegatePorts.get (delegateID);
        if (ddp == null) {
            ddp = new LocalDelegateDeploymentPort (delegate, delegateID);
            m_delegatePorts.put (delegateID, ddp);
            connectMasterAndDelegate (delegateID);
        }
        return ddp;
    }

    private void connectMasterAndDelegate (String delegateID) {
        LocalMasterDeploymentPort mdp = m_masterPorts.get (delegateID);
        LocalDelegateDeploymentPort ddp = m_delegatePorts.get (delegateID);
        if (mdp != null && ddp != null) {
            mdp.connect (ddp);
            ddp.connect (mdp);
        }
    }

    @Override
    @NonNull
    public IRainbowMasterConnectionPort createDelegateConnectionPort (final RainbowMaster rainbowMaster) {
        if (m_masterConnectionPort == null) {
            m_masterConnectionPort = new LocalMasterConnectionPort (rainbowMaster);
        }
        return m_masterConnectionPort;
    }

    @Override
    @NonNull
    public IRainbowMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate) {
        LocalDelegateConnectionPort ldcp = m_delegateConnectionPorts.get (delegate.getId ());
        if (ldcp == null) {
            ldcp = new LocalDelegateConnectionPort (delegate, this);
            ldcp.connect (m_masterConnectionPort);
            m_delegateConnectionPorts.put (delegate.getId (), ldcp);
        }
        return ldcp;
    }

    public static IRainbowDeploymentPortFactory getFactory () {
        if (m_instance == null) {
            m_instance = new LocalRainbowDeploymentPortFactory ();
        }
        return m_instance;
    }

}
