package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.RainbowDelegate;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.management.ports.DisconnectedRainbowManagementPort;
import org.sa.rainbow.management.ports.DisconnectedRainbowMasterConnectionPort;
import org.sa.rainbow.management.ports.IRainbowManagementPort;
import org.sa.rainbow.management.ports.IRainbowManagementPortFactory;
import org.sa.rainbow.management.ports.IRainbowMasterConnectionPort;

public class ESEBRainbowManagementPortFactory implements IRainbowManagementPortFactory {

    private static ESEBRainbowManagementPortFactory m_instance;

    private ESEBRainbowManagementPortFactory () {

    }

    @Override
    public IRainbowMasterConnectionPort createDelegateSideConnectionPort (RainbowDelegate delegate) {
        try {
            return new ESEBDelegateConnectionPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowMasterConnectionPort.instance ();
        }

    }

    @Override
    public IRainbowMasterConnectionPort createMasterSideConnectionPort (RainbowMaster rainbowMaster) {

        try {
            return new ESEBMasterConnectionPort (rainbowMaster);
        }
        catch (IOException e) {
            return DisconnectedRainbowMasterConnectionPort.instance ();
        }
    }

    @Override
    public IRainbowManagementPort createDelegateSideManagementPort (RainbowDelegate delegate, String delegateID) {
        try {
            return new ESEBDelegateManagementPort (delegate);
        }
        catch (IOException e) {
            return DisconnectedRainbowManagementPort.instance ();
        }
    }

    @Override
    public IRainbowManagementPort createMasterSideManagementPort (RainbowMaster rainbowMaster,
            String delegateID,
            Properties connectionProperties) {
        try {
            return new ESEBMasterSideManagementPort (rainbowMaster, delegateID, connectionProperties);
        }
        catch (Throwable t) {
            return DisconnectedRainbowManagementPort.instance ();
        }

    }

    public static IRainbowManagementPortFactory getFactory () {
        if (m_instance == null) {
            m_instance = new ESEBRainbowManagementPortFactory ();
        }
        return m_instance;
    }

}
