package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.sa.rainbow.core.ConfigHelper;
import org.sa.rainbow.core.ports.IDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConversionRule;

public class AbstractESEBDisposableRPCPort implements IDisposablePort {

    private ESEBRPCConnector m_connectionRole;
    private String           m_host;
    private short            m_port;
    private String           m_id;

    public AbstractESEBDisposableRPCPort (String host, short port, String id) throws IOException, ParticipantException {
        m_host = host;
        m_port = port;
        m_id = id;
    }


    @Override
    public void dispose () {
        try {
            getConnectionRole().close ();
            m_connectionRole = null;
        }
        catch (IOException | ParticipantException e) {
            ESEBRPCConnector.LOGGER.error (e);
        }
    }

    protected ESEBRPCConnector getConnectionRole () throws IOException, ParticipantException {
        if (m_connectionRole == null) {
            initializeConnectionRole ();
        }
        return m_connectionRole;
    }

    private void initializeConnectionRole () throws IOException, ParticipantException {
        m_connectionRole = new ESEBRPCConnector (m_host, m_port, m_id);

    }

    protected void setupModelConverters (String key) {
        List<String> converters = ConfigHelper.getProperties (key);
        if (converters.isEmpty ()) {
            ESEBRPCConnector.LOGGER
            .warn ("No model converters were found. Errors could ensue when using this connector.");
        }
        else {
            for (String cvt : converters) {
                try {
                    Class<?> cls = Class.forName (cvt);
                    ESEBProvider.registerConverter ((Class<TypelibJavaConversionRule> )cls);
                }
                catch (ClassNotFoundException e) {
                    ESEBRPCConnector.LOGGER.error (MessageFormat
                            .format ("Could not find converter class ''{0}''.", cvt));
                }
            }
        }
    }


}
