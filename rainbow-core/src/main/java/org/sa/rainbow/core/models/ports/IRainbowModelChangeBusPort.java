package org.sa.rainbow.core.models.ports;

import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;

public interface IRainbowModelChangeBusPort extends IRainbowMessageFactory {

    public void announce (IRainbowMessage event);
    public void announce (List<? extends IRainbowMessage> event);

}
