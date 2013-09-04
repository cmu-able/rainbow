package org.sa.rainbow.core.ports;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Represents a deployment port that is not connected to either a master or a delegate. Any calls will log an error.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class DisconnectedRainbowManagementPort implements IRainbowManagementPort {

    Logger                                   LOGGER     = Logger.getLogger (DisconnectedRainbowManagementPort.class);

    static DisconnectedRainbowManagementPort m_instance = new DisconnectedRainbowManagementPort ();

    public static IRainbowManagementPort instance () {
        return m_instance;
    }

    private DisconnectedRainbowManagementPort () {
    }

    @Override
    public String getDelegateId () {
        LOGGER.error ("Attempt to get the delegate of a disconnected deployment port");
        return "";
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        LOGGER.error ("Attempt to send configuration information to a disconnected deployment port");

    }

    @Override
    public void heartbeat () {
        LOGGER.error ("Attempt to receive heartbeat from a disconnected deployment port");

    }

    @Override
    public void requestConfigurationInformation () {
        LOGGER.error ("Attempt to request configuration information from a disconnected deployment port");

    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to start a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to pause a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        LOGGER.error ("Attempt to terminate a delegate from a disconnected deployment port");
        return false;
    }

    @Override
    public void dispose () {
    }

}
