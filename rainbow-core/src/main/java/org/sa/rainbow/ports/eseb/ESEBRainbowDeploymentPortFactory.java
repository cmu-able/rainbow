package org.sa.rainbow.ports.eseb;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.ports.DisconnectedRainbowDeploymentPort;
import org.sa.rainbow.ports.DisconnectedRainbowMasterConnectionPort;
import org.sa.rainbow.ports.IRainbowDeploymentPort;
import org.sa.rainbow.ports.IRainbowDeploymentPortFactory;
import org.sa.rainbow.ports.IRainbowMasterConnectionPort;

public class ESEBRainbowDeploymentPortFactory implements IRainbowDeploymentPortFactory {

    private static ESEBRainbowDeploymentPortFactory m_instance;

    private ESEBRainbowDeploymentPortFactory () {

    }

    @Override
    public IRainbowMasterConnectionPort createDelegateMasterConnectionPort (RainbowDelegate delegate) {
        try {
            return new ESEBDelegateConnectionPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowMasterConnectionPort.instance ();
        }

    }

    @Override
    public IRainbowMasterConnectionPort createDelegateConnectionPort (RainbowMaster rainbowMaster) {

        try {
            return new ESEBMasterConnectionPort (rainbowMaster);
        }
        catch (IOException e) {
            return DisconnectedRainbowMasterConnectionPort.instance ();
        }
    }

    @Override
    public IRainbowDeploymentPort createDelegateDeploymentPortPort (RainbowDelegate delegate, String delegateID) {
        try {
            return new ESEBDelegateDeploymentPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowDeploymentPort.instance ();
        }
    }

    @Override
    public IRainbowDeploymentPort createMasterDeploymentePort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {
        try {
            return new ESEBMasterDeploymentPort (rainbowMaster, delegateID, connectionProperties);
        }
        catch (Throwable t) {
            return DisconnectedRainbowDeploymentPort.instance ();
        }

    }

    public static IRainbowDeploymentPortFactory getFactory () {
        if (m_instance == null) {
            m_instance = new ESEBRainbowDeploymentPortFactory ();
        }
        return m_instance;
    }

}
