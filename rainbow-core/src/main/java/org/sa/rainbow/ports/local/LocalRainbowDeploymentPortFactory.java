package org.sa.rainbow.ports.local;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.ports.IRainbowDeploymentPort;
import org.sa.rainbow.ports.IRainbowDeploymentPortFactory;
import org.sa.rainbow.ports.IRainbowMasterConnectionPort;

public class LocalRainbowDeploymentPortFactory implements IRainbowDeploymentPortFactory {

    /**
     * Singleton instance
     */
    private static IRainbowDeploymentPortFactory m_instance;
    Map<String, LocalMasterDeploymentPort>           m_masterPorts   = new HashMap<> ();
    IRainbowMasterConnectionPort                 m_masterConnectionPort;
    Map<String, LocalDelegateDeploymentPort>         m_delegatePorts = new HashMap<> ();

    private LocalRainbowDeploymentPortFactory () {
    };


    @Override
    @NonNull
    public IRainbowDeploymentPort createMasterDelegatePort (RainbowMaster rainbowMaster, String delegateID) {

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
    public IRainbowDeploymentPort createDelegateDelegatePort (RainbowDelegate delegate, String delegateID) {
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
            m_masterConnectionPort = new IRainbowMasterConnectionPort () {

                @Override
                public IRainbowDeploymentPort connectDelegate (RainbowDelegate delegate, String delegateID) {
                    IRainbowDeploymentPort ddp = createDelegateDelegatePort (delegate, delegateID);
                    rainbowMaster.connectDelegate (delegateID);
                    return ddp;
                }

                @Override
                public void disconnectDelegate (RainbowDelegate delegate) {
                    rainbowMaster.disconnectDelegate (delegate.getId ());
                }

            };
        }
        return m_masterConnectionPort;
    }

    @Override
    @NonNull
    public IRainbowMasterConnectionPort getDelegateMasterConnectionPort () {
        if (m_masterConnectionPort != null) return m_masterConnectionPort;
        else
            throw new IllegalStateException ("A master does not seem to exist to connect to");
    }

    public static IRainbowDeploymentPortFactory createFactory () {
        if (m_instance == null) {
            m_instance = new LocalRainbowDeploymentPortFactory ();
        }
        return m_instance;
    }

}
