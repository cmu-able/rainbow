package org.sa.rainbow.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Rainbow;

import edu.cmu.cs.able.eseb.BusClient;
import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.BusServer;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * The ESEBConnector implements both a publish/subscribe and call-return connection over ESEB.
 * 
 * If the connector is created with a remoteHost and a remotePort, a client is started, connecting to the server on that
 * port. (N.B. it is assumed that a server is started)
 * 
 * If the connector is created with just a port, then a server is started on that port, and a client is created to
 * publish and receive information. If a connector is created with the same port as used previously, then the caches
 * server will be used. If a client is requested on a previously used remote host and port, then the cached client will
 * be used.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class ESEBConnector {

    /**
     * An interface that allows users to receive information that is published on the bus
     * 
     * @author Bradley Schmerl: schmerl
     * 
     */
    public interface IESEBListener {
        /**
         * Receive a message from ESEB
         * 
         * @param msg
         *            The message that is received
         */
        public void receive (Map<String, String> msg);
    }

    static Logger                           LOGGER               = Logger.getLogger (ESEBConnector.class);

    /** The set of BusServers on the local machine, keyed by the port **/
    protected static Map<Short, BusServer>  s_servers            = new HashMap<> ();
    /** The set of BusClients already created, keyed by host:port **/
    protected static Map<String, BusClient> s_clients            = new HashMap<> ();

    protected static final PrimitiveScope   SCOPE                = new PrimitiveScope ();
    protected static final MapDataType      MAP_STRING_TO_STRING = MapDataType.map_of (SCOPE.string (),
            SCOPE.string (), SCOPE);
    /** The server corresponding to this connector **/
    protected BusServer                     m_srvr;
    /** The client for this connector to publish and receive information **/
    protected BusClient                     m_client;
    /** The set of listeners that are awaiting replies. **/
    private static Map<String, IESEBListener> m_replyListeners     = new HashMap<> ();

    /**
     * Return the cached BusServer for this port, creating a new one if it doesn't yet exist
     * 
     * @param port
     *            The port that the server is connected to
     * @return the cached or newly created BusServer
     * @throws IOException
     */
    protected static BusServer getBusServer (short port) throws IOException {
        BusServer s = s_servers.get (port);
        if (s == null) {
            LOGGER.debug (MessageFormat.format ("Constructing a new BusServer on port {0}", port));
            try {
                s = new BusServer (port, SCOPE);
                s_servers.put (port, s);
                s.start ();
            }
            catch (Exception e) {
                LOGGER.warn (MessageFormat.format ("BusServer could not be created on port {0}", port));
            }
        }
        return s;
    }

    /**
     * Return the cached BusClient for this host:port combination, or create a new one if it doesn't yet exist
     * 
     * @param remoteHost
     *            The host for the client
     * @param remotePort
     *            The port for the client
     * @return The cached or newly created BusClient
     */
    protected static BusClient getBusClient (String remoteHost, short remotePort) {
        // Make sure that we translate host names to IPs
        remoteHost = Rainbow.canonicalizeHost2IP (remoteHost);
        String key = clientKey (remoteHost, remotePort);
        BusClient c = s_clients.get (key);
        if (c == null) {
            LOGGER.debug (MessageFormat.format ("Constructing a new BusClient on {0}", key));
            c = new BusClient (remoteHost, remotePort, SCOPE);
            s_clients.put (key, c);
            c.start ();
        }
        return c;
    }

    private static String clientKey (String remoteHost, short remotePort) {
        StringBuilder sb = new StringBuilder ();
        sb.append (remoteHost);
        sb.append (":");
        sb.append (remotePort);
        String key = sb.toString ();
        return key;
    }

    /**
     * Constructs a new ESEBConnector, connecting to a remote bus
     * 
     * @param remoteHost
     *            The host to connect to
     * @param remotePort
     *            The port to connect to
     * @throws IOException
     */
    public ESEBConnector (String remoteHost, short remotePort) throws IOException {
        // No need to create a server, as it can't be created remotely anyway
        // So, just set the client
        setClient (remoteHost, remotePort);
    }

    /**
     * Construct a new ESEBConnector locally on the specified port.
     * 
     * @param port
     *            The port to connect to
     * @throws IOException
     */
    public ESEBConnector (short port) throws IOException {
        // Create the server on this port
        m_srvr = getBusServer (port);
        // Create a local client
        setClient ("localhost", port);
    }

    /**
     * Setup the client for listening for messages, and the respective queues for the client
     * 
     * @param remoteHost
     * @param remotePort
     */
    protected void setClient (String remoteHost, short remotePort) {
        // reset the client if one was already set
        if (m_client != null) {
            if (!m_client.host ().equals (remoteHost) || m_client.port () != remotePort) {
                m_client.stop ();
                m_client = null;
            }
            else
                return;
        }

        m_client = getBusClient (remoteHost, remotePort);
    }

    /**
     * Encodes a map as an ESEB string to string map
     * 
     * @param map
     *            The map to encode
     * @return The ESEB value corresponding to the map
     */
    protected MapDataValue encodeMap (Map<String, String> map) {
        MapDataValue v = MAP_STRING_TO_STRING.make ();
        for (Entry<String, String> entry : map.entrySet ()) {
            v.put (SCOPE.string ().make (entry.getKey ()), SCOPE.string ().make (entry.getValue ()));
        }
        return v;
    }

    /**
     * Decodes an ESEB map into a Java Map
     * 
     * @param mdv
     *            The ESEB string to stirng map
     * @return The corresponding map
     */
    protected Map<String, String> decodeMap (MapDataValue mdv) {
        Map<String, String> m = new HashMap<> ();
        for (Entry<DataValue, DataValue> entry : mdv.all ().entrySet ()) {
            if (entry.getKey ().type ().equals (SCOPE.string ()) && entry.getValue ().type ().equals (SCOPE.string ())) {
                m.put (((StringValue )entry.getKey ()).value (), ((StringValue )entry.getValue ()).value ());
            }
        }
        return m;
    }


    /**
     * Encodes a Properties object as an ESEB string to string map
     * 
     * @param props
     *            The properties object to encode
     * @return The corresponding ESEB map value
     */
    protected MapDataValue encodeProperties (Properties props) {
        MapDataValue v = MAP_STRING_TO_STRING.make ();
        for (Entry<Object, Object> entry : props.entrySet ()) {
            String key = null;
            if (entry.getKey () instanceof String) {
                key = "__PROP_" + (String )entry.getKey ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string key is not allowed: {0}", entry.getKey ()));
                continue;
            }
            String value = null;
            if (entry.getValue () instanceof String) {
                value = (String )entry.getValue ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string value is not allowed: {0} -> {1}",
                        entry.getKey (), entry.getValue ().getClass ()));
                continue;
            }
            v.put (SCOPE.string ().make (key), SCOPE.string ().make (value));
        }
        return v;
    }

    protected Map<String, String> encodePropertiesAsMap (Properties props) {
        Map<String, String> map = new HashMap<> ();
        for (Entry<Object, Object> entry : props.entrySet ()) {
            String key = null;
            if (entry.getKey () instanceof String) {
                key = "__PROP_" + (String )entry.getKey ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string key is not allowed: {0}", entry.getKey ()));
                continue;
            }
            String value = null;
            if (entry.getValue () instanceof String) {
                value = (String )entry.getValue ();
            }
            else {
                LOGGER.error (MessageFormat.format (
                        "Attempting to encode a property with non-string value is not allowed: {0} -> {1}",
                        entry.getKey (), entry.getValue ().getClass ()));
                continue;
            }
            map.put (key, value);
        }
        return map;
    }

    /**
     * Generates a Properties object from a Map by pulling out the __PROP_ preceded keys
     * 
     * @param msg
     * @return
     */
    public Properties decodeProperties (Map<String, String> msg) {
        Properties p = new Properties ();
        for (Entry<String, String> entry : msg.entrySet ()) {
            if (entry.getKey ().startsWith ("__PROP_")) {
                p.setProperty (entry.getKey ().substring (7), entry.getValue ());
            }
        }
        return p;
    }

    protected Properties decodeProperties (MapDataValue mdv) {
        Properties props = new Properties ();
        for (Entry<DataValue, DataValue> entry : mdv.all ().entrySet ()) {
            if (entry.getKey ().type ().equals (SCOPE.string ()) && entry.getValue ().type ().equals (SCOPE.string ())) {
                String key = ((StringValue )entry.getKey ()).value ();
                if (key.startsWith ("__PROP_")) {
                    key = key.substring (7);
                    props.setProperty (key, ((StringValue )entry.getValue ()).value ());
                }
            }
        }
        return props;
    }

    /**
     * Publishes a map onto the bus
     * 
     * @param msg
     *            the message to publish, as key/value pairs
     */
    public void publish (Map<String, String> msg) {
        m_client.send (encodeMap (msg));
    }

    /**
     * Implements a call and return connection over the bus. The call is represented in msg, which is sent to the bus.
     * The receiveListener is what is called when a call is responded to. This is done asynchronously, so clients who
     * want to block will need to handle their own blocking.
     * 
     * @param msg
     *            The call
     * @param receiveListener
     *            The listener to call when a response returns
     */
    public void sendAndReceive (Map<String, String> msg, final IESEBListener receiveListener) {
        // Generate a random reply key and put it in the message so that responder knows how to respond
        final String replyKey = UUID.randomUUID ().toString ();
        msg.put (ESEBConstants.MSG_REPLY_KEY, replyKey);

        // Add the reply listener to the reply queue
        synchronized (m_replyListeners) {
            m_replyListeners.put (replyKey, receiveListener);
        }

        // Add a reply queue that will process the reply 
        // Note that if we want the send/receive to be blocking, then that needs to be processed
        // by the client.
        final BusDataQueue replyQ = new BusDataQueue ();
        m_client.queue_group ().add (replyQ);

        replyQ.dispatcher ().add (new BusDataQueueListener () {
            @Override
            public void data_added_to_queue () {
                BusData bd = null;
                while ((bd = replyQ.poll ()) != null) {
                    DataValue v = bd.value ();
                    if (v instanceof MapDataValue) {
                        MapDataValue mdv = (MapDataValue )v;
                        StringValue repKey = (StringValue )mdv.get (SCOPE.string ().make (ESEBConstants.MSG_REPLY_KEY));
                        DataValue msgType = mdv.get (SCOPE.string ().make (ESEBConstants.MSG_TYPE_KEY));

                        // Make sure that the message is a reply, otherwise ignore
                        // TODO: Bug --> if it isn't a reply, we've removed it so others now cannot process it
                        if (SCOPE.string ().make (ESEBConstants.MSG_TYPE_REPLY).equals (msgType)) {
                            // Get the associated reply listener
                            IESEBListener l = null;
                            synchronized (m_replyListeners) {
                                l = m_replyListeners.remove (repKey.value ());
                            }
                            // Call the listener
                            if (l != null) {
                                Map<String, String> msg = decodeMap (mdv);
                                sanitizeMessage (msg);
                                l.receive (msg);
                            }
                            else {
                                LOGGER.error (MessageFormat.format (
                                        "Received a reply on ESEB for which there is no listener. For reply key: {0}",
                                        repKey.value ()));
                            }
                        }

                    }
                }
            }
        });

        // send the call
        m_client.send (encodeMap (msg));

    }

    /**
     * Removes any internal key, value pairs from the message
     * 
     * @param msg
     *            The message to sanitize
     */
    protected void sanitizeMessage (Map<String, String> msg) {
        msg.remove (ESEBConstants.MSG_REPLY_KEY);
    }


    /**
     * Adds a listener to the client queue.
     * 
     * @param l
     *            The listener to call if a message is received.
     */
    public void addListener (final IESEBListener l) {
        // Set up the queues
        final BusDataQueue clientReceiveQ = new BusDataQueue ();
        m_client.queue_group ().add (clientReceiveQ);
        clientReceiveQ.dispatcher ().add (new BusDataQueueListener () {

            @Override
            public void data_added_to_queue () {
                BusData bd = null;
                while ((bd = clientReceiveQ.poll ()) != null) {
                    DataValue v = bd.value ();
                    if (v instanceof MapDataValue) {
                        MapDataValue mdv = (MapDataValue )v;
                        Map<String, String> msg = decodeMap (mdv);
                        // Ignore any replies on this queue
                        if (!ESEBConstants.MSG_TYPE_REPLY.equals (msg.get (ESEBConstants.MSG_TYPE_KEY)))
                        {
                            l.receive (msg);
                        }
                    }
                }
            }
        });
    }

    public void close () throws IOException {
        if (m_client != null) {
            m_client.close ();
        }
        if (m_srvr != null) {
            m_srvr.close ();
        }
    }

}
