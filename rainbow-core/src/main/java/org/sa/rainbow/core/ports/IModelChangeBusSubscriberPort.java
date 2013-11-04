package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.IModelInstance;

public interface IModelChangeBusSubscriberPort {

    public interface IRainbowChangeBusSubscription {
        public boolean matches (IRainbowMessage message);
    }

    public interface IRainbowModelChangeCallback<T> {
        public void onEvent (IModelInstance<T> model, IRainbowMessage message);
    }

    public void subscribe (IRainbowChangeBusSubscription subscriber, IRainbowModelChangeCallback callback);

    public void unsubscribe (IRainbowModelChangeCallback callback);
}
