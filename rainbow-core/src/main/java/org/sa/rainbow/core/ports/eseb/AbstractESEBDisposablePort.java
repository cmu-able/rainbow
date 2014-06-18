package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;

import org.sa.rainbow.core.ports.IDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

/**
 * The parent class of all ESEB publishing ports. This class manages the connection to ESEB, as well as disposing
 * resources.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractESEBDisposablePort implements IDisposablePort {

    private ESEBConnector m_connectionRole;

    protected AbstractESEBDisposablePort (String host, short port, ChannelT channel) throws IOException {
        if (host != null) {
            m_connectionRole = new ESEBConnector (host, port, channel);
        }
    }

    public AbstractESEBDisposablePort (short port, ChannelT channel) throws IOException {
        m_connectionRole = new ESEBConnector (port, channel);
    }

    /**
     * Closes the connection for this port.
     */
    @Override
    public void dispose () {
        if (getConnectionRole () == null) return;
        try {
            getConnectionRole().close ();
            m_connectionRole = null;
        }
        catch (IOException e) {
            ESEBConnector.LOGGER.error (e);
        }
    }

    protected ESEBConnector getConnectionRole () {
        return m_connectionRole;
    }


}
