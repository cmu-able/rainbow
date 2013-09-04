package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.bus.EventBus;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
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
    static Logger               LOGGER             = Logger.getLogger (ESEBConnector.class);

    public static enum ChannelT {
        HEALTH, MODEL_US, MODE_DS, MODEL_CHANGE, SYSTEM_US
    };


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
        public void receive (RainbowESEBMessage msg);
    }


    /** The server corresponding to this connector **/
    protected EventBus                          m_srvr;
    /** The client for this connector to publish and receive information **/
    protected BusConnection                     m_client;
    /** The set of listeners that are awaiting replies. **/
    private static Map<String, IESEBListener> m_replyListeners     = new HashMap<> ();
    private ChannelT m_channel;

    /**
     * Constructs a new ESEBConnector, connecting to a remote bus
     * 
     * @param remoteHost
     *            The host to connect to
     * @param remotePort
     *            The port to connect to
     * @throws IOException
     */
    public ESEBConnector (String remoteHost, short remotePort, ChannelT channel) throws IOException {
        m_channel = channel;
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
    public ESEBConnector (short port, ChannelT channel) throws IOException {
        this.m_channel = channel;
        // Create the server on this port
        m_srvr = ESEBProvider.getBusServer (port);
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

        m_client = ESEBProvider.getBusClient (remoteHost, remotePort);
    }

    /**
     * Publishes a map onto the bus
     * 
     * @param msg
     *            the message to publish, as key/value pairs
     */
    public void publish (RainbowESEBMessage msg) {
        msg.setProperty (ESEBConstants.MSG_SENT, System.currentTimeMillis ());
        m_client.send (msg.getDataValue ());
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
    public void sendAndReceive (RainbowESEBMessage msg, final IESEBListener receiveListener) {
        // Generate a random reply key and put it in the message so that responder knows how to respond
        final String replyKey = UUID.randomUUID ().toString ();
        msg.setProperty (ESEBConstants.MSG_REPLY_KEY, replyKey);

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
                        RainbowESEBMessage msg = new RainbowESEBMessage (mdv);
                        if (!msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (m_channel.name ())) {
                            continue;
                        }
                        String repKey = (String )msg.getProperty (ESEBConstants.MSG_REPLY_KEY);
                        Object msgType = msg.getProperty (ESEBConstants.MSG_TYPE_KEY);

                        // Make sure that the message is a reply, otherwise ignore
                        // TODO: Bug --> if it isn't a reply, we've removed it so others now cannot process it
                        if (ESEBConstants.MSG_TYPE_REPLY.equals (msgType)) {
                            // Get the associated reply listener
                            IESEBListener l = null;
                            synchronized (m_replyListeners) {
                                l = m_replyListeners.remove (repKey);
                            }
                            // Call the listener
                            if (l != null) {
                                sanitizeMessage (msg);
                                l.receive (msg);
                            }
                            else {
                                LOGGER.error (MessageFormat.format (
                                        "Received a reply on ESEB for which there is no listener. For reply key: {0}",
                                        repKey));
                            }
                            replyQ.dispatcher ().remove (this);
                        }

                    }
                }
            }
        });

        // send the call
        publish (msg);

    }

    public class BlockingListener implements IESEBListener {

        private IESEBListener m_l;
        boolean               ret = false;

        public BlockingListener (IESEBListener l) {
            m_l = l;

        }

        @Override
        public void receive (RainbowESEBMessage msg) {
            m_l.receive (msg);
            ret = true;
            synchronized (this) {
                this.notifyAll ();
            }
        }

    }

    public void blockingSendAndReceive (RainbowESEBMessage msg, final IESEBListener l, long timeout)
            throws RainbowConnectionException {
        BlockingListener bl = new BlockingListener (l);
        synchronized (bl) {
            sendAndReceive (msg, bl);
            try {
                bl.wait (timeout);
            }
            catch (InterruptedException e) {
            }
        }
        if (!bl.ret)
            throw new RainbowConnectionException (MessageFormat.format (
                    "Blocking send and receive did not return in specified time {0}", timeout));
    }

    /**
     * Removes any internal key, value pairs from the message
     * 
     * @param msg
     *            The message to sanitize
     */
    protected void sanitizeMessage (RainbowESEBMessage msg) {
        msg.removeProperty (ESEBConstants.MSG_REPLY_KEY);
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
                        RainbowESEBMessage msg = new RainbowESEBMessage (mdv);
                        if (!msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (m_channel.name ())) {
                            continue;
                        }
                        // Ignore any replies on this queue
                        if (!ESEBConstants.MSG_TYPE_REPLY.equals (msg.getProperty (ESEBConstants.MSG_TYPE_KEY)))
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

    public RainbowESEBMessage createMessage () {
        RainbowESEBMessage msg = new RainbowESEBMessage ();
        msg.setProperty (ESEBConstants.MSG_CHANNEL_KEY, m_channel.name ());
        return msg;
    }

    public void replyToMessage (RainbowESEBMessage msg, Object result) {
        String repKey = (String )msg.getProperty (ESEBConstants.MSG_REPLY_KEY);
        if (repKey != null) {
            RainbowESEBMessage reply = createMessage (/*ChannelT.valueOf ((String )msg
                    .getProperty (ESEBConstants.MSG_CHANNEL_KEY))*/);
            reply.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
            reply.setProperty (ESEBConstants.MSG_REPLY_KEY, repKey);
            try {
                reply.setProperty (ESEBConstants.MSG_REPLY_VALUE, result);
            }
            catch (RainbowException e) {
            }
            publish (reply);
        }
    }

}
