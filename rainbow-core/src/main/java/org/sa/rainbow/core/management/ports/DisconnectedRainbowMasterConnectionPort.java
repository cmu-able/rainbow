package org.sa.rainbow.core.management.ports;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;

/**
 * Represetns a connection port that is not connected to anything. Any calls will be logged as an error.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class DisconnectedRainbowMasterConnectionPort implements IRainbowMasterConnectionPort {

    static DisconnectedRainbowMasterConnectionPort m_instance = new DisconnectedRainbowMasterConnectionPort ();

    public static IRainbowMasterConnectionPort instance () {
        return m_instance;
    }
    private DisconnectedRainbowMasterConnectionPort () {
    }

    Logger LOGGER = Logger.getLogger (DisconnectedRainbowMasterConnectionPort.class);

    @Override
    public IRainbowManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        LOGGER.error ("Attempt to connect through a disconnected port!");
        return DisconnectedRainbowManagementPort.instance ();
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        LOGGER.error ("Attempt to disconnect through a disconnected port!");

    }

    @Override
    public void dispose () {
    }

}
