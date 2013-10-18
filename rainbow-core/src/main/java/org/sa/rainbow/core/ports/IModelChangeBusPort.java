package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;

public interface IModelChangeBusPort extends IRainbowMessageFactory {

    public void announce (IRainbowMessage event);
    public void announce (List<? extends IRainbowMessage> event);

}
