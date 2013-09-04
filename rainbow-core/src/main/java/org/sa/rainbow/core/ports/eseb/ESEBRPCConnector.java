package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.ports.eseb.converters.CollectionConverter;
import org.sa.rainbow.core.ports.eseb.converters.CommandRepresentationConverter;
import org.sa.rainbow.core.ports.eseb.converters.DescriptionAttributesConverter;
import org.sa.rainbow.core.ports.eseb.converters.GaugeStateConverter;
import org.sa.rainbow.core.ports.eseb.converters.TypedAttributeConverter;

import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.participant.ParticipantIdentifier;
import edu.cmu.cs.able.eseb.rpc.RpcEnvironment;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConverter;

public class ESEBRPCConnector {
    static Logger LOGGER = Logger.getLogger (ESEBRPCConnector.class);
    private BusConnection m_client;
    private EventBus      m_srvr;

    private static class RPCInfo {
        ParticipantIdentifier m_pId;
        RpcEnvironment        m_env;
    }

    private static Map<BusConnection, RPCInfo> m_infoMap = new HashMap<> ();

    private RPCInfo                            m_info;

    public ESEBRPCConnector (String remoteHost, short remotePort) throws IOException, ParticipantException {
        setClient (remoteHost, remotePort);
        setUpEnvironment ();
    }

    public ESEBRPCConnector (short port) throws IOException, ParticipantException {
        m_srvr = ESEBProvider.getBusServer (port);
        setClient ("localhost", port);
        setUpEnvironment ();

    }

    private void setUpEnvironment () throws ParticipantException {
        RPCInfo info = m_infoMap.get (m_client);
        if (info == null) {
            info = new RPCInfo ();
            info.m_pId = new ParticipantIdentifier (m_client);
            info.m_env = new RpcEnvironment (m_client, info.m_pId.id ());
            m_infoMap.put (m_client, info);
            setupConverters (info);
        }
        m_info = info;
    }

    private void setupConverters (RPCInfo info) {
        // Share the participant, environment, and then you can share the connection 
        TypelibJavaConverter converter = info.m_env.converter ();
        converter.add (new CollectionConverter ());
        converter.add (new TypedAttributeConverter (ESEBProvider.SCOPE));
        converter.add (new CommandRepresentationConverter (ESEBProvider.SCOPE));
        converter.add (new GaugeStateConverter (ESEBProvider.SCOPE));
        converter.add (new DescriptionAttributesConverter (ESEBProvider.SCOPE));
    }

    protected void setClient (String remoteHost, short remotePort) {
        if (m_client != null) {
            if (!m_client.host ().equals (remoteHost) || m_client.port () != remotePort) {
                m_client.stop ();
                m_client = null;
            }
            else
                return;
        }
        m_client = ESEBProvider.getBusClient (remoteHost, remotePort);
    }

    public BusConnection getESEBConnection () {
        return m_client;
    }

    public ParticipantIdentifier getParticipantIdentifier () {
        return m_info.m_pId;
    }

    public RpcEnvironment getRPCEnvironment () {
        return m_info.m_env;
    }

}
