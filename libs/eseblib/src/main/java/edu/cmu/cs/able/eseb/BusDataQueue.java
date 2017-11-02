package edu.cmu.cs.able.eseb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Queue used to receive data from the event bus. This queue guarantees that
 * received data is ordered. The queue can notify listener when it receives
 * new data.
 */
public class BusDataQueue {
    /**
     * The queue implementation.
     */
    private Queue<BusData> m_queue;

    int count = 0;
    static final int MOD = 1000;
    String creationStack;

    /**
     * Dispatcher that notifies listeners.
     */
    private LocalDispatcher<BusDataQueueListener> m_dispatcher;

    /**
     * Creates a new queue.
     */
    public BusDataQueue () {
        m_queue = new LinkedList<> ();
        m_dispatcher = new LocalDispatcher<> ();
        try {
            throw new Exception ();
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            e.printStackTrace (new PrintStream (baos));
            try {
                baos.flush ();
                baos.close ();
            } catch (IOException e1) {
            }
            creationStack = baos.toString ();
        }
    }

    /**
     * Adds data to the queue and notifies listeners.
     *
     * @param v the data to add
     */
    public synchronized void add (BusData v) {
        Ensure.not_null (v);
        m_queue.add (v);
        count = (count + 1) % MOD;
        if (count == 0) {
            System.out.println ("-------------------> Size of data queue after 1000 adds is " + m_queue.size ());
            if (m_queue.size () > 900) {
                System.out.println ("This Queue was created with the following stack:\n" + creationStack);
            }
        }
        m_dispatcher.dispatch (new DispatcherOp<BusDataQueueListener> () {
            @Override
            public void dispatch (BusDataQueueListener l) {
                l.data_added_to_queue ();
            }
        });
    }

    /**
     * Obtains the dispatcher used to register listeners on this queue.
     *
     * @return the dispatcher
     */
    public Dispatcher<BusDataQueueListener> dispatcher () {
        return m_dispatcher;
    }

    /**
     * Removes the first element from the queue.
     *
     * @return the first element or <code>null</code> if the queue is empty
     */
    public synchronized BusData poll () {
        return m_queue.poll ();
    }
}
