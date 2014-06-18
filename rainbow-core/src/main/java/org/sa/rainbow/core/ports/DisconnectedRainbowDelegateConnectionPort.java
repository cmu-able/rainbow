package org.sa.rainbow.core.ports;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

public class DisconnectedRainbowDelegateConnectionPort extends AbstractDelegateConnectionPort {

    public DisconnectedRainbowDelegateConnectionPort () throws IOException {
        super (null, null, (short )-1, ChannelT.HEALTH);
    }

    Logger                                   LOGGER     = Logger.getLogger (DisconnectedRainbowDelegateConnectionPort.class);

    static DisconnectedRainbowDelegateConnectionPort m_instance;

    static {
        try {
            m_instance = new DisconnectedRainbowDelegateConnectionPort ();
        }
        catch (IOException e) {
        }
    }

    public static AbstractDelegateConnectionPort instance () {
        return m_instance;
    }

    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties)
            throws RainbowConnectionException {
        LOGGER.error ("Attempt to connect through a disconnected deployment port");
        return DisconnectedRainbowManagementPort.instance ();
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        LOGGER.error ("Attempt to disconnect through a disconnected deployment port");


    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        String log = MessageFormat.format ("Delegate[{3}]: {0}: {1}", delegateID, msg, compT.name ());
        switch (type) {
        case INFO:
            LOGGER.info (log);
            break;
        case WARNING:
            LOGGER.warn (log);
            break;
        case ERROR:
            LOGGER.error (log);
            break;
        case FATAL:
            LOGGER.fatal (log);
            break;
        }
    }

    @Override
    public void dispose () {
    }

    @Override
    public void trace (RainbowComponentT type, String msg) {
        if (LOGGER.isTraceEnabled ()) {
            LOGGER.trace (msg);
        }
    }

}
