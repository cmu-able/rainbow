package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;

public interface IModelChangeBusPort extends IRainbowMessageFactory, IDisposablePort {

    /**
     * Announce a message (that is an operation) on the change bus
     * 
     * @param event
     */
    public void announce (IRainbowMessage event);

    /**
     * Announce a list of messages on the change bus
     * 
     * @param events
     */
    public void announce (List<? extends IRainbowMessage> events);


}
