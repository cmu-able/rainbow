package edu.cmu.cs.able.eseb.conn;

import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.wt.CloseableListener;
import incubator.wt.WorkerThread;
import incubator.wt.WtState;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueGroup;
import edu.cmu.cs.able.eseb.BusDataQueueGroupImpl;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.ControlledDataTypeSocketConnection;
import edu.cmu.cs.able.eseb.ControlledDataTypeSocketConnectionImpl;
import edu.cmu.cs.able.eseb.DataTypeSocketConnectionImpl;
import edu.cmu.cs.able.eseb.filter.BusDataQueueGroupSink;
import edu.cmu.cs.able.eseb.filter.EventFilterChain;
import edu.cmu.cs.able.eseb.filter.EventSink;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Connection that can be used to send and receive data from the bus. This
 * class will establish the connection and re-establish it if it drops. Data
 * can be sent without the connection being established: it will be buffered
 * and sent when possible.
 */
public class BusConnection implements Closeable {
    /**
     * Time to "cool down" after an unsuccessful connection attempt. This is
     * used to prevent cycling very fast on connection attempts.
     */
    private static final long ESTABLISHER_COOLING_PERIOD_MS = 5_000;

    /**
     * Logger to use.
     */
    private static final Logger LOG = Logger.getLogger(BusConnection.class);

    /**
     * The host to connect to.
     */
    private String m_host;

    /**
     * The port to connect to.
     */
    private short m_port;

    /**
     * The connection state.
     */
    private BusConnectionState m_state;

    /**
     * The local dispatcher.
     */
    private LocalDispatcher<BusConnectionListener> m_dispatcher;

    /**
     * Buffer with pending data values to send.
     */
    private LinkedList<DataValue> m_out_buffer;

    /**
     * The socket connection if the client is connected. <code>null</code> if
     * the client is not connected.
     */
    private ControlledDataTypeSocketConnection m_connection;

    /**
     * Worker thread that establishes the connection. <code>null</code> if
     * the client is not connecting.
     */
    private WorkerThread m_connection_establisher;

    /**
     * Time at which the connection was established if the client is connected
     * and the time at which the client was disconnected if the client is
     * disconnected or connecting.
     */
    private Date m_connection_time;

    /**
     * Number of times the client has successfully connected to the bus.
     */
    private int m_connect_count;

    /**
     * Number of values received from the server.
     */
    private int m_receive_count;

    /**
     * Number of values sent to the server.
     */
    private int m_send_count;

    /**
     * Exception collector.
     */
    private LocalCollector m_collector;

    /**
     * The primitive scope.
     */
    private PrimitiveScope m_primitive_scope;

    /**
     * Queue used to receive data from the underlaying connection.
     */
    private BusDataQueue m_queue;

    /**
     * Queue group to inform connection listeners.
     */
    private BusDataQueueGroupImpl m_queue_group;

    /**
     * Incoming filter chain. Because the connection is reset every time we
     * reconnect, we need to have our own chain.
     */
    private EventFilterChain m_incoming_chain;

    /**
     * Outgoing filter chain. Because the connection is reset every time we
     * reconnect, we need to have our own chain.
     */
    private EventFilterChain m_outgoing_chain;

    /**
     * Encoding to use.
     */
    private DataValueEncoding m_encoding;

    /**
     * Creates a new client with the default encoding.
     * @param host the host to connect to
     * @param port the port
     * @param scope the data type scope
     */
    public BusConnection(String host, short port, PrimitiveScope scope) {
        this(host, port, scope, new DefaultTextEncoding(scope));
    }

    /**
     * Creates a new client.
     * @param host the host to connect to
     * @param port the port
     * @param scope the data type scope
     * @param encoding the encoding to use
     */
    public BusConnection(String host, short port, PrimitiveScope scope,
            DataValueEncoding encoding) {
        Ensure.not_null(host);
        Ensure.greater(port, 0);
        Ensure.not_null(scope);
        Ensure.not_null(encoding);

        m_host = host;
        m_port = port;
        m_state = BusConnectionState.DISCONNECTED;
        m_dispatcher = new LocalDispatcher<>();
        m_out_buffer = new LinkedList<>();
        m_connection = null;
        m_connection_establisher = null;
        m_connection_time = new Date();
        m_connect_count = 0;
        m_receive_count = 0;
        m_send_count = 0;
        m_encoding = encoding;
        m_collector = new LocalCollector("BusClient (" + host + ":" + port
                + ")");
        m_primitive_scope = scope;
        m_queue = new BusDataQueue();
        m_queue.dispatcher().add(new BusDataQueueListener() {
            @Override
            public void data_added_to_queue() {
                received();
            }
        });

        m_queue_group = new BusDataQueueGroupImpl(); 
        m_incoming_chain = new EventFilterChain(new BusDataQueueGroupSink(
                m_queue_group));
        m_outgoing_chain = new EventFilterChain(new EventSink() {
            @Override
            public void sink(BusData data) throws IOException {
                Ensure.not_null(data);
                internal_send(data.value());
            }
        });
    }

