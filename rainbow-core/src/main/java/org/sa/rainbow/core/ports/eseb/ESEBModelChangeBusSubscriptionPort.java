package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelInstanceProvider;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.util.Pair;

public class ESEBModelChangeBusSubscriptionPort implements IModelChangeBusSubscriberPort {

    private ESEBConnector m_connector;
    private Collection<Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback>> m_subscribers = new LinkedList<> ();

    public ESEBModelChangeBusSubscriptionPort (final IModelInstanceProvider mm) throws IOException {
        this (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), mm);

    }

    public ESEBModelChangeBusSubscriptionPort (String esebClientHost, short esebClientPort,
            final IModelInstanceProvider mm)
            throws IOException {
        m_connector = new ESEBConnector (esebClientHost, esebClientPort,
                ChannelT.MODEL_CHANGE);
        m_connector.addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.MODEL_CHANGE.name ())) {
                    synchronized (m_subscribers) {
                        for (Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> pair : m_subscribers) {
                            if (pair.firstValue ().matches (msg)) {
                                IModelInstance<Object> model = mm.getModelInstance (
                                        (String )msg.getProperty (IModelChangeBusPort.MODEL_TYPE_PROP),
                                        (String )msg.getProperty (IModelChangeBusPort.MODEL_NAME_PROP));
                                pair.secondValue ().onEvent (model, msg);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void subscribe (IRainbowChangeBusSubscription subscriber, IRainbowModelChangeCallback callback) {
        Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = new Pair<> (subscriber,
                callback);
        synchronized (m_subscribers) {
            m_subscribers.add (subscription);
        }
    }

    @Override
    public void unsubscribe (IRainbowModelChangeCallback callback) {
        synchronized (m_subscribers) {
            for (Iterator i = m_subscribers.iterator (); i.hasNext ();) {
                Pair<IRainbowChangeBusSubscription,IRainbowModelChangeCallback> subscription = (Pair<IRainbowChangeBusSubscription,IRainbowModelChangeCallback> )i.next ();
                if (subscription.secondValue () == callback) {
                    i.remove ();
                }
            }
        }
    }

}