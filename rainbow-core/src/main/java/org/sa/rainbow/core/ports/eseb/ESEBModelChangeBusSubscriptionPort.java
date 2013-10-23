package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.util.Pair;

public class ESEBModelChangeBusSubscriptionPort implements IModelChangeBusSubscriberPort {

    private ESEBConnector m_connector;
    private Collection<Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback>> m_subscribers = new LinkedList<> ();

    public ESEBModelChangeBusSubscriptionPort () throws IOException {
        m_connector = new ESEBConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.MODEL_CHANGE);
        final ModelsManager mm = Rainbow.instance ().getRainbowMaster ().modelsManager ();
        m_connector.addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.MODEL_CHANGE.name ())) {
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
        });
    }

    @Override
    public void subscribe (IRainbowChangeBusSubscription subscriber, IRainbowModelChangeCallback callback) {
        Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = new Pair<> (subscriber,
                callback);
        m_subscribers.add (subscription);
    }

}