    /**
     * Starts the connection. This will establish the connection if
     * necessary. This method doesn't do anything if invoked when the
     * connection is already running.
     */
    public synchronized void start() {
        switch (m_state) {
        case CONNECTED:
        case CONNECTING:
            break;
        case DISCONNECTED:
            switch_disconnected_connecting();
            break;
        default:
            assert false;
        }
    }

    /**
     * Stops and disconnects the client.
     */
    public void stop() {
        /*
         * When stopping we want to wait for the connection establisher thread
         * to die. However, we can't do it from a synchronized block because
         * the establisher will need to acquire the lock in order to exit.
         */
        WorkerThread establisher;
        synchronized (this) {
            establisher = m_connection_establisher;

            switch (m_state) {
            case CONNECTED:
                switch_connected_disconnected();
                break;
            case CONNECTING:
                switch_connecting_disconnected();
                break;
            case DISCONNECTED:
                break;
            default:
                assert false;
            }
        }

        if (establisher != null) {
            while (establisher.state() != WtState.STOPPED) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    /*
                     * Ignored.
                     */
                }
            }
        }
    }

    /**
     * Adds a listener that is informed of changes in the connection and of
     * received values
     * @param l the listener
     */
    public void add_listener(BusConnectionListener l) {
        m_dispatcher.add(l);
    }

    /**
     * Removes a previously added listener.
     * @param l the listener
     */
    public void remove_listener(BusConnectionListener l) {
        m_dispatcher.remove(l);
    }

    /**
     * Obtains the current state of the client.
     * @return the state
     */
    public synchronized BusConnectionState state() {
        return m_state;
    }

    /**
     * Obtains the time at which the client connected or disconnected
     * depending whether the current state is connected or
     * connecting/disconnected.
     * @return the time
     */
    public synchronized Date connection_time() {
        return m_connection_time;
    }

    /**
     * Obtains the number of values received by the client.
     * @return the number of values
     */
    public synchronized int receive_count() {
        return m_receive_count;
    }

    /**
     * Obtains the number of values sent by the client.
     * @return the number of values
     */
    public synchronized int sent_count() {
        return m_send_count;
    }

    /**
     * Obtains the number of times the connection has successfully connected
     * to the bus.
     * @return the count
     */
    public synchronized int connect_count() {
        return m_connect_count;
    }

    /**
     * Sends a data type to the server or queues it locally if the connection
     * to the server has not yet been established. Note that value delivery
     * is not guaranteed if the connection is lost.
     * @param value the value to send
     */
    public synchronized void send(DataValue value) {
        try {
            this.notifyAll ();
            m_outgoing_chain.sink(new BusData(value));
        } catch (IOException e) {
            Ensure.never_thrown(e);
        }
    }

    /**
     * Method that actually does the send, after the outgoing chain has been
     * processed.
     * @param value the value to send
     */
    private synchronized void internal_send(DataValue value) {
        Ensure.not_null(value, "value == null");
        m_send_count++;

        switch (m_state) {
        case CONNECTED:
            /*
             * We want to preserve ordering of values sent, so if the buffer
             * has values, we need to send those first (and there should be
             * a dispatched event to do that).
             */
            if (m_out_buffer.size() > 0) {
                LOG.debug("Queueing: " + value);
                m_out_buffer.addLast(value);
            } else {
                try {
                    LOG.debug("Writing (online): " + value);
                    m_connection.write(value);
                } catch (IOException e) {
                    m_collector.collect(e, "send");
                    LOG.error("Error while writing {" + value + "}.", e);
                }
            }

            break;
        case CONNECTING:
        case DISCONNECTED:
            m_out_buffer.add(value);
            break;
        default:
            assert false;
        }
    }

    /**
     * Switches from DISCONNECTED to CONNECTING state.
     */
    private synchronized void switch_disconnected_connecting() {
        assert m_state == BusConnectionState.DISCONNECTED;

        m_state = BusConnectionState.CONNECTING;
        m_connection_establisher = new WorkerThread("Connection ("
                + m_host + ":" + m_port + ")") {
            @Override
            protected void do_cycle_operation() throws Exception {
                establisher_cycle();
            }
        };

        m_connection_establisher.start();
        notify_state_changed();
    }

    /**
     * Switches from CONNECTED to DISCONNECTED state.
     */
    private synchronized void switch_connected_disconnected() {
        assert m_state == BusConnectionState.CONNECTED;

        try {
            m_connection.close();
        } catch (IOException e) {
            m_collector.collect(e, "connect->disconnect");
            LOG.error("Error while closing connection.", e);
        }

        m_connection.stop();

        m_state = BusConnectionState.DISCONNECTED; 
        m_connection = null;
        m_connection_time = new Date();
        notify_state_changed();
    }

    /**
     * Switches from CONNECTING to DISCONNECTED state.
     */
    private synchronized void switch_connecting_disconnected() {
        assert m_state == BusConnectionState.CONNECTING;

        /*
         * Although not strictly necessary, if we set the state to DISCONNECTED
         * before asking for the establisher thread to stop (the method will
         * only return after the stop) we may avoid the cooling period of
         * the establisher if we hit while waiting for the connection. It is
         * still possible, due to a race condition, that we may have to wait
         * for the cooling period anyway.
         */
        m_state = BusConnectionState.DISCONNECTED;
        final WorkerThread ce = m_connection_establisher;
        m_connection_establisher = null;
        notify_state_changed();

        /*
         * The connection establisher may need to query our state to make sure
         * it quits. So we can't do it in this thread. 
         */
        m_dispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                ce.stop();
            }
        });
    }

    /**
     * Switches from CONNECTING to CONNECTED state.
     * @param s the connection socket
     * @throws IOException failed to modify the state
     */
    private synchronized void switch_connecting_connected(Socket s)
            throws IOException {
        assert m_state == BusConnectionState.CONNECTING;
        assert s != null;

        WorkerThread establisher = m_connection_establisher;

        m_state = BusConnectionState.CONNECTED;

        /*
         * The Eclipse warning on potential resource leak is true. If the
         * controlled connection constructor fails with an exception we'll
         * leak the socket connection. However, this is untestable because
         * the controlled connection cannot throw an exception in any
         * testable situation.  
         */
        @SuppressWarnings("resource")
        ControlledDataTypeSocketConnectionImpl impl = 
        new ControlledDataTypeSocketConnectionImpl(
                m_primitive_scope,
                new DataTypeSocketConnectionImpl("Connection " + m_host
                        + ":" + m_port, s, m_encoding,
                        m_primitive_scope));
        m_connection = impl;
        m_connection_establisher = null;
        m_connection_time = new Date();
        m_connect_count++;

        send_clear_out_buffer();

        m_connection.queue_group().add(m_queue);

        m_connection.closeable_dispatcher().add(new CloseableListener() {
            @Override
            public void closed(IOException e) {
                /*
                 * If we're not in connected state, then a close is irrelevant.
                 * It may have been generated on purpose when the connection
                 * is closed.
                 */
                synchronized (BusConnection.this) {
                    if (m_state == BusConnectionState.CONNECTED) {
                        switch_connected_connecting();
                    }
                }
            }
        });

        m_connection.start();
        notify_state_changed();

        /*
         * This may fire an exception if it is the establisher that is
         * trying to stop itself. That is fine because the exception will
         * propagate and will terminate the thread.
         */
        establisher.stop();
    }

    /**
     * Switches from CONNECTED to CONNECTING
     */
    private void switch_connected_connecting() {
        switch_connected_disconnected();
        switch_disconnected_connecting();
    }

    /**
     * Invoked from within the establisher thread cycle without any locks on
     * the current object.
     * @throws InterruptedException wait interrupted
     */
    @SuppressWarnings("resource")
    private void establisher_cycle() throws InterruptedException {
        synchronized (this) {
            /*
             * We only want to establish a connecting if we're connecting.
             * However, because there is a lot of asynchronous stuff going on
             * this is not guaranteed to happen.
             */
            if (m_state != BusConnectionState.CONNECTING) {
                wait(ESTABLISHER_COOLING_PERIOD_MS);
                return;
            }
        }

        /*
         * Try to connect.
         */
        Socket s = null;
        try {
            s = new Socket(m_host, m_port);
        } catch (IOException e) {
            m_collector.collect(e, "connect");
            LOG.info("Error while connecting to host " + m_host + ", port "
                    + m_port + ".", e);
        }

        /*
         * If we didn't manage to connect, we should cool a little bit to
         * avoid spamming connection attempts.
         */
        synchronized (this) {
            if (s != null) {
                if (m_state == BusConnectionState.CONNECTING) {
                    try {
                        switch_connecting_connected(s);
                    } catch (IOException e) {
                        m_collector.collect(e, "establish after connect");
                        LOG.error("Error establishing connection after socket "
                                + "connect.", e);
                    }
                } else {
                    /*
                     * We managed to connect but now we should disconnect.
                     */
                    try {
                        s.close();
                    } catch (IOException e) {
                        m_collector.collect(e, "close unnecessary socket");
                        LOG.error("Error while closing unnecessary socket.", e);
                    }

                    s = null;
                }
            }

            if (s == null && m_state == BusConnectionState.CONNECTING) {
                wait(ESTABLISHER_COOLING_PERIOD_MS);
            }
        }
    }

    /**
     * Dispatch an event to send the topmost value in the output buffer if
     * there is one and trigger another event.
     */
    private synchronized void send_clear_out_buffer() {
        m_dispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                synchronized (BusConnection.this) {
                    if (m_out_buffer.size() > 0
                            && m_state == BusConnectionState.CONNECTED) {
                        DataValue v = m_out_buffer.removeFirst();

                        try {
                            LOG.debug("Writing (deferred): " + v);
                            m_connection.write(v);
                        } catch (IOException e) {
                            m_collector.collect(e, "send from buffer");
                            LOG.error("Error while writing {" + v + "}.", e);
                        }

                        send_clear_out_buffer();
                    }
                }
            }
        });
    }

    /**
     * Data may have been received in the queue.
     */
    private synchronized void received() {
        BusData data;
        while ((data = m_queue.poll()) != null) {
            m_receive_count++;
            LOG.debug("Received: {" + data.value() + "}.");
            try {
                m_incoming_chain.sink(data);
            } catch (IOException e) {
                Ensure.never_thrown(e);
            }
        }
    }

    /**
     * Notifies all listeners that the connection's state has changed.
     */
    private synchronized void notify_state_changed() {
        m_dispatcher.dispatch(new DispatcherOp<BusConnectionListener>() {
            @Override
            public void dispatch(BusConnectionListener l) {
                l.connection_state_changed();
            }
        });
    }

    /**
     * Same as {@link #stop}.
     */
    @Override
    public void close() throws IOException {
        stop();
    }

    /**
     * Obtains the exception collector.
     * @return the collector
     */
    public synchronized LocalCollector collector() {
        return m_collector;
    }

    /**
     * Obtains the queue group to register receiving queues.
     * @return the group
     */
    public BusDataQueueGroup queue_group() {
        return m_queue_group;
    }

    /**
     * Obtains the host this client is connected (or connecting) to.
     * @return the host
     */
    public synchronized String host() {
        return m_host;
    }

    /**
     * Obtains the port this client is connected (or connecting) to.
     * @return the port
     */
    public synchronized short port() {
        return m_port;
    }

    /**
     * Obtains the incoming event filter chain.
     * @return the chain
     */
    public synchronized EventFilterChain incoming_chain() {
        return m_incoming_chain;
    }

    /**
     * Obtains the outgoing event filter chain.
     * @return the chain
     */
    public synchronized EventFilterChain outgoing_chain() {
        return m_outgoing_chain;
    }

    /**
     * Obtains the primitive scope used in this connection.
     * @return the connection
     */
    public PrimitiveScope primitive_scope() {
        return m_primitive_scope;
    }

    /**
     * Obtains the encoding used in the connection.
     * @return the encoding used
     */
    public DataValueEncoding encoding() {
        return m_encoding;
    }
}
